package com.dhj.hiresshot.client.capture;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class InstantCaptureMode extends AbstractCaptureMode {

    public InstantCaptureMode(Minecraft mc, CaptureState state) {
        super(mc, state);
    }

    @Override
    public void start(int warmupFrames) {
        try {
            mc.player.sendMessage(new TextComponentString("§eInstant Capture..."));

            applyResolution();

            for (int i = 0; i < warmupFrames; i++) {
                renderFrame();
            }

            renderFrame();
            saveScreenshot();

        } catch (OutOfMemoryError e) {
            handleOOM(e);
        } catch (Exception e) {
            LOGGER.error("Capture failed", e);
            mc.player.sendMessage(new TextComponentString("§cError: " + e.getMessage()));
        } finally {
            if (active) {
                restoreState();
            }
        }
    }

    @Override
    public void onRenderTick() {
    }
}