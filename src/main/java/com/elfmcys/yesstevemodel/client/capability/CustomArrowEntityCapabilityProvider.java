package com.elfmcys.yesstevemodel.client.capability;

import com.elfmcys.yesstevemodel.client.entity.CustomArrowEntity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class CustomArrowEntityCapabilityProvider implements ICapabilityProvider {
    @CapabilityInject(CustomArrowEntity.class)
    public static Capability<CustomArrowEntity> CAP = null;
    private CustomArrowEntity instance = CAP.getDefaultInstance();
    private EntityArrow entity;

    public CustomArrowEntityCapabilityProvider(EntityArrow entity) {
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
    private CustomArrowEntity createCapability() {
        if (this.instance == null) {
            this.instance = new CustomArrowEntity(this.entity);
            this.entity = null;
        }
        return this.instance;
    }
}
