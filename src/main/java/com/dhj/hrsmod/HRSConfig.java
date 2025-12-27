package com.dhj.hrsmod;

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
}