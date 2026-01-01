package com.dhj.hiresshot;

import com.dhj.hiresshot.client.ConfigScreen;
import com.dhj.hiresshot.client.HiResShotHandler;
import com.dhj.hiresshot.client.KeyInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(HiResShot.MODID)
public class HiResShot {
    public static final String MODID = "hiresshot";

    public HiResShot(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, HRSConfig.CLIENT_SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(KeyInit::registerKeyMappings);
            NeoForge.EVENT_BUS.register(new HiResShotHandler());
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (client, parent) ->
                    ConfigScreen.createScreen(parent)
            );
        }
    }
}