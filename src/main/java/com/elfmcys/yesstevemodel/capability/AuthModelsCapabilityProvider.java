package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.util.Keep;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthModelsCapabilityProvider implements ICapabilitySerializable<ListNBT> {
    @CapabilityInject(AuthModelsCapability.class)
    public static Capability<AuthModelsCapability> AUTH_MODELS_CAP = null;
    private AuthModelsCapability instance = AUTH_MODELS_CAP.getDefaultInstance();

    @Nonnull
    @Override
    @Keep
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == AUTH_MODELS_CAP) {
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
    private AuthModelsCapability createCapability() {
        if (instance == null) {
            this.instance = new AuthModelsCapability();
        }
        return instance;
    }

    @Override
    @Keep
    public void deserializeNBT(ListNBT nbt) {
        createCapability().deserializeNBT(nbt);
    }

    @Override
    @Keep
    public ListNBT serializeNBT() {
        return createCapability().serializeNBT();
    }
}
