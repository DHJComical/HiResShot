package com.dhj.hiresshot.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Accessor("transparencyChain")
    @Nullable
    PostChain getTransparencyChain();

    @Accessor("entityEffect")
    @Nullable
    PostChain getEntityEffect();

    @Accessor("entityTarget")
    @Nullable
    RenderTarget getEntityTarget();

    @Accessor("translucentTarget")
    @Nullable
    RenderTarget getTranslucentTarget();

    @Accessor("particlesTarget")
    @Nullable
    RenderTarget getParticlesTarget();

    @Accessor("weatherTarget")
    @Nullable
    RenderTarget getWeatherTarget();

    @Accessor("cloudsTarget")
    @Nullable
    RenderTarget getCloudsTarget();

    @Accessor("itemEntityTarget")
    @Nullable
    RenderTarget getItemEntityTarget();
}