package com.dhj.hiresshot.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @Shadow public abstract Minecraft getMinecraft();

    @Inject(method = "getProjectionMatrix", at = @At("RETURN"))
    private void onGetProjectionMatrix(float fov, CallbackInfoReturnable<Matrix4f> cir) {
    }

    @Inject(method = "getProjectionMatrix", at = @At("RETURN"), cancellable = true)
    private void fixZPlanes(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (!com.dhj.hiresshot.client.HiResShotHandler.isCapturing()) return;
        Minecraft mc = this.getMinecraft();
        float aspectRatio = (float) mc.getWindow().getWidth() / (float) mc.getWindow().getHeight();
        float zNear = 0.2F;
        float zFar = 10000.0F;
        Matrix4f matrix = new Matrix4f();
        matrix.setPerspective((float) Math.toRadians(fov), aspectRatio, zNear, zFar);
        cir.setReturnValue(matrix);
    }
}