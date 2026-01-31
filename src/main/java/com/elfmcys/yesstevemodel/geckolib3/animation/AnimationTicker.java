package com.elfmcys.yesstevemodel.geckolib3.animation;

import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AnimationTicker {
    private final AnimationData data;

    public AnimationTicker(AnimationData data) {
        this.data = data;
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().isGamePaused() && !data.shouldPlayWhilePaused) {
            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            data.tick++;
        }
    }
}
