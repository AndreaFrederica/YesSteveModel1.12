package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.client.model.MainModelData;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.file.ModelExtraResourcesFile;
import com.elfmcys.yesstevemodel.geckolib3.file.ProjectileModelFiles;
import com.elfmcys.yesstevemodel.geckolib3.file.VehicleModelFiles;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;

import javax.annotation.Nonnull;
import java.util.Map;

public final class ClientModelInfo {
    private final MainModelData mainModelData;
    private final ProjectileModelFiles[] projectileModelFiles;
    private final VehicleModelFiles[] vehicleModelFiles;
    private final ModelExtraResourcesFile extraResources;
    @Nonnull
    private final ServerModelInfo info;
    private final Map<String, OuterFileTexture> avatarTextures;
    private final Map<String, OuterFileTexture> guiTextures;

    public ClientModelInfo(MainModelData mainModelData, ProjectileModelFiles[] projectileModelFiles, VehicleModelFiles[] vehicleModelFiles, ModelExtraResourcesFile extraResources, @Nonnull ServerModelInfo info, Map<String, OuterFileTexture> avatarTextures, Map<String, OuterFileTexture> guiTextures) {
        this.mainModelData = mainModelData;
        this.projectileModelFiles = projectileModelFiles;
        this.vehicleModelFiles = vehicleModelFiles;
        this.extraResources = extraResources;
        this.info = info;
        this.avatarTextures = avatarTextures;
        this.guiTextures = guiTextures;
    }

    public MainModelData getMainModelData() {
        return this.mainModelData;
    }

    public ProjectileModelFiles[] getExtraItemModels() {
        return this.projectileModelFiles;
    }

    public VehicleModelFiles[] getVehicleModelFiles() {
        return this.vehicleModelFiles;
    }

    public ModelExtraResourcesFile getExtraResources() {
        return this.extraResources;
    }

    public Map<String, OuterFileTexture> getAvatarTextures() {
        return this.avatarTextures;
    }

    public Map<String, OuterFileTexture> getGuiTextures() {
        return this.guiTextures;
    }

    public ClientModelInfo withInfo(@Nonnull ServerModelInfo info) {
        return new ClientModelInfo(this.mainModelData, this.projectileModelFiles, this.vehicleModelFiles,
                this.extraResources, info, this.avatarTextures, this.guiTextures);
    }

    @Nonnull
    public ServerModelInfo getInfo() {
        return this.info;
    }
}
