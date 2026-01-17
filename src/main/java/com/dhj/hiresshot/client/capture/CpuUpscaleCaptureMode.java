package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.mixin.NativeImageAccessor;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
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
            int hardwareLimit = RenderSystem.getDevice().getMaxTextureSize();
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
                mc.player.displayClientMessage(Component.literal("§eCPU Hybrid Capture..."), false);
            }

            if (renderWidth < state.targetWidth || renderHeight < state.targetHeight) {
                if (mc.player != null) {
                    mc.player.displayClientMessage(Component.literal("§7GPU Base: " + renderWidth + "x" + renderHeight), false);
                    mc.player.displayClientMessage(Component.literal("§bCPU Upscale -> " + state.targetWidth + "x" + state.targetHeight), false);
                }

            } else {
                if (mc.player != null) {
                    mc.player.displayClientMessage(Component.literal("§aNative Render: " + renderWidth + "x" + renderHeight), false);
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
                mc.player.displayClientMessage(Component.literal("§cError: " + e.getMessage()), false);
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

            NativeImage image = grabNativeImage();

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
                                    mc.player.displayClientMessage(Component.literal("§aUpscaling Finished: " + finalFile.getName()), false);
                                }
                            }
                    );

                } catch (Exception e) {
                    LOGGER.error("CPU Upscale failed", e);
                    mc.execute(() -> {
                                if (mc.player != null) {
                                    mc.player.displayClientMessage(Component.literal("§cPost-Processing Failed: " + e.getMessage()), false);
                                }
                            }
                    );
                }
            }, "HiResShot-Upscaler").start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private NativeImage grabNativeImage() {
        int w = renderWidth;
        int h = renderHeight;
        NativeImage image = new NativeImage(w, h, false);
        RenderSystem.outputColorTextureOverride = mc.getMainRenderTarget().getColorTextureView();
        GlStateManager._pixelStore(org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT, 1);
        GlStateManager._readPixels(
                0, 0, w, h,
                org.lwjgl.opengl.GL11.GL_RGBA,
                org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE,
                image.getPointer()
        );

        flipImage(image);

        RenderSystem.outputColorTextureOverride = null;
        return image;
    }

    private void flipImage(NativeImage image) {
        int h = image.getHeight();
        int w = image.getWidth();
        for (int y = 0; y < h / 2; ++y) {
            for (int x = 0; x < w; ++x) {
                int bottomY = h - 1 - y;
                NativeImageAccessor accessor = (NativeImageAccessor) (Object) image;
                int topColor = accessor.invokeGetPixelABGR(x, y);
                int bottomColor = accessor.invokeGetPixelABGR(x, bottomY);
                image.setPixelABGR(x, y, bottomColor);
                image.setPixelABGR(x, bottomY, topColor);
            }
        }
    }
}