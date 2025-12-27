package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderPlayerEvent;
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

    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    private static boolean isCapturing = false;
    private static int framesToWait = 0;
    private static boolean registered = false;

    private static boolean isHidingPlayer = false;

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

        int configWarmup = HRSConfig.warmupFrames;
        boolean useRealTime = HRSConfig.realTimeMode;
        boolean useCustomRes = HRSConfig.useCustomResolution;

        if (!OpenGlHelper.isFramebufferEnabled()) {
            mc.player.sendMessage(new TextComponentString("§cError: Framebuffer disabled!"));
            return;
        }

        int tW, tH;
        String modeInfo;
        if (useCustomRes) {
            tW = HRSConfig.customWidth;
            tH = HRSConfig.customHeight;
            modeInfo = "Custom: " + tW + "x" + tH;
        } else {
            int scaleFactor = HRSConfig.multiplier;
            tW = mc.displayWidth * scaleFactor;
            tH = mc.displayHeight * scaleFactor;
            modeInfo = "Multiplier: x" + scaleFactor + " (" + tW + "x" + tH + ")";
        }

        int maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        if (tW > maxTexSize || tH > maxTexSize) {
            mc.player.sendMessage(new TextComponentString("§cError: Size " + tW + "x" + tH + " > GPU Max " + maxTexSize));
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

            if (HRSConfig.hidePlayer) {
                isHidingPlayer = true;
            }

            applyResolution(mc, targetWidth, targetHeight);

            if (useRealTime) {
                isCapturing = true;
                framesToWait = Math.max(5, configWarmup);
                mc.player.sendMessage(new TextComponentString("§eCapturing [" + modeInfo + "]... Please wait."));
            } else {
                mc.player.sendMessage(new TextComponentString("§eInstant Capture [" + modeInfo + "]..."));
                for (int i = 0; i < configWarmup; i++) {
                    mc.getFramebuffer().bindFramebuffer(true);
                    mc.entityRenderer.updateCameraAndRender(1.0f, System.nanoTime() + (long)i * 1000000);
                }
                doSaveScreenshot(mc);
                restoreState(mc);
            }

        } catch (OutOfMemoryError e) {
            handleOOM(mc, e);
        } catch (Exception e) {
            LOGGER.error("Start failed", e);
            restoreState(mc);
            mc.player.sendMessage(new TextComponentString("§cError: " + e.getMessage()));
        }
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (isHidingPlayer) {
            if (event.getEntityPlayer().equals(Minecraft.getMinecraft().player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isCapturing) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.displayWidth != targetWidth || mc.displayHeight != targetHeight) {
            applyResolution(mc, targetWidth, targetHeight);
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
            mc.player.sendMessage(new TextComponentString("§cError: " + e.getMessage()));
        } finally {
            if (mc.displayWidth == targetWidth) {
                restoreState(mc);
                isCapturing = false;
            }
        }
    }

    private static void handleOOM(Minecraft mc, OutOfMemoryError e) {
        LOGGER.error("Out of Memory!", e);
        restoreState(mc);
        isCapturing = false;
        mc.player.sendMessage(new TextComponentString("§cError: Out of Memory! Reduce resolution."));
        System.gc();
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void applyResolution(Minecraft mc, int width, int height) {
        mc.displayWidth = width;
        mc.displayHeight = height;
        if (mc.getFramebuffer().framebufferWidth != width || mc.getFramebuffer().framebufferHeight != height) {
            if (mc.getFramebuffer() != null) {
                fbResize(mc.getFramebuffer(), width, height);
            }
            mc.entityRenderer.onResourceManagerReload(mc.getResourceManager());
        }
    }

    private static void fbResize(Framebuffer fb, int width, int height) {
        fb.createBindFramebuffer(width, height);
    }

    private static void restoreState(Minecraft mc) {
        applyResolution(mc, originalWidth, originalHeight);
        if (HRSConfig.hideGUI) {
            mc.gameSettings.hideGUI = originalHideGUI;
        }
        isHidingPlayer = false;
        mc.player.sendMessage(new TextComponentString("§aScreenshot Saved!"));
    }
}