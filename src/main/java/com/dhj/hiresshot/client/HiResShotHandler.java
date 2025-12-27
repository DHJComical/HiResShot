package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.Tags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HiResShotHandler {

    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    public static void startCapture() {
        Minecraft mc = Minecraft.getMinecraft();

        int scaleFactor = HRSConfig.multiplier;
        boolean shouldHideGUI = HRSConfig.hideGUI;
        int warmUpFrames = HRSConfig.warmupFrames;

        if (!OpenGlHelper.isFramebufferEnabled()) {
            mc.player.sendMessage(new TextComponentString("Error: Framebuffer disabled!"));
            LOGGER.warn("Framebuffer is disabled, cannot capture screenshot.");
            return;
        }

        mc.player.sendMessage(new TextComponentString("§eCapturing Hi-Res Image (x" + scaleFactor + ")..."));

        int originalWidth = mc.displayWidth;
        int originalHeight = mc.displayHeight;
        int targetWidth = originalWidth * scaleFactor;
        int targetHeight = originalHeight * scaleFactor;

        int maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        if (targetWidth > maxTexSize || targetHeight > maxTexSize) {
            mc.player.sendMessage(new TextComponentString("§cError: Size " + targetWidth + "x" + targetHeight +
                    " exceeds GPU limit (" + maxTexSize + ")"));
            return;
        }

        boolean originalGuiState = mc.gameSettings.hideGUI;

        try {
            if (shouldHideGUI) {
                mc.gameSettings.hideGUI = true;
            }

            mc.displayWidth = targetWidth;
            mc.displayHeight = targetHeight;
            resizeFramebuffer(mc, targetWidth, targetHeight);

            for (int i = 0; i < warmUpFrames; i++) {
                mc.getFramebuffer().bindFramebuffer(true);
                mc.entityRenderer.updateCameraAndRender(1.0f, System.nanoTime() + (long)i * 1000000);
            }

            mc.getFramebuffer().bindFramebuffer(true);
            mc.entityRenderer.updateCameraAndRender(1.0f, System.nanoTime() + (long)warmUpFrames * 1000000);

            File gameDir = mc.gameDir;
            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String customFileName = dateStr + "_hrs.png";

            ITextComponent msg = ScreenShotHelper.saveScreenshot(gameDir, customFileName, targetWidth, targetHeight, mc.getFramebuffer());
            mc.player.sendMessage(msg);

        } catch (OutOfMemoryError e) {
            LOGGER.error("Out of Memory while capturing screenshot!", e);
            if (shouldHideGUI) mc.gameSettings.hideGUI = originalGuiState;
            mc.player.sendMessage(new TextComponentString("§cError: Out of Memory! Try a lower multiplier in Config."));
            System.gc();
        } catch (Exception e) {
            LOGGER.error("Failed to capture high-resolution screenshot", e);
            mc.player.sendMessage(new TextComponentString("§cError: " + e.getMessage()));
        } finally {
            mc.displayWidth = originalWidth;
            mc.displayHeight = originalHeight;
            resizeFramebuffer(mc, originalWidth, originalHeight);

            if (shouldHideGUI) {
                mc.gameSettings.hideGUI = originalGuiState;
            }
        }
    }

    private static void resizeFramebuffer(Minecraft mc, int width, int height) {
        Framebuffer fb = mc.getFramebuffer();
        if (fb != null) {
            fb.createBindFramebuffer(width, height);
        }
        mc.entityRenderer.onResourceManagerReload(mc.getResourceManager());
    }
}