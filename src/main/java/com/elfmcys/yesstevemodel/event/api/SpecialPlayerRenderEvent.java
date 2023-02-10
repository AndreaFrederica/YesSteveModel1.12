package com.elfmcys.yesstevemodel.event.api;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class SpecialPlayerRenderEvent extends Event {
    private final PlayerEntity player;
    private final CustomPlayerEntity customPlayer;
    private final ResourceLocation modelId;

    public SpecialPlayerRenderEvent(PlayerEntity player, CustomPlayerEntity customPlayer, ResourceLocation modelId) {
        this.player = player;
        this.customPlayer = customPlayer;
        this.modelId = modelId;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public CustomPlayerEntity getCustomPlayer() {
        return customPlayer;
    }

    public ResourceLocation getModelId() {
        return modelId;
    }
}
