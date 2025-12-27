package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HiResShotHandler {

    private static final Logger LOGGER = LogManager.getLogger("HiResShot");

    private static boolean isCapturing = false;
    private static int framesToWait = 0;
    private static boolean registered = false;

    private static int originalWidth;
    private static int originalHeight;
    private static boolean originalHideGUI;
    private static int targetWidth;
    private static int targetHeight;

    public static void startCapture() {
        if (isCapturing) return;

        if (!registered) {
            MinecraftForge.EVENT_BUS.register(new HiResShotHandler());
            registered = true;
        }

        Minecraft mc = Minecraft.getMinecraft();

        int scaleFactor = HRSConfig.multiplier;
        int configWarmup = HRSConfig.warmupFrames;
        boolean useRealTime = HRSConfig.realTimeMode;

        if (!OpenGlHelper.isFramebufferEnabled()) {
            mc.player.sendMessage(new TextComponentString("§cError: Framebuffer disabled!"));
            return;
        }

        int maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        int tW = mc.displayWidth * scaleFactor;
        int tH = mc.displayHeight * scaleFactor;
        if (tW > maxTexSize || tH > maxTexSize) {
            mc.player.sendMessage(new TextComponentString("§cError: Too big! Max GPU size: " + maxTexSize));
            return;
        }

        originalWidth = mc.displayWidth;
        originalHeight = mc.displayHeight;
        originalHideGUI = mc.gameSettings.hideGUI;
        targetWidth = tW;
        targetHeight = tH;

        try {
            if (HRSConfig.hideGUI) {
                mc.gameSettings.hideGUI = true;
            }
            mc.displayWidth = targetWidth;
            mc.displayHeight = targetHeight;
            resizeFramebuffer(mc, targetWidth, targetHeight);
            if (useRealTime) {
                isCapturing = true;
                framesToWait = Math.max(5, configWarmup);
                mc.player.sendMessage(new TextComponentString("§eStabilizing Shaders (" + framesToWait + " frames)..."));
            } else {
                mc.player.sendMessage(new TextComponentString("§eInstant Capture (x" + scaleFactor + ")..."));
                for (int i = 0; i < configWarmup; i++) {
                    mc.getFramebuffer().bindFramebuffer(true);
                    mc.entityRenderer.updateCameraAndRender(1.0f, System.nanoTime() + (long) i * 1000000);
                }
                doSaveScreenshot(mc);
                restoreState(mc);
            }
        } catch (Exception e) {
            LOGGER.error("Capture start failed", e);
            restoreState(mc);
            mc.player.sendMessage(new TextComponentString("§cError: " + e.getMessage()));
        }
    }

    private static void doSaveScreenshot(Minecraft mc) {
        try {
            mc.getFramebuffer().bindFramebuffer(true);
            if (!isCapturing) {
                mc.entityRenderer.updateCameraAndRender(1.0f, System.nanoTime());
            }
            File gameDir = mc.gameDir;
            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String customFileName = dateStr + "_hrs.png";
            ITextComponent msg = ScreenShotHelper.saveScreenshot(gameDir, customFileName, targetWidth, targetHeight, mc.getFramebuffer());
            LOGGER.info("Saved: " + customFileName);
            mc.player.sendMessage(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void restoreState(Minecraft mc) {
        mc.displayWidth = originalWidth;
        mc.displayHeight = originalHeight;
        resizeFramebuffer(mc, originalWidth, originalHeight);
        if (HRSConfig.hideGUI) {
            mc.gameSettings.hideGUI = originalHideGUI;
        }
    }

    private static void resizeFramebuffer(Minecraft mc, int width, int height) {
        Framebuffer fb = mc.getFramebuffer();
        if (fb != null) {
            fb.createBindFramebuffer(width, height);
        }
        mc.entityRenderer.onResourceManagerReload(mc.getResourceManager());
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isCapturing) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (framesToWait > 0) {
            framesToWait--;
            return;
        }
        try {
            doSaveScreenshot(mc);
        } catch (Exception e) {
            LOGGER.error("Capture save failed", e);
            mc.player.sendMessage(new TextComponentString("§cError: " + e.getMessage()));
        } finally {
            restoreState(mc);
            isCapturing = false;
        }
    }
}