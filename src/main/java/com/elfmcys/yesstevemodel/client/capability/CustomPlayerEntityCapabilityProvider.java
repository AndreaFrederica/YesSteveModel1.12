package com.elfmcys.yesstevemodel.client.capability;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class CustomPlayerEntityCapabilityProvider implements ICapabilityProvider {
    @CapabilityInject(CustomPlayerEntity.class)
    public static Capability<CustomPlayerEntity> CAP = null;
    private CustomPlayerEntity instance = CAP.getDefaultInstance();
    private EntityPlayer entity;

    public CustomPlayerEntityCapabilityProvider(EntityPlayer entity) {
        this.entity = entity;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
        return cap == CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (this.hasCapability(cap, side)) {
            return CAP.cast(this.createCapability());
        }
        return null;
    }

    @Nonnull
    private CustomPlayerEntity createCapability() {
        if (this.instance == null) {
            this.instance = new CustomPlayerEntity(this.entity);
            this.entity = null;
        }
        return this.instance;
    }
}
