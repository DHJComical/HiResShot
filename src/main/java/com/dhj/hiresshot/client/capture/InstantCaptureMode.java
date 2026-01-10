package com.dhj.hiresshot.client.capture;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

public class InstantCaptureMode extends AbstractCaptureMode {

    public InstantCaptureMode(Minecraft mc, CaptureState state) {
        super(mc, state);
    }

    @Override
    public void start(int warmupFrames) {
        try {
            if (mc.player != null) {
                mc.player.sendMessage(new TextComponent("§eInstant Capture..."), mc.player.getUUID());
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
                mc.player.sendMessage(new TextComponent("§cError: " + e.getMessage()), mc.player.getUUID());
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