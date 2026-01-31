package com.elfmcys.yesstevemodel;

import com.elfmcys.yesstevemodel.capability.AuthModelsCapability;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.elfmcys.yesstevemodel.capability.StarModelsCapability;
import com.elfmcys.yesstevemodel.command.RootCommand;
import com.elfmcys.yesstevemodel.config.Config;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
        ServerModelManager.reloadPacks();
        NetworkHandler.init();
        registerCapability();
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new RootCommand());
    }

    private static void registerCapability() {
        CapabilityManager.INSTANCE.register(ModelInfoCapability.class, new ModelInfoCapability.Storage(), ModelInfoCapability::new);
        CapabilityManager.INSTANCE.register(AuthModelsCapability.class, new AuthModelsCapability.Storage(), AuthModelsCapability::new);
        CapabilityManager.INSTANCE.register(StarModelsCapability.class, new StarModelsCapability.Storage(), StarModelsCapability::new);
    }
}
