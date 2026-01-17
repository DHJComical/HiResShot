package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.mixin.WindowAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class RealTimeCaptureMode extends AbstractCaptureMode {

    private int framesToWait;

    public RealTimeCaptureMode(Minecraft mc, CaptureState state) {
        super(mc, state);
    }

    @Override
    public void start(int delayFrames) {
        this.framesToWait = Math.max(1, delayFrames);
        try {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§eCapturing... Waiting " + framesToWait + " frames."), false);
            }
            applyResolution();
        } catch (OutOfMemoryError e) {
            handleOOM(e);
        } catch (Exception e) {
            LOGGER.error("Start failed", e);
            restoreState();
        }
    }

    @Override
    public void onRenderTick() {
        if (!active) return;

        resize(state.targetWidth, state.targetHeight);

        if (framesToWait > 0) {
            RenderSystem.outputColorTextureOverride = mc.getMainRenderTarget().getColorTextureView();
            RenderSystem.outputDepthTextureOverride = mc.getMainRenderTarget().getDepthTextureView();
            mc.gameRenderer.resize(state.targetWidth, state.targetHeight);
            framesToWait--;
            return;
        }

        try {
            saveScreenshot();
        } finally {
            RenderSystem.outputColorTextureOverride = null;
            RenderSystem.outputDepthTextureOverride = null;
            if (active) {
                restoreState();
            }
        }
    }
}