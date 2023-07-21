package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ModelInfoCapability {
    private ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, GeneralConfig.DEFAULT_MODEL_ID.get());
    private ResourceLocation selectTexture = new ResourceLocation(YesSteveModel.MOD_ID, GeneralConfig.DEFAULT_MODEL_ID.get() + "/" + GeneralConfig.DEFAULT_MODEL_TEXTURE.get());
    private String animation = "idle";
    private boolean playAnimation = false;
    private boolean dirty;

    public void setModelAndTexture(ResourceLocation modelId, ResourceLocation selectTexture) {
        this.modelId = modelId;
        this.selectTexture = selectTexture;
        markDirty();
    }

    public void copyFrom(ModelInfoCapability source) {
        this.modelId = source.modelId;
        this.selectTexture = source.selectTexture;
        this.animation = source.animation;
        this.playAnimation = source.playAnimation;
        markDirty();
    }

    public ResourceLocation getModelId() {
        return modelId;
    }

    public ResourceLocation getSelectTexture() {
        return selectTexture;
    }

    public void setSelectTexture(ResourceLocation selectTexture) {
        this.selectTexture = selectTexture;
        markDirty();
    }

    public void playAnimation(String animation) {
        this.animation = animation;
        this.playAnimation = true;
        markDirty();
    }

    public void stopAnimation() {
        this.playAnimation = false;
        markDirty();
    }

    public String getAnimation() {
        return animation;
    }

    public boolean isPlayAnimation() {
        return playAnimation;
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("model_id", this.modelId.toString());
        tag.putString("select_texture", this.selectTexture.toString());
        tag.putString("animation", this.animation);
        tag.putBoolean("play_animation", this.playAnimation);
        return tag;
    }

    public void deserializeNBT(CompoundNBT nbt) {
        this.modelId = new ResourceLocation(nbt.getString("model_id"));
        this.selectTexture = new ResourceLocation(nbt.getString("select_texture"));
        this.animation = nbt.getString("animation");
        this.playAnimation = nbt.getBoolean("play_animation");
    }

    public static class Storage implements Capability.IStorage<ModelInfoCapability> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<ModelInfoCapability> capability, ModelInfoCapability instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<ModelInfoCapability> capability, ModelInfoCapability instance, Direction side, INBT nbt) {
            instance.deserializeNBT((CompoundNBT) nbt);
        }
    }
}
