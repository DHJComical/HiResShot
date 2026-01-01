package com.dhj.hiresshot.client.capture;

import net.minecraft.client.Minecraft;

public class CaptureState {

    public int originalWidth;
    public int originalHeight;
    public boolean originalHideGui;

    public int targetWidth;
    public int targetHeight;

    public boolean hidePlayer;

    public CaptureState(Minecraft mc, int tW, int tH, boolean hidePlayerConfig) {
        this.originalWidth = mc.displayWidth;
        this.originalHeight = mc.displayHeight;
        this.originalHideGui = mc.gameSettings.hideGUI;

        this.targetWidth = tW;
        this.targetHeight = tH;
        this.hidePlayer = hidePlayerConfig;
    }
}