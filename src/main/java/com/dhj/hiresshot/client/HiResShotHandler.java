package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.client.capture.*;
import com.dhj.hiresshot.mixin.WindowAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

public class HiResShotHandler {

    private AbstractCaptureMode currentMode = null;

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (KeyInit.KEY_CAPTURE.consumeClick()) {
            startCapture();
        }
    }

    public void startCapture() {
        if (currentMode != null && currentMode.isActive()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getMainRenderTarget() == null) return;

        int scaleFactor = HRSConfig.CLIENT.multiplier.get();
        boolean useCustomRes = HRSConfig.CLIENT.useCustomResolution.get();
        int configWarmup = HRSConfig.CLIENT.warmupFrames.get();
        boolean hidePlayer = HRSConfig.CLIENT.hidePlayer.get();
        CaptureMode mode = HRSConfig.CLIENT.captureMode.get();

        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        int currentWidth = window.getFramebufferWidth();
        int currentHeight = window.getFramebufferHeight();

        int tW, tH;
        if (useCustomRes) {
            tW = HRSConfig.CLIENT.customWidth.get();
            tH = HRSConfig.CLIENT.customHeight.get();
        } else {
            tW = currentWidth * scaleFactor;
            tH = currentHeight * scaleFactor;
        }

        int maxTexSize = RenderSystem.maxSupportedTextureSize();
        if (mode != CaptureMode.CPU_UPSCALE && (tW > maxTexSize || tH > maxTexSize)) {
            mc.player.sendSystemMessage(Component.literal("§cError: Size " + tW + "x" + tH + " > GPU Max " + maxTexSize));
            mc.player.sendSystemMessage(Component.literal("§eTip: Switch to 'CPU_UPSCALE' mode."));
            return;
        }

        CaptureState state = new CaptureState(mc, tW, tH, hidePlayer);

        switch (mode) {
            case REAL_TIME -> currentMode = new RealTimeCaptureMode(mc, state);
            case INSTANT -> currentMode = new InstantCaptureMode(mc, state);
            case CPU_UPSCALE -> currentMode = new CpuUpscaleCaptureMode(mc, state);
        }

        currentMode.start(configWarmup);
    }

    // 【重要变化】使用 RenderFrameEvent.Post 替代 TickEvent
    @SubscribeEvent
    public void onRenderFrame(RenderFrameEvent.Post event) {
        if (currentMode != null) {
            if (currentMode.isActive()) {
                currentMode.onRenderTick();
            } else {
                currentMode = null;
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (currentMode != null && currentMode.shouldHidePlayer()) {
            if (event.getEntity() == Minecraft.getInstance().player) {
                event.setCanceled(true);
            }
        }
    }
}