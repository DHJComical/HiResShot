package com.dhj.hiresshot;

import com.dhj.hiresshot.client.capture.CaptureMode;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HRSConfig {
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Client {
        public final ModConfigSpec.IntValue multiplier;
        public final ModConfigSpec.BooleanValue hideGui;
        public final ModConfigSpec.BooleanValue hidePlayer;
        public final ModConfigSpec.IntValue warmupFrames;
        public final ModConfigSpec.EnumValue<CaptureMode> captureMode;
        public final ModConfigSpec.BooleanValue useCustomResolution;
        public final ModConfigSpec.IntValue customWidth;
        public final ModConfigSpec.IntValue customHeight;
        public final ModConfigSpec.BooleanValue cpuUpscaleLimitGpu;
        public final ModConfigSpec.IntValue realtimeDelay;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("general");

            multiplier = builder
                    .comment("Resolution Multiplier (Range: 2-64)")
                    .translation("config.hiresshot.multiplier")
                    .defineInRange("multiplier", 4, 2, 64);

            hideGui = builder
                    .comment("Hide GUI during screenshot")
                    .translation("config.hiresshot.hideGui")
                    .define("hideGui", true);

            hidePlayer = builder
                    .comment("Hide player model")
                    .translation("config.hiresshot.hidePlayer")
                    .define("hidePlayer", true);

            warmupFrames = builder
                    .comment("Warm-up frames for Instant/CPU Mode")
                    .translation("config.hiresshot.warmupFrames")
                    .defineInRange("warmupFrames", 10, 0, 100);

            realtimeDelay = builder
                    .comment("Seconds to wait in Real-time Mode before saving (Allows shaders to stabilize)")
                    .translation("config.hiresshot.realtimeDelay")
                    .defineInRange("realtimeDelay", 3, 1, 60);

            captureMode = builder
                    .comment("Capture Mode")
                    .translation("config.hiresshot.captureMode")
                    .defineEnum("captureMode", CaptureMode.REAL_TIME);

            useCustomResolution = builder
                    .comment("Use Custom Resolution")
                    .translation("config.hiresshot.useCustomResolution")
                    .define("useCustomResolution", false);

            customWidth = builder
                    .comment("Custom Width")
                    .translation("config.hiresshot.customWidth")
                    .defineInRange("customWidth", 3840, 100, 64000);

            customHeight = builder
                    .comment("Custom Height")
                    .translation("config.hiresshot.customHeight")
                    .defineInRange("customHeight", 2160, 100, 64000);

            cpuUpscaleLimitGpu = builder
                    .comment("Limit GPU render size in CPU mode")
                    .translation("config.hiresshot.cpuUpscaleLimitGpu")
                    .define("cpuUpscaleLimitGpu", true);

            builder.pop();
        }
    }
}