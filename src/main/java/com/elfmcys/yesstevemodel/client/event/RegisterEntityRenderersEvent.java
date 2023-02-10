package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = YesSteveModel.MOD_ID)
public class RegisterEntityRenderersEvent {
    private static CustomPlayerRenderer CUSTOM_PLAYER_RENDERER;

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        EntityRendererManager dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        CUSTOM_PLAYER_RENDERER = new CustomPlayerRenderer(dispatcher);
    }

    public static CustomPlayerRenderer getInstance() {
        return CUSTOM_PLAYER_RENDERER;
    }
}
