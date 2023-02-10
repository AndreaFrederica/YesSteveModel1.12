package com.elfmcys.yesstevemodel.capability;

import com.google.common.collect.Sets;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Set;

public class StarModelsCapability {
    private Set<ResourceLocation> starModels = Sets.newHashSet();

    public void addModel(ResourceLocation modelId) {
        starModels.add(modelId);
    }

    public void copyFrom(StarModelsCapability source) {
        this.starModels = source.starModels;
    }

    public void removeModel(ResourceLocation modelId) {
        starModels.remove(modelId);
    }

    public boolean containModel(ResourceLocation modelId) {
        return starModels.contains(modelId);
    }

    public Set<ResourceLocation> getStarModels() {
        return starModels;
    }

    public void setStarModels(Set<ResourceLocation> starModels) {
        this.starModels = starModels;
    }

    public void clear() {
        starModels.clear();
    }

    public ListNBT serializeNBT() {
        ListNBT listTag = new ListNBT();
        for (ResourceLocation modelId : starModels) {
            listTag.add(StringNBT.valueOf(modelId.toString()));
        }
        return listTag;
    }

    public void deserializeNBT(ListNBT nbt) {
        this.starModels.clear();
        for (INBT tag : nbt) {
            starModels.add(new ResourceLocation(tag.getAsString()));
        }
    }

    public static class Storage implements Capability.IStorage<StarModelsCapability> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<StarModelsCapability> capability, StarModelsCapability instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<StarModelsCapability> capability, StarModelsCapability instance, Direction side, INBT nbt) {
            instance.deserializeNBT((ListNBT) nbt);
        }
    }
}
