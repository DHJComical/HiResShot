package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.client.capture.AbstractCaptureMode;
import com.dhj.hiresshot.client.capture.CaptureState;
import com.dhj.hiresshot.client.capture.InstantCaptureMode;
import com.dhj.hiresshot.client.capture.RealTimeCaptureMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class HiResShotHandler {

    private AbstractCaptureMode currentMode = null;
    private static boolean registered = false;

    public static void register() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(new HiResShotHandler());
            registered = true;
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyHandler.KEY_CAPTURE.isPressed()) {
            startCapture();
        }
    }

    public void startCapture() {
        if (currentMode != null && currentMode.isActive()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (!OpenGlHelper.isFramebufferEnabled()) {
            mc.player.sendMessage(new TextComponentString("§cError: Framebuffer disabled!"));
            return;
        }

        int scaleFactor = HRSConfig.multiplier;
        boolean useCustomRes = HRSConfig.useCustomResolution;
        int configWarmup = HRSConfig.warmupFrames;
        boolean useRealTime = HRSConfig.realTimeMode;
        boolean hidePlayer = HRSConfig.hidePlayer;

        int currentWidth = mc.displayWidth;
        int currentHeight = mc.displayHeight;

        int tW, tH;
        if (useCustomRes) {
            tW = HRSConfig.customWidth;
            tH = HRSConfig.customHeight;
        } else {
            tW = currentWidth * scaleFactor;
            tH = currentHeight * scaleFactor;
        }

        int maxTexSize = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
        if (tW > maxTexSize || tH > maxTexSize) {
            mc.player.sendMessage(new TextComponentString("§cError: Size " + tW + "x" + tH + " > GPU Max " + maxTexSize));
            return;
        }

        CaptureState state = new CaptureState(mc, tW, tH, hidePlayer);

        if (useRealTime) {
            currentMode = new RealTimeCaptureMode(mc, state);
        } else {
            currentMode = new InstantCaptureMode(mc, state);
        }

        currentMode.start(configWarmup);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END && currentMode != null) {
            if (currentMode.isActive()) {
                currentMode.onRenderTick();
            } else {
                currentMode = null;
            }
        }
    }

    @SubscribeEvent
    public void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (currentMode != null && currentMode.shouldHidePlayer()) {
            if (event.getEntityPlayer().equals(Minecraft.getMinecraft().player)) {
                event.setCanceled(true);
            }
        }
    }
}