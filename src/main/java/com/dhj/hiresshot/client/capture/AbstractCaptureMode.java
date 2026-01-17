package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.mixin.LevelRendererAccessor;
import com.dhj.hiresshot.mixin.NativeImageAccessor;
import com.dhj.hiresshot.mixin.WindowAccessor;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.network.chat.Component;
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
        GlStateManager._viewport(0, 0, state.originalWidth, state.originalHeight);
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        tryReloadShaders();
        if (HRSConfig.CLIENT.hideGui.get()) {
            mc.options.hideGui = state.originalHideGui;
        }
    }

    protected void resize(int width, int height) {
        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        window.setFramebufferWidth(width);
        window.setFramebufferHeight(height);
        window.setWidth(width);
        window.setHeight(height);

        RenderTarget mainTarget = mc.getMainRenderTarget();
        if (mainTarget.width != width || mainTarget.height != height) {
            mainTarget.resize(width, height);
        }

        LevelRendererAccessor lvl = (LevelRendererAccessor) mc.levelRenderer;

        LevelTargetBundle targets = lvl.getTargets();
        if (targets != null) {
            resizeHandle(targets.translucent, width, height);
            resizeHandle(targets.particles, width, height);
            resizeHandle(targets.weather, width, height);
            resizeHandle(targets.clouds, width, height);
            resizeHandle(targets.itemEntity, width, height);
        }

        if (lvl.getEntityOutlineTarget() != null) {
            lvl.getEntityOutlineTarget().resize(width, height);
        }

        mc.gameRenderer.resize(width, height);
    }

    private void resizeHandle(ResourceHandle<RenderTarget> handle, int width, int height) {
        if (handle != null && handle.get() != null) {
            handle.get().resize(width, height);
        }
    }

    protected void renderFrame() {
        RenderTarget target = mc.getMainRenderTarget();

        CaptureState.IS_CAPTURING = true;
        CaptureState.FAKE_WIDTH = state.targetWidth;
        CaptureState.FAKE_HEIGHT = state.targetHeight;

        mc.gameRenderer.resize(state.targetWidth, state.targetHeight);

        RenderSystem.outputColorTextureOverride = target.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = target.getDepthTextureView();

        net.minecraft.client.DeltaTracker dummyTracker = new net.minecraft.client.DeltaTracker() {
            @Override public float getGameTimeDeltaPartialTick(boolean b) { return 1.0F; }
            @Override public float getRealtimeDeltaTicks() { return 1.0F; }
            @Override public float getGameTimeDeltaTicks() { return 1.0F; }
        };

        mc.gameRenderer.render(dummyTracker, true);

        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        CaptureState.IS_CAPTURING = false;

        mc.gameRenderer.resize(state.originalWidth, state.originalHeight);
    }

    protected void saveScreenshot() {
        try {
            RenderTarget target = mc.getMainRenderTarget();
            int w = target.width;
            int h = target.height;

            File screenshotsDir = new File(mc.gameDirectory, "screenshots");
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs();
            String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            File screenshotFile = new File(screenshotsDir, dateStr + "_hrs.png");

            NativeImage image = new NativeImage(w, h, false);

            RenderSystem.outputColorTextureOverride = target.getColorTextureView();
            GlStateManager._pixelStore(org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT, 1);

            GlStateManager._readPixels(
                    0, 0, w, h,
                    org.lwjgl.opengl.GL11.GL_RGBA,
                    org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE,
                    image.getPointer()
            );

            flipImage(image);

            Util.ioPool().execute(() -> {
                try {
                    image.writeToFile(screenshotFile.toPath());
                    LOGGER.info("High-Res Screenshot Saved: " + screenshotFile.getAbsolutePath());
                } catch (Exception e) {
                    LOGGER.error("Failed to write image", e);
                } finally {
                    image.close();
                }
            });

            RenderSystem.outputColorTextureOverride = null;

        } catch (Exception e) {
            LOGGER.error("Capture failed", e);
        }
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

    protected void handleOOM(OutOfMemoryError e) {
        LOGGER.error("Out of Memory!", e);
        active = false;
        restoreState();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§cError: Out of Memory! Reduce resolution."), false);
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