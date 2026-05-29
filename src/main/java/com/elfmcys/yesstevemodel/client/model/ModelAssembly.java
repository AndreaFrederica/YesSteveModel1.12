package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;

public class ModelAssembly {
    private final PlayerModelBundle animationBundle;
    private final Map<ResourceLocation, ProjectileModelBundle> projectileModels;
    private final Map<ResourceLocation, VehicleModelBundle> vehicleModels;
    private final ModelResourceBundle resourceBundle;
    private final ServerModelInfo modelData;
    private final ModelDisplayAssets displayAssets;
    private final List<AbstractTexture> textures;

    public ModelAssembly(PlayerModelBundle animationBundle, Map<ResourceLocation, ProjectileModelBundle> projectileModels, Map<ResourceLocation, VehicleModelBundle> vehicleModels, ModelResourceBundle resourceBundle, ServerModelInfo modelData, ModelDisplayAssets displayAssets, List<AbstractTexture> textures) {
        this.animationBundle = animationBundle;
        this.projectileModels = projectileModels;
        this.vehicleModels = vehicleModels;
        this.resourceBundle = resourceBundle;
        this.modelData = modelData;
        this.displayAssets = displayAssets;
        this.textures = textures;
    }

    public PlayerModelBundle getAnimationBundle() {
        return this.animationBundle;
    }

    public Map<ResourceLocation, ProjectileModelBundle> getProjectileModels() {
        return this.projectileModels;
    }

    public Map<ResourceLocation, VehicleModelBundle> getVehicleModels() {
        return this.vehicleModels;
    }

    public ModelResourceBundle getResourceBundle() {
        return this.resourceBundle;
    }

    public ServerModelInfo getModelData() {
        return this.modelData;
    }

    public ModelDisplayAssets getDisplayAssets() {
        return this.displayAssets;
    }

    public List<AbstractTexture> getTextures() {
        return this.textures;
    }

    public String getDisplayName(String fallback) {
        Metadata metadata = this.modelData.getExtraInfo();
        if (metadata != null && metadata.getName() != null && !metadata.getName().isEmpty()) {
            return metadata.getName();
        }
        return fallback;
    }
}
