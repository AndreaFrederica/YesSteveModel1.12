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

public class AuthModelsCapability {
    private Set<ResourceLocation> authModels = Sets.newHashSet();

    public void addModel(ResourceLocation modelId) {
        authModels.add(modelId);
    }

    public void copyFrom(AuthModelsCapability source) {
        this.authModels = source.authModels;
    }

    public void removeModel(ResourceLocation modelId) {
        authModels.remove(modelId);
    }

    public boolean containModel(ResourceLocation modelId) {
        return authModels.contains(modelId);
    }

    public Set<ResourceLocation> getAuthModels() {
        return authModels;
    }

    public void setAuthModels(Set<ResourceLocation> authModels) {
        this.authModels = authModels;
    }

    public void clear() {
        authModels.clear();
    }

    public ListNBT serializeNBT() {
        ListNBT listTag = new ListNBT();
        for (ResourceLocation modelId : authModels) {
            listTag.add(StringNBT.valueOf(modelId.toString()));
        }
        return listTag;
    }

    public void deserializeNBT(ListNBT nbt) {
        this.authModels.clear();
        for (INBT tag : nbt) {
            authModels.add(new ResourceLocation(tag.getAsString()));
        }
    }

    public static class Storage implements Capability.IStorage<AuthModelsCapability> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<AuthModelsCapability> capability, AuthModelsCapability instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<AuthModelsCapability> capability, AuthModelsCapability instance, Direction side, INBT nbt) {
            instance.deserializeNBT((ListNBT) nbt);
        }
    }
}
