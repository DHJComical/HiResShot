package com.dhj.hiresshot.client;

import com.dhj.hiresshot.HRSConfig;
import com.dhj.hiresshot.client.capture.CaptureMode;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class ConfigScreen {

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new TranslatableComponent("config.hiresshot.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(new TranslatableComponent("key.categories.hiresshot"));

        general.addEntry(entryBuilder.startIntSlider(new TranslatableComponent("config.hiresshot.multiplier"), HRSConfig.CLIENT.multiplier.get(), 2, 32)
                .setDefaultValue(4)
                .setTooltip(new TranslatableComponent("config.hiresshot.multiplier.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.multiplier::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.hiresshot.hideGui"), HRSConfig.CLIENT.hideGui.get())
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.hiresshot.hideGui.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.hideGui::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.hiresshot.hidePlayer"), HRSConfig.CLIENT.hidePlayer.get())
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.hiresshot.hidePlayer.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.hidePlayer::set)
                .build());

        // 截图模式
        general.addEntry(entryBuilder.startEnumSelector(new TranslatableComponent("config.hiresshot.captureMode"), CaptureMode.class, HRSConfig.CLIENT.captureMode.get())
                .setDefaultValue(CaptureMode.REAL_TIME)
                .setTooltip(new TranslatableComponent("config.hiresshot.captureMode.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.captureMode::set)
                .build());

        // 实时模式延迟
        general.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.hiresshot.realtimeDelay"), HRSConfig.CLIENT.realtimeDelay.get())
                .setDefaultValue(20)
                .setMin(1).setMax(600)
                .setTooltip(new TranslatableComponent("config.hiresshot.realtimeDelay.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.realtimeDelay::set)
                .build());

        general.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.hiresshot.warmupFrames"), HRSConfig.CLIENT.warmupFrames.get())
                .setDefaultValue(10)
                .setMin(0).setMax(100)
                .setTooltip(new TranslatableComponent("config.hiresshot.warmupFrames.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.warmupFrames::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.hiresshot.cpuUpscaleLimitGpu"), HRSConfig.CLIENT.cpuUpscaleLimitGpu.get())
                .setDefaultValue(true)
                .setTooltip(new TranslatableComponent("config.hiresshot.cpuUpscaleLimitGpu.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.cpuUpscaleLimitGpu::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableComponent("config.hiresshot.useCustomResolution"), HRSConfig.CLIENT.useCustomResolution.get())
                .setDefaultValue(false)
                .setTooltip(new TranslatableComponent("config.hiresshot.useCustomResolution.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.useCustomResolution::set)
                .build());

        general.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.hiresshot.customWidth"), HRSConfig.CLIENT.customWidth.get())
                .setDefaultValue(3840)
                .setMin(100).setMax(64000)
                .setTooltip(new TranslatableComponent("config.hiresshot.customWidth.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.customWidth::set)
                .build());

        general.addEntry(entryBuilder.startIntField(new TranslatableComponent("config.hiresshot.customHeight"), HRSConfig.CLIENT.customHeight.get())
                .setDefaultValue(2160)
                .setMin(100).setMax(64000)
                .setTooltip(new TranslatableComponent("config.hiresshot.customHeight.tooltip"))
                .setSaveConsumer(HRSConfig.CLIENT.customHeight::set)
                .build());

        builder.setSavingRunnable(HRSConfig.CLIENT_SPEC::save);

        return builder.build();
    }
}