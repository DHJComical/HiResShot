package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.HRSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.opengl.GL11;

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
            int hardwareLimit = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);

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

            mc.player.sendMessage(new TextComponentString("§eCPU Hybrid Capture..."));

            if (renderWidth < state.targetWidth || renderHeight < state.targetHeight) {
                mc.player.sendMessage(new TextComponentString("§7GPU Base: " + renderWidth + "x" + renderHeight));
                mc.player.sendMessage(new TextComponentString("§bCPU Upscale -> " + state.targetWidth + "x" + state.targetHeight));
            } else {
                mc.player.sendMessage(new TextComponentString("§aNative Render: " + renderWidth + "x" + renderHeight));
            }

            resize(renderWidth, renderHeight);

            if (HRSConfig.hideGUI) {
                mc.gameSettings.hideGUI = true;
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

    private void saveAndProcess() {
        try {
            File gameDir = mc.gameDir;
            File screenshotsDir = new File(gameDir, "screenshots");
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());

            String rawFileName = dateStr + "_raw.png";
            File rawFile = new File(screenshotsDir, rawFileName);

            String finalFileName = dateStr + "_hrs_upscaled.png";
            File finalFile = new File(screenshotsDir, finalFileName);

            ScreenShotHelper.saveScreenshot(gameDir, rawFileName, renderWidth, renderHeight, mc.getFramebuffer());

            new Thread(() -> {
                try {
                    int retries = 0;
                    while (!rawFile.exists() && retries < 50) {
                        Thread.sleep(100);
                        retries++;
                    }

                    if (!rawFile.exists()) {
                        LOGGER.error("Raw file missing: " + rawFile.getAbsolutePath());
                        return;
                    }

                    if (renderWidth == state.targetWidth && renderHeight == state.targetHeight) {
                        if (rawFile.renameTo(finalFile)) {
                            mc.addScheduledTask(() ->
                                    mc.player.sendMessage(new TextComponentString("§aSaved: " + finalFileName))
                            );
                            return;
                        }
                    }

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

                    mc.addScheduledTask(() ->
                            mc.player.sendMessage(new TextComponentString("§aUpscaling Finished: " + finalFileName))
                    );

                } catch (Exception e) {
                    LOGGER.error("CPU Upscale failed", e);
                    mc.addScheduledTask(() ->
                            mc.player.sendMessage(new TextComponentString("§cPost-Processing Failed: " + e.getMessage()))
                    );
                }
            }, "HiResShot-Upscaler").start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}