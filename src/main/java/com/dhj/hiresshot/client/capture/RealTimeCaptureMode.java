package com.dhj.hiresshot.client.capture;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import com.dhj.hiresshot.mixin.WindowAccessor;
import net.minecraft.network.chat.TextComponent;

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
                mc.player.sendMessage(new TextComponent("§eCapturing... Waiting " + framesToWait + " frames."), mc.player.getUUID());
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
        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        if (window.getFramebufferWidth() != state.targetWidth ||
                window.getFramebufferHeight() != state.targetHeight) {
            resize(state.targetWidth, state.targetHeight);
        }

        if (framesToWait > 0) {
            framesToWait--;
            return;
        }

        try {
            saveScreenshot();
        } catch (OutOfMemoryError e) {
            handleOOM(e);
        } catch (Exception e) {
            LOGGER.error("Save failed", e);
            if (mc.player != null) {
                mc.player.sendMessage(new TextComponent("§cError: " + e.getMessage()), mc.player.getUUID());
            }
        } finally {
            if (active) restoreState();
        }
    }
}