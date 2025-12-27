package com.dhj.hiresshot;

import com.dhj.hiresshot.client.KeyHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public class HiResShot {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        KeyHandler.register();
    }

}