package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.mixin.LevelRendererAccessor;
import com.dhj.hiresshot.mixin.WindowAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractCaptureMode {

    protected static final Logger LOGGER = LoggerFactory.getLogger("HiResShot");
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
        if (HRSConfig.CLIENT.hideGui.get()) {
            mc.options.hideGui = true;
        }
    }

    protected void restoreState() {
        active = false;

        resize(state.originalWidth - 1, state.originalHeight);
        resize(state.originalWidth, state.originalHeight);

        RenderSystem.viewport(0, 0, state.originalWidth, state.originalHeight);
        mc.getMainRenderTarget().bindWrite(true);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        tryReloadShaders();

        if (HRSConfig.CLIENT.hideGui.get()) {
            mc.options.hideGui = state.originalHideGui;
        }

        mc.player.sendMessage(new TextComponent("§aScreenshot Saved!"), mc.player.getUUID());
    }

    protected void resize(int width, int height) {
        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        window.setFramebufferWidth(width);
        window.setFramebufferHeight(height);
        window.setWidth(width);
        window.setHeight(height);

        RenderSystem.viewport(0, 0, width, height);

        RenderTarget mainTarget = mc.getMainRenderTarget();
        if (mainTarget.width != width || mainTarget.height != height) {
            mainTarget.resize(width, height, Minecraft.ON_OSX);
        }

        LevelRendererAccessor lvl = (LevelRendererAccessor) mc.levelRenderer;
        if (lvl.getEntityEffect() != null) lvl.getEntityEffect().resize(width, height);
        if (lvl.getEntityTarget() != null) lvl.getEntityTarget().resize(width, height, Minecraft.ON_OSX);
        if (lvl.getTranslucentTarget() != null) lvl.getTranslucentTarget().resize(width, height, Minecraft.ON_OSX);
        if (lvl.getParticlesTarget() != null) lvl.getParticlesTarget().resize(width, height, Minecraft.ON_OSX);
        if (lvl.getWeatherTarget() != null) lvl.getWeatherTarget().resize(width, height, Minecraft.ON_OSX);
        if (lvl.getCloudsTarget() != null) lvl.getCloudsTarget().resize(width, height, Minecraft.ON_OSX);
        if (lvl.getItemEntityTarget() != null) lvl.getItemEntityTarget().resize(width, height, Minecraft.ON_OSX);

        mc.gameRenderer.resize(width, height);
    }

    protected void renderFrame() {
        mc.getMainRenderTarget().bindWrite(true);
        mc.gameRenderer.render(1.0F, System.nanoTime(), true);
    }

    protected void saveScreenshot() {
        try {
            File gameDir = mc.gameDirectory;
            File screenshotsDir = new File(gameDir, "screenshots");
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs();

            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String customFileName = dateStr + "_hrs.png";
            File screenshotFile = new File(screenshotsDir, customFileName);

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

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void handleOOM(OutOfMemoryError e) {
        LOGGER.error("Out of Memory!", e);
        active = false;
        restoreState();
        if (mc.player != null) {
            mc.player.sendMessage(new TextComponent("§cError: Out of Memory! Reduce resolution."), mc.player.getUUID());
        }
        System.gc();
    }

    private void tryReloadShaders() {
        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            Method reloadMethod = irisClass.getMethod("reload");
            reloadMethod.invoke(null);
        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
            LOGGER.warn("Failed to reload shaders: " + e.getMessage());
        }
    }
}