package com.dhj.hiresshot.client.capture;

import com.dhj.hiresshot.mixin.WindowAccessor;
import net.minecraft.client.Minecraft;

public class CaptureState {
    public int originalWidth;
    public int originalHeight;
    public boolean originalHideGui;

    public int targetWidth;
    public int targetHeight;

    public boolean hidePlayer;

    public CaptureState(Minecraft mc, int tW, int tH, boolean hidePlayerConfig) {
        WindowAccessor window = (WindowAccessor) (Object) mc.getWindow();
        this.originalWidth = window.getFramebufferWidth();
        this.originalHeight = window.getFramebufferHeight();
        this.originalHideGui = mc.options.hideGui;
        this.targetWidth = tW;
        this.targetHeight = tH;
        this.hidePlayer = hidePlayerConfig;
    }
}