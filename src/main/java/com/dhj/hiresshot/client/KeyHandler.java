package com.dhj.hiresshot.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {
    public static final KeyBinding KEY_CAPTURE = new KeyBinding("key.hiresshot.capture", Keyboard.KEY_F9, "key.categories.hiresshot");

    public static void register() {
        ClientRegistry.registerKeyBinding(KEY_CAPTURE);
        MinecraftForge.EVENT_BUS.register(new KeyHandler());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KEY_CAPTURE.isPressed()) {
            HiResShotHandler.startCapture();
        }
    }
}