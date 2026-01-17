package com.dhj.hiresshot.client.capture;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class InstantCaptureMode extends AbstractCaptureMode {

    public InstantCaptureMode(Minecraft mc, CaptureState state) {
        super(mc, state);
    }

    @Override
    public void start(int warmupFrames) {
        try {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§eInstant Capture..."),  false);
            }

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
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§cError: " + e.getMessage()),  false);
            }
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