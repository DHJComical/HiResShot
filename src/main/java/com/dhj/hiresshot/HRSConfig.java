package com.dhj.hiresshot;

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
        public final ForgeConfigSpec.BooleanValue realTimeMode;
        public final ForgeConfigSpec.BooleanValue useCustomResolution;
        public final ForgeConfigSpec.IntValue customWidth;
        public final ForgeConfigSpec.IntValue customHeight;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            multiplier = builder
                    .comment("Resolution Multiplier (Range: 2-32)")
                    .defineInRange("multiplier", 4, 2, 32);

            hideGui = builder
                    .comment("Hide GUI during screenshot")
                    .define("hideGui", true);

            hidePlayer = builder
                    .comment("Hide player model to prevent self-shadows")
                    .define("hidePlayer", true);

            warmupFrames = builder
                    .comment("Warm-up frames to stabilize shaders")
                    .defineInRange("warmupFrames", 10, 0, 100);

            realTimeMode = builder
                    .comment("True = Real-time (Wait in-game); False = Frozen (Instant)")
                    .define("realTimeMode", false);

            useCustomResolution = builder
                    .comment("Use Custom Resolution instead of Multiplier")
                    .define("useCustomResolution", false);

            customWidth = builder
                    .comment("Custom Width")
                    .defineInRange("customWidth", 3840, 100, 32000);

            customHeight = builder
                    .comment("Custom Height")
                    .defineInRange("customHeight", 2160, 100, 32000);

            builder.pop();
        }
    }
}