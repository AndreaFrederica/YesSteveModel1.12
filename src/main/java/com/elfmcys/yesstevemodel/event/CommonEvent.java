package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.AuthModelsCapability;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.elfmcys.yesstevemodel.capability.StarModelsCapability;
import com.elfmcys.yesstevemodel.command.argument.AnimationArgument;
import com.elfmcys.yesstevemodel.command.argument.ModelsArgument;
import com.elfmcys.yesstevemodel.command.argument.TexturesArgument;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = YesSteveModel.MOD_ID)
public final class CommonEvent {
    @SubscribeEvent
    public static void onSetupEvent(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::init);
        event.enqueueWork(CommonEvent::registerCapability);
        event.enqueueWork(() -> ArgumentTypes.register("yes_steve_model:models", ModelsArgument.class, new ArgumentSerializer<>(ModelsArgument::ids)));
        event.enqueueWork(() -> ArgumentTypes.register("yes_steve_model:animations", AnimationArgument.class, new ArgumentSerializer<>(AnimationArgument::animations)));
        event.enqueueWork(() -> ArgumentTypes.register("yes_steve_model:textures", TexturesArgument.class, new ArgumentSerializer<>(TexturesArgument::ids)));
    }

    private static void registerCapability() {
        CapabilityManager.INSTANCE.register(ModelInfoCapability.class, new ModelInfoCapability.Storage(), ModelInfoCapability::new);
        CapabilityManager.INSTANCE.register(AuthModelsCapability.class, new AuthModelsCapability.Storage(), AuthModelsCapability::new);
        CapabilityManager.INSTANCE.register(StarModelsCapability.class, new StarModelsCapability.Storage(), StarModelsCapability::new);
    }
}
