package com.elfmcys.yesstevemodel.mixin.early;

import com.elfmcys.yesstevemodel.api.IArrowExtraInfo;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(EntityArrow.class)
public class EntityArrowMixin implements IArrowExtraInfo {
    @Unique
    private static final DataParameter<String> MODEL_ID = EntityDataManager.createKey(EntityArrow.class, DataSerializers.STRING);
    @Unique
    private static final String MODEL_ID_TAG = "YsmArrowModelId";

    @Inject(method = "entityInit", at = @At("RETURN"))
    private void defineSynchedData(CallbackInfo callbackInfo) {
        this.getYsmEntityData().register(MODEL_ID, IArrowExtraInfo.EMPTY);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)V", at = @At("RETURN"))
    private void setOwner(World world, EntityLivingBase entity, CallbackInfo callbackInfo) {
        if (entity instanceof EntityPlayer player) {
            CapabilityEvent.getCapability(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> this.getYsmEntityData().set(MODEL_ID, cap.getModelId().toString()));
        }
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    private void addAdditionalSaveData(NBTTagCompound pCompound, CallbackInfo callbackInfo) {
        pCompound.setString(MODEL_ID_TAG, this.getYsmModelId());
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    private void readAdditionalSaveData(NBTTagCompound pCompound, CallbackInfo callbackInfo) {
        if (pCompound.hasKey(MODEL_ID_TAG)) {
            this.getYsmEntityData().set(MODEL_ID, pCompound.getString(MODEL_ID_TAG));
        }
    }

    @Override
    @Unique
    public String getYsmModelId() {
        return this.getYsmEntityData().get(MODEL_ID);
    }

    @Unique
    public EntityDataManager getYsmEntityData() {
        return ((Entity) (Object) this).getDataManager();
    }
}
