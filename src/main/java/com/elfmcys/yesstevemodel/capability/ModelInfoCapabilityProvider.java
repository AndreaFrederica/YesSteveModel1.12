package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.util.Keep;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelInfoCapabilityProvider implements ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(ModelInfoCapability.class)
    public static Capability<ModelInfoCapability> MODEL_INFO_CAP = null;
    private ModelInfoCapability instance = MODEL_INFO_CAP.getDefaultInstance();

    @Nonnull
    @Override
    @Keep
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == MODEL_INFO_CAP) {
            return LazyOptional.of(this::createCapability).cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    @Keep
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        return getCapability(cap, null);
    }

    @Nonnull
    private ModelInfoCapability createCapability() {
        if (instance == null) {
            this.instance = new ModelInfoCapability();
        }
        return instance;
    }

    @Override
    @Keep
    public void deserializeNBT(CompoundNBT nbt) {
        createCapability().deserializeNBT(nbt);
    }

    @Override
    @Keep
    public CompoundNBT serializeNBT() {
        return createCapability().serializeNBT();
    }
}
