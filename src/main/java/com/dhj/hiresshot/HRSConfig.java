package com.dhj.hiresshot;

import com.dhj.hiresshot.client.capture.CaptureMode;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID)
@Config.LangKey("config.hiresshot.title")
public class HRSConfig {

    @Config.LangKey("config.hiresshot.multiplier")
    @Config.Comment("Resolution Multiplier (Range: 2-64)")
    @Config.RangeInt(min = 2, max = 64)
    public static int multiplier = 4;

    @Config.LangKey("config.hiresshot.hide_gui")
    @Config.Comment("Hide GUI during screenshot")
    public static boolean hideGUI = true;

    @Config.LangKey("config.hiresshot.hide_player")
    @Config.Comment("Hide player model to prevent self-shadows")
    public static boolean hidePlayer = true;

    @Config.LangKey("config.hiresshot.warmup_frames")
    @Config.Comment("Warm-up frames. Used for Instant Mode and CPU Mode.")
    @Config.RangeInt(min = 0, max = 100)
    public static int warmupFrames = 10;

    @Config.LangKey("config.hiresshot.realtime_delay")
    @Config.Comment("Frames to wait in Real-time Mode (Allows shaders to stabilize). Recommended: 20-60.")
    @Config.RangeInt(min = 1, max = 600)
    public static int realtimeDelay = 20;

    @Config.LangKey("config.hiresshot.capture_mode")
    @Config.Comment({
            "Select Capture Mode:",
            "REAL_TIME: Waits in-game, best for Shaders.",
            "INSTANT: Freezes game, faster.",
            "CPU_UPSCALE: Renders max GPU size then upscales with CPU."
    })
    public static CaptureMode captureMode = CaptureMode.REAL_TIME;

    @Config.LangKey("config.hiresshot.use_custom_res")
    @Config.Comment("Use Custom Resolution instead of Multiplier")
    public static boolean useCustomResolution = false;

    @Config.LangKey("config.hiresshot.custom_width")
    @Config.Comment("Custom Width")
    @Config.RangeInt(min = 100, max = 64000)
    public static int customWidth = 3840;

    @Config.LangKey("config.hiresshot.custom_height")
    @Config.Comment("Custom Height")
    @Config.RangeInt(min = 100, max = 64000)
    public static int customHeight = 2160;

    @Config.LangKey("config.hiresshot.cpu_upscale_limit_gpu")
    @Config.Comment("Limit GPU render size to 16K in CPU mode to prevent crashes.")
    public static boolean cpuUpscaleLimitGpu = true;
}