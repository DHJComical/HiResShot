package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.HRSConfig;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CpuUpscaleCaptureMode extends AbstractCaptureMode {

    private static final int SAFE_MAX_GPU_SIZE = 16384;
    private int renderWidth;
    private int renderHeight;

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

            this.renderWidth = (int) (tW * scale);
            this.renderHeight = (int) (tH * scale);
            this.renderWidth = Math.max(1, this.renderWidth);
            this.renderHeight = Math.max(1, this.renderHeight);

            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("§eCPU Hybrid Capture..."));
            }

            if (renderWidth < state.targetWidth || renderHeight < state.targetHeight) {
                mc.player.sendSystemMessage(Component.literal("§7GPU Base: " + renderWidth + "x" + renderHeight));
                mc.player.sendSystemMessage(Component.literal("§bCPU Upscale -> " + state.targetWidth + "x" + state.targetHeight));
            } else {
                mc.player.sendSystemMessage(Component.literal("§aNative Render: " + renderWidth + "x" + renderHeight));
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
                mc.player.sendSystemMessage(Component.literal("§cError: " + e.getMessage()));
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
                                    mc.player.sendSystemMessage(Component.literal("§aUpscaling Finished: " + finalFile.getName()));
                                }
                            }
                    );

                } catch (Exception e) {
                    LOGGER.error("CPU Upscale failed", e);
                    mc.execute(() -> {
                                if (mc.player != null) {
                                    mc.player.sendSystemMessage(Component.literal("§cPost-Processing Failed: " + e.getMessage()));
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