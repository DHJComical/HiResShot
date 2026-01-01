package com.dhj.hiresshot.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static final KeyMapping KEY_CAPTURE = new KeyMapping(
            "key.hiresshot.capture",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.hiresshot"
    );

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_CAPTURE);
    }
}