package com.elfmcys.yesstevemodel.client.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class GeckoVehicleEntity extends GeoEntity<Entity> {

    public GeckoVehicleEntity(@Nullable Entity entity, int fps) {
        super(entity, fps);
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
