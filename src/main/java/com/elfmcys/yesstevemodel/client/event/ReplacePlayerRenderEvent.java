package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ReplacePlayerRenderEvent {
    @SubscribeEvent
    public static void onRender(RenderPlayerEvent.Pre event) {
        PlayerEntity playerRender = event.getPlayer();
        ClientPlayerEntity playerSelf = Minecraft.getInstance().player;
        if (playerRender.equals(playerSelf) && GeneralConfig.DISABLE_SELF_MODEL.get()) {
            return;
        }
        if (!playerRender.equals(playerSelf) && GeneralConfig.DISABLE_OTHER_MODEL.get()) {
            return;
        }
        event.setCanceled(true);
        RegisterEntityRenderersEvent.getInstance().render(event.getPlayer(), event.getPlayer().yRot, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
    }
}
