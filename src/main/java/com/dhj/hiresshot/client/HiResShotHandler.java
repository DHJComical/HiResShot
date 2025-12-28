package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.mixin.WindowAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HiResShotHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("HiResShot");

    private boolean isCapturing = false;
    private int framesToWait = 0;

    private boolean isHidingPlayer = false;

    private int originalFbWidth;
    private int originalFbHeight;
    private boolean originalHideGui;

    private int targetWidth;
    private int targetHeight;

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (KeyInit.KEY_CAPTURE.consumeClick()) {
            startCapture();
        }
    }

    public void startCapture() {
        if (isCapturing) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int scaleFactor = HRSConfig.CLIENT.multiplier.get();
        boolean useCustomRes = HRSConfig.CLIENT.useCustomResolution.get();
        int configWarmup = HRSConfig.CLIENT.warmupFrames.get();
        boolean useRealTime = HRSConfig.CLIENT.realTimeMode.get();

        if (mc.getMainRenderTarget() == null) {
            mc.player.sendSystemMessage(Component.literal("§cError: Framebuffer is null!"));
            return;
        }

        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        int currentWidth = window.getFramebufferWidth();
        int currentHeight = window.getFramebufferHeight();

        int tW, tH;
        String modeInfo;
        if (useCustomRes) {
            tW = HRSConfig.CLIENT.customWidth.get();
            tH = HRSConfig.CLIENT.customHeight.get();
            modeInfo = "Custom: " + tW + "x" + tH;
        } else {
            tW = currentWidth * scaleFactor;
            tH = currentHeight * scaleFactor;
            modeInfo = "Multiplier: x" + scaleFactor + " (" + tW + "x" + tH + ")";
        }

        int maxTexSize = RenderSystem.maxSupportedTextureSize();
        if (tW > maxTexSize || tH > maxTexSize) {
            mc.player.sendSystemMessage(Component.literal("§cError: Size " + tW + "x" + tH + " > GPU Max " + maxTexSize));
            return;
        }

        originalFbWidth = currentWidth;
        originalFbHeight = currentHeight;
        originalHideGui = mc.options.hideGui;
        targetWidth = tW;
        targetHeight = tH;

        try {
            if (HRSConfig.CLIENT.hideGui.get()) mc.options.hideGui = true;
            if (HRSConfig.CLIENT.hidePlayer.get()) isHidingPlayer = true;
            resize(mc, targetWidth, targetHeight);
            if (useRealTime) {
                isCapturing = true;
                framesToWait = Math.max(5, configWarmup);
                mc.player.sendSystemMessage(Component.literal("§eCapturing [" + modeInfo + "]... Please wait."));
            } else {
                mc.player.sendSystemMessage(Component.literal("§eInstant Capture [" + modeInfo + "]..."));
                for (int i = 0; i < configWarmup; i++) {
                    renderFrame(mc);
                }
                doSaveScreenshot(mc);
                restoreState(mc);
            }

        } catch (OutOfMemoryError e) {
            handleOOM(mc, e);
        } catch (Exception e) {
            LOGGER.error("Capture start failed", e);
            restoreState(mc);
            mc.player.sendSystemMessage(Component.literal("§cError: " + e.getMessage()));
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isCapturing) return;
        Minecraft mc = Minecraft.getInstance();
        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        if (window.getFramebufferWidth() != targetWidth || window.getFramebufferHeight() != targetHeight) {
            resize(mc, targetWidth, targetHeight);
        }
        if (framesToWait > 0) {
            framesToWait--;
            return;
        }
        try {
            doSaveScreenshot(mc);
        } catch (OutOfMemoryError e) {
            handleOOM(mc, e);
        } catch (Exception e) {
            LOGGER.error("Save failed", e);
            restoreState(mc);
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("§cError: " + e.getMessage()));
            }
        } finally {
            if (!isCapturing) restoreState(mc);
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (isHidingPlayer && event.getEntity() == Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }

    private void renderFrame(Minecraft mc) {
        mc.getMainRenderTarget().bindWrite(true);
        mc.gameRenderer.render(1.0F, System.nanoTime(), true);
    }

    private void doSaveScreenshot(Minecraft mc) {
        try {
            if (!isCapturing) {
                renderFrame(mc);
            }
            File gameDir = mc.gameDirectory;
            File screenshotsDir = new File(gameDir, "screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs();
            }
            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String customFileName = dateStr + "_hrs.png";
            File screenshotFile = new File(new File(gameDir, "screenshots"), customFileName);
            NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget());
            Util.ioPool().execute(() -> {
                try {
                    image.writeToFile(screenshotFile);
                    LOGGER.info("Saved: " + screenshotFile.getAbsolutePath());
                } catch (Exception e) {
                    LOGGER.error("Failed to write screenshot", e);
                } finally {
                    image.close();
                }
            });
            restoreState(mc);
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal("§aScreenshot Saved: " + customFileName));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void resize(Minecraft mc, int width, int height) {
        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        window.setFramebufferWidth(width);
        window.setFramebufferHeight(height);
        window.setWidth(width);
        window.setHeight(height);
        RenderSystem.viewport(0, 0, width, height);
        if (mc.getMainRenderTarget().width != width || mc.getMainRenderTarget().height != height) {
            mc.getMainRenderTarget().resize(width, height, Minecraft.ON_OSX);
        }
        mc.levelRenderer.resize(width, height);
        mc.gameRenderer.resize(width, height);
    }

    private void restoreState(Minecraft mc) {
        isCapturing = false;
        resize(mc, originalFbWidth, originalFbHeight);
        if (HRSConfig.CLIENT.hideGui.get()) {
            mc.options.hideGui = originalHideGui;
        }
        isHidingPlayer = false;
    }

    private void handleOOM(Minecraft mc, OutOfMemoryError e) {
        LOGGER.error("Out of Memory!", e);
        restoreState(mc);
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("§cError: Out of Memory! Reduce resolution."));
        }
        System.gc();
    }
}