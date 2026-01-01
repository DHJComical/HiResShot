package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.mixin.WindowAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class RealTimeCaptureMode extends AbstractCaptureMode {

    private int framesToWait;

    public RealTimeCaptureMode(Minecraft mc, CaptureState state) {
        super(mc, state);
    }

    @Override
    public void start(int warmupFrames) {
        this.framesToWait = 1;
        try {
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("§eCapturing..."));
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
                mc.player.sendSystemMessage(Component.literal("§cError: " + e.getMessage()));
            }
        } finally {
            if (active) {
                restoreState();
            }
        }
    }
}