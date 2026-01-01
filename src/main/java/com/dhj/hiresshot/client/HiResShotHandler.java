package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.client.capture.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class HiResShotHandler {

    private AbstractCaptureMode currentMode = null;
    private static boolean registered = false;

    public static void register() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(new HiResShotHandler());
            registered = true;
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyHandler.KEY_CAPTURE.isPressed()) {
            startCapture();
        }
    }

    public void startCapture() {
        if (currentMode != null && currentMode.isActive()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (!OpenGlHelper.isFramebufferEnabled()) {
            mc.player.sendMessage(new TextComponentString("§cError: Framebuffer disabled!"));
            return;
        }

        int scaleFactor = HRSConfig.multiplier;
        boolean useCustomRes = HRSConfig.useCustomResolution;
        int instantWarmup = HRSConfig.warmupFrames;
        int realtimeDelay = HRSConfig.realtimeDelay;
        boolean hidePlayer = HRSConfig.hidePlayer;
        CaptureMode mode = HRSConfig.captureMode;

        int currentWidth = mc.displayWidth;
        int currentHeight = mc.displayHeight;

        int tW, tH;
        if (useCustomRes) {
            tW = HRSConfig.customWidth;
            tH = HRSConfig.customHeight;
        } else {
            tW = currentWidth * scaleFactor;
            tH = currentHeight * scaleFactor;
        }

        int maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        if (mode != CaptureMode.CPU_UPSCALE && (tW > maxTexSize || tH > maxTexSize)) {
            mc.player.sendMessage(new TextComponentString("§cError: Size " + tW + "x" + tH + " > GPU Max " + maxTexSize));
            mc.player.sendMessage(new TextComponentString("§eTip: Switch to 'CPU_UPSCALE' mode."));
            return;
        }

        CaptureState state = new CaptureState(mc, tW, tH, hidePlayer);

        int framesToPass = 0;
        switch (mode) {
            case REAL_TIME:
                currentMode = new RealTimeCaptureMode(mc, state);
                framesToPass = realtimeDelay;
                break;
            case INSTANT:
                currentMode = new InstantCaptureMode(mc, state);
                framesToPass = instantWarmup;
                break;
            case CPU_UPSCALE:
                currentMode = new CpuUpscaleCaptureMode(mc, state);
                framesToPass = instantWarmup;
                break;
        }

        if (currentMode != null) {
            currentMode.start(framesToPass);
        }
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
            if (event.getEntityPlayer().equals(Minecraft.getMinecraft().player)) {
                event.setCanceled(true);
            }
        }
    }
}