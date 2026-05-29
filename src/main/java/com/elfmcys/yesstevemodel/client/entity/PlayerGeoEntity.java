package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class PlayerGeoEntity extends GeoEntity<EntityPlayer> {

    public PlayerGeoEntity(@Nullable EntityPlayer entity) {
        super(entity, 60);
    }

    @Nullable
    public ConditionArmor getArmModelProcessor() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getModelLocation() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAnimationFileLocation() {
        return null;
    }
}
