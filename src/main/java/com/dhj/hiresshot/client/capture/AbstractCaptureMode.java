package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.HRSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractCaptureMode {

    protected static final Logger LOGGER = LogManager.getLogger("HiResShot");
    protected final Minecraft mc;
    protected final CaptureState state;
    protected boolean active = true;

    public AbstractCaptureMode(Minecraft mc, CaptureState state) {
        this.mc = mc;
        this.state = state;
    }

    public abstract void start(int frames);
    public abstract void onRenderTick();

    public boolean isActive() { return active; }
    public boolean shouldHidePlayer() { return active && state.hidePlayer; }

    protected void applyResolution() {
        resize(state.targetWidth, state.targetHeight);
        if (HRSConfig.hideGUI) {
            mc.gameSettings.hideGUI = true;
        }
    }

    protected void restoreState() {
        active = false;

        resize(state.originalWidth - 1, state.originalHeight);
        resize(state.originalWidth, state.originalHeight);

        GlStateManager.viewport(0, 0, state.originalWidth, state.originalHeight);

        if (OpenGlHelper.isFramebufferEnabled()) {
            mc.getFramebuffer().bindFramebuffer(true);
        }

        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();

        mc.entityRenderer.onResourceManagerReload(mc.getResourceManager());

        if (HRSConfig.hideGUI) {
            mc.gameSettings.hideGUI = state.originalHideGui;
        }

        mc.player.sendMessage(new TextComponentString("§aScreenshot Saved!"));
    }

    protected void resize(int width, int height) {
        mc.displayWidth = width;
        mc.displayHeight = height;

        if (mc.getFramebuffer() != null) {
            if (mc.getFramebuffer().framebufferWidth != width || mc.getFramebuffer().framebufferHeight != height) {
                mc.getFramebuffer().createBindFramebuffer(width, height);
            }
        }

        mc.entityRenderer.onResourceManagerReload(mc.getResourceManager());
    }

    protected void renderFrame() {
        if (mc.getFramebuffer() != null) {
            mc.getFramebuffer().bindFramebuffer(true);
        }
        mc.entityRenderer.updateCameraAndRender(1.0F, System.nanoTime());
    }

    protected void saveScreenshot() {
        try {
            File gameDir = mc.gameDir;
            File screenshotsDir = new File(gameDir, "screenshots");
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String customFileName = dateStr + "_hrs.png";

            ITextComponent msg = ScreenShotHelper.saveScreenshot(gameDir, customFileName, state.targetWidth, state.targetHeight, mc.getFramebuffer());
            LOGGER.info("Saved: {}", customFileName);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleOOM(OutOfMemoryError e) {
        LOGGER.error("Out of Memory!", e);
        active = false;
        restoreState();
        mc.player.sendMessage(new TextComponentString("§cError: Out of Memory! Reduce resolution."));
        System.gc();
    }
}