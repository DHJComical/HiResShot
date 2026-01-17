package com.dhj.hiresshot.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    // 获取打包的缓冲区 (新版关键)
    @Accessor("targets")
    LevelTargetBundle getTargets();

    // 实体描边通常还是独立的
    @Accessor("entityOutlineTarget")
    @Nullable
    RenderTarget getEntityOutlineTarget();

}