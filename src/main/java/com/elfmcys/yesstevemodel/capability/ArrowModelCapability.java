package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ArrowModelCapability {
    public static final String DEFAULT = new ResourceLocation(YesSteveModel.MOD_ID, "default").toString();
    public static final String EMPTY = new ResourceLocation(YesSteveModel.MOD_ID, "ysm_empty").toString();
    private String modelId = EMPTY;

    public String getModelId() {
        return this.modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void copyFrom(ArrowModelCapability source) {
        this.modelId = source.modelId;
    }

    public NBTTagString serializeNBT() {
        return new NBTTagString(this.modelId);
    }

    public void deserializeNBT(NBTTagString nbt) {
        this.modelId = nbt.getString();
    }

    public static class Storage implements Capability.IStorage<ArrowModelCapability> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<ArrowModelCapability> capability, ArrowModelCapability instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<ArrowModelCapability> capability, ArrowModelCapability instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagString) nbt);
        }
    }
}
