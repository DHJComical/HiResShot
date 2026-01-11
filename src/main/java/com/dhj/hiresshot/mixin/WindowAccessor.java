package com.dhj.hiresshot.mixin;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Window.class)
public interface WindowAccessor {
    @Accessor("width")
    void setWidth(int width);

    @Accessor("height")
    void setHeight(int height);

    @Accessor("framebufferWidth")
    int getFramebufferWidth();

    @Accessor("framebufferWidth")
    void setFramebufferWidth(int width);

    @Accessor("framebufferHeight")
    int getFramebufferHeight();

    @Accessor("framebufferHeight")
    void setFramebufferHeight(int height);
}