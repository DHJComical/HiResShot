package com.dhj.hiresshot;

import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
@Config.LangKey("config.hiresshot.title")
public class HRSConfig {

    @Config.LangKey("config.hiresshot.multiplier")
    @Config.Comment("Resolution Multiplier (Range: 2-32)")
    @Config.RangeInt(min = 2, max = 32)
    public static int multiplier = 4;

    @Config.LangKey("config.hiresshot.hide_gui")
    @Config.Comment("Hide GUI during screenshot")
    public static boolean hideGUI = true;

    @Config.LangKey("config.hiresshot.warmup_frames")
    @Config.Comment("Warm-up frames to render before saving. Helps shaders stabilize exposure.")
    @Config.RangeInt(min = 0, max = 100)
    public static int warmupFrames = 10;

    @Config.LangKey("config.hiresshot.realtime_mode")
    @Config.Comment("True = Real-time (Wait in-game, Best for Shaders); False = Frozen (Instant, Faster)")
    public static boolean realTimeMode = false;

    @Config.LangKey("config.hiresshot.use_custom_res")
    @Config.Comment("If True, uses the Custom Width/Height defined below instead of the Multiplier.")
    public static boolean useCustomResolution = false;

    @Config.LangKey("config.hiresshot.custom_width")
    @Config.Comment("Custom Width in pixels (e.g., 3840 for 4K)")
    @Config.RangeInt(min = 100, max = 32000)
    public static int customWidth = 3840;

    @Config.LangKey("config.hiresshot.custom_height")
    @Config.Comment("Custom Height in pixels (e.g., 2160 for 4K)")
    @Config.RangeInt(min = 100, max = 32000)
    public static int customHeight = 2160;

    @Config.LangKey("config.hiresshot.hide_player")
    @Config.Comment("Hide player model to prevent self-shadows")
    public static boolean hidePlayer = true;
}