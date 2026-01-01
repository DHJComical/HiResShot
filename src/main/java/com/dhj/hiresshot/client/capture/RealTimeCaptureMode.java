package com.dhj.hiresshot.client.capture;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class RealTimeCaptureMode extends AbstractCaptureMode {

    private int framesToWait;

    public RealTimeCaptureMode(Minecraft mc, CaptureState state) {
        super(mc, state);
    }

    @Override
    public void start(int warmupFrames) {
        this.framesToWait = 1;

        try {
            mc.player.sendMessage(new TextComponentString("§eCapturing..."));
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

        if (mc.displayWidth != state.targetWidth || mc.displayHeight != state.targetHeight) {
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
            mc.player.sendMessage(new TextComponentString("§cError: " + e.getMessage()));
        } finally {
            if (active) {
                restoreState();
            }
        }
    }
}