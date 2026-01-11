package com.dhj.hiresshot;

import com.dhj.hiresshot.client.ConfigScreen;
import com.dhj.hiresshot.client.HiResShotHandler;
import com.dhj.hiresshot.client.KeyInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(HiResShot.MODID)
public class HiResShot {
    public static final String MODID = "hiresshot";

    public HiResShot() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HRSConfig.CLIENT_SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerKeys);
            MinecraftForge.EVENT_BUS.register(new HiResShotHandler());
            ModLoadingContext.get().registerExtensionPoint(
                    ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, screen) -> ConfigScreen.createScreen(screen)
                    )
            );
        }
    }

    private void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyInit.KEY_CAPTURE);
    }
}