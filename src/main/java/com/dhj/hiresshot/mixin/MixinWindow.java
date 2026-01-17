package com.dhj.hiresshot.mixin;

import com.dhj.hiresshot.client.capture.CaptureState;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Window.class)
public class MixinWindow {

    @Inject(method = "getWidth", at = @At("HEAD"), cancellable = true)
    private void hookGetWidth(CallbackInfoReturnable<Integer> cir) {
        if (CaptureState.IS_CAPTURING) {
            cir.setReturnValue(CaptureState.FAKE_WIDTH);
        }
    }

    @Inject(method = "getHeight", at = @At("HEAD"), cancellable = true)
    private void hookGetHeight(CallbackInfoReturnable<Integer> cir) {
        if (CaptureState.IS_CAPTURING) {
            cir.setReturnValue(CaptureState.FAKE_HEIGHT);
        }
    }

    @Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
    private void hookGetGuiScaledWidth(CallbackInfoReturnable<Integer> cir) {
        if (CaptureState.IS_CAPTURING) {
            cir.setReturnValue(CaptureState.FAKE_WIDTH / 2);
        }
    }
}