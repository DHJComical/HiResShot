package com.dhj.hiresshot;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
@Config.LangKey("config.hrsmod.title")
public class HRSConfig {

    @Config.LangKey("config.hrsmod.multiplier")
    @Config.Comment("Resolution Multiplier (Range: 2-32)")
    @Config.RangeInt(min = 2, max = 32)
    public static int multiplier = 4;

    @Config.LangKey("config.hrsmod.hide_gui")
    @Config.Comment("Hide GUI during screenshot")
    public static boolean hideGUI = true;

    @Config.LangKey("config.hrsmod.warmup_frames")
    @Config.Comment("Warm-up frames to render before saving. Helps shaders stabilize exposure.")
    @Config.RangeInt(min = 0, max = 100)
    public static int warmupFrames = 10;

    @Config.LangKey("config.hrsmod.realtime_mode")
    @Config.Comment("True = Real-time (Wait in-game, Best for Shaders); False = Frozen (Instant, Faster)")
    public static boolean realTimeMode = true;
}