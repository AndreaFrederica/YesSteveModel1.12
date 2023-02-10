package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ReplacePlayerRenderEvent {
    @SubscribeEvent
    public static void onRender(RenderPlayerEvent.Pre event) {
        event.setCanceled(true);
        RegisterEntityRenderersEvent.getInstance().render(event.getPlayer(), event.getPlayer().yRot, event.getPartialRenderTick(), event.getMatrixStack(), event.getBuffers(), event.getLight());
    }
}
