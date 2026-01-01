package com.dhj.hiresshot;

import com.dhj.hiresshot.client.capture.CaptureMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HRSConfig {
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static class Client {
        public final ForgeConfigSpec.IntValue multiplier;
        public final ForgeConfigSpec.BooleanValue hideGui;
        public final ForgeConfigSpec.BooleanValue hidePlayer;
        public final ForgeConfigSpec.IntValue warmupFrames;
        public final ForgeConfigSpec.EnumValue<CaptureMode> captureMode;
        public final ForgeConfigSpec.BooleanValue useCustomResolution;
        public final ForgeConfigSpec.IntValue customWidth;
        public final ForgeConfigSpec.IntValue customHeight;
        public final ForgeConfigSpec.BooleanValue cpuUpscaleLimitGpu;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            multiplier = builder
                    .comment("Resolution Multiplier")
                    .translation("config.hiresshot.multiplier")
                    .defineInRange("multiplier", 4, 2, 64);

            hideGui = builder
                    .comment("Hide GUI")
                    .translation("config.hiresshot.hideGui")
                    .define("hideGui", true);

            hidePlayer = builder
                    .comment("Hide Player")
                    .translation("config.hiresshot.hidePlayer")
                    .define("hidePlayer", true);

            warmupFrames = builder
                    .comment("Warm-up frames")
                    .translation("config.hiresshot.warmupFrames")
                    .defineInRange("warmupFrames", 10, 0, 100);

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
                    .comment("Limit GPU size in CPU Mode")
                    .translation("config.hiresshot.cpuUpscaleLimitGpu")
                    .define("cpuUpscaleLimitGpu", true);

            builder.pop();
        }
    }
}