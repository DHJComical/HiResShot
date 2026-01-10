package com.dhj.hiresshot.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static final KeyMapping KEY_CAPTURE = new KeyMapping(
            "key.hiresshot.capture",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.hiresshot"
    );

    public static void register() {
        ClientRegistry.registerKeyBinding(KEY_CAPTURE);
    }
}