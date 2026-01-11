package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.client.capture.CaptureMode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen {

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.hiresshot.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("key.categories.hiresshot"));

        general.addEntry(entryBuilder.startIntSlider(Component.translatable("config.hiresshot.multiplier"), HRSConfig.CLIENT.multiplier.get(), 2, 32)
                .setDefaultValue(4)
                .setTooltip(Component.translatable("config.hiresshot.multiplier.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.multiplier::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.hiresshot.hideGui"), HRSConfig.CLIENT.hideGui.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.hiresshot.hideGui.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.hideGui::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.hiresshot.hidePlayer"), HRSConfig.CLIENT.hidePlayer.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.hiresshot.hidePlayer.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.hidePlayer::set)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Component.translatable("config.hiresshot.captureMode"), CaptureMode.class, HRSConfig.CLIENT.captureMode.get())
                .setDefaultValue(CaptureMode.REAL_TIME)
                .setTooltip(Component.translatable("config.hiresshot.captureMode.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.captureMode::set)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.hiresshot.realtimeDelay"), HRSConfig.CLIENT.realtimeDelay.get())
                .setDefaultValue(20)
                .setMin(1).setMax(600)
                .setTooltip(Component.translatable("config.hiresshot.realtimeDelay.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.realtimeDelay::set)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.hiresshot.warmupFrames"), HRSConfig.CLIENT.warmupFrames.get())
                .setDefaultValue(10)
                .setMin(0).setMax(100)
                .setTooltip(Component.translatable("config.hiresshot.warmupFrames.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.warmupFrames::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.hiresshot.cpuUpscaleLimitGpu"), HRSConfig.CLIENT.cpuUpscaleLimitGpu.get())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.hiresshot.cpuUpscaleLimitGpu.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.cpuUpscaleLimitGpu::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.hiresshot.useCustomResolution"), HRSConfig.CLIENT.useCustomResolution.get())
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.hiresshot.useCustomResolution.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.useCustomResolution::set)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.hiresshot.customWidth"), HRSConfig.CLIENT.customWidth.get())
                .setDefaultValue(3840)
                .setMin(100).setMax(64000)
                .setTooltip(Component.translatable("config.hiresshot.customWidth.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.customWidth::set)
                .build());

        general.addEntry(entryBuilder.startIntField(Component.translatable("config.hiresshot.customHeight"), HRSConfig.CLIENT.customHeight.get())
                .setDefaultValue(2160)
                .setMin(100).setMax(64000)
                .setTooltip(Component.translatable("config.hiresshot.customHeight.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.customHeight::set)
                .build());

        builder.setSavingRunnable(HRSConfig.CLIENT_SPEC::save);

        return builder.build();
    }
}