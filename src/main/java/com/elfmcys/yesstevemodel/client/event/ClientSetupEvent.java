package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.input.AnimationRouletteKey;
import com.elfmcys.yesstevemodel.client.input.PlayerModelScreenKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ClientSetupEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(PlayerModelScreenKey.PLAYER_MODEL_KEY);
        ClientRegistry.registerKeyBinding(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        AnimationRegister.registerAnimationState();
        AnimationRegister.registerVariables();
    }
}
