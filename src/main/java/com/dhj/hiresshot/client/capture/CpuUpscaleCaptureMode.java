package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.HRSConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.TextComponent;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CpuUpscaleCaptureMode extends AbstractCaptureMode {

    private static final int SAFE_MAX_GPU_SIZE = 16384;

    public CpuUpscaleCaptureMode(Minecraft mc, CaptureState state) {
        super(mc, state);
    }

    @Override
    public void start(int warmupFrames) {
        try {
            int hardwareLimit = RenderSystem.maxSupportedTextureSize();
            int limit = Math.min(hardwareLimit - 100, SAFE_MAX_GPU_SIZE);

            double tW = state.targetWidth;
            double tH = state.targetHeight;
            double scale = 1.0;

            if (tW > limit || tH > limit) {
                double scaleW = (double) limit / tW;
                double scaleH = (double) limit / tH;
                scale = Math.min(scaleW, scaleH);
            }

            int renderWidth = (int) (tW * scale);
            int renderHeight = (int) (tH * scale);
            renderWidth = Math.max(1, renderWidth);
            renderHeight = Math.max(1, renderHeight);

            if (mc.player != null) {
                mc.player.sendMessage(new TextComponent("§eCPU Hybrid Capture..."), mc.player.getUUID());
            }

            if (renderWidth < state.targetWidth || renderHeight < state.targetHeight) {
                if (mc.player != null) {
                    mc.player.sendMessage(new TextComponent("§7GPU Base: " + renderWidth + "x" + renderHeight), mc.player.getUUID());
                    mc.player.sendMessage(new TextComponent("§bCPU Upscale -> " + state.targetWidth + "x" + state.targetHeight), mc.player.getUUID());
                }
                } else {
                if (mc.player != null) {
                    mc.player.sendMessage(new TextComponent("§aNative Render: " + renderWidth + "x" + renderHeight), mc.player.getUUID());
                }
            }

            resize(renderWidth, renderHeight);

            if (HRSConfig.CLIENT.hideGui.get()) {
                mc.options.hideGui = true;
            }

            for (int i = 0; i < warmupFrames; i++) {
                renderFrame();
            }

            renderFrame();
            saveAndProcess();

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

    private void saveAndProcess() {
        try {
            File gameDir = mc.gameDirectory;
            File screenshotsDir = new File(gameDir, "screenshots");
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            File rawFile = new File(screenshotsDir, dateStr + "_raw.png");
            File finalFile = new File(screenshotsDir, dateStr + "_hrs_upscaled.png");

            NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget());
            image.writeToFile(rawFile);
            image.close();

            new Thread(() -> {
                try {
                    LOGGER.info("Starting CPU Upscaling...");

                    BufferedImage bufImg = ImageIO.read(rawFile);

                    double wr = (double) state.targetWidth / (double) bufImg.getWidth();
                    double hr = (double) state.targetHeight / (double) bufImg.getHeight();

                    AffineTransformOp ato = new AffineTransformOp(
                            AffineTransform.getScaleInstance(wr, hr),
                            AffineTransformOp.TYPE_BICUBIC
                    );

                    BufferedImage finalImg = ato.filter(bufImg, null);
                    ImageIO.write(finalImg, "png", finalFile);

                    if (rawFile.exists()) {
                        rawFile.delete();
                    }

                    mc.execute(() -> {
                                if (mc.player != null) {
                                    mc.player.sendMessage(new TextComponent("§aUpscaling Finished: " + finalFile.getName()), mc.player.getUUID());
                                }
                            }
                    );

                } catch (Exception e) {
                    LOGGER.error("CPU Upscale failed", e);
                    mc.execute(() -> {
                                if (mc.player != null) {
                                    mc.player.sendMessage(new TextComponent("§cPost-Processing Failed: " + e.getMessage()), mc.player.getUUID());
                                }
                            }
                    );
                }
            }, "HiResShot-Upscaler").start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}