package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.client.capture.*;
import com.dhj.hiresshot.mixin.WindowAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        int instantWarmup = HRSConfig.CLIENT.warmupFrames.get();
        int realtimeDelay = HRSConfig.CLIENT.realtimeDelay.get();
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

        int framesToPass = 0;
        switch (mode) {
            case REAL_TIME -> {
                currentMode = new RealTimeCaptureMode(mc, state);
                framesToPass = realtimeDelay;
            }
            case INSTANT -> {
                currentMode = new InstantCaptureMode(mc, state);
                framesToPass = instantWarmup;
            }
            case CPU_UPSCALE -> {
                currentMode = new CpuUpscaleCaptureMode(mc, state);
                framesToPass = instantWarmup;
            }
        }

        currentMode.start(framesToPass);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && currentMode != null) {
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
            if (event.getEntity().equals(Minecraft.getInstance().player)) {
                event.setCanceled(true);
            }
        }
    }
}