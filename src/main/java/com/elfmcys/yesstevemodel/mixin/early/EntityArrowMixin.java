package com.elfmcys.yesstevemodel.mixin.early;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.mixininterface.EntityArrowAccessor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityArrow.class)
public class EntityArrowMixin implements EntityArrowAccessor {
    @Unique
    private String ysm$shootItemId = StringUtils.EMPTY;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)V", at = @At("RETURN"))
    private void setOwner(World world, EntityLivingBase entity, CallbackInfo callbackInfo) {
        if (entity instanceof EntityPlayer player) {
            CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
                EntityArrow self = (EntityArrow) (Object) this;
                CapabilityEvent.getArrowModelCap(self).ifPresent(arrowCap -> {
                    arrowCap.setModelId(cap.getModelId().toString());
                });
            });
        }
        // 记录射箭时所用的物品
        ResourceLocation mainHandItemId = entity.getHeldItemMainhand().getItem().getRegistryName();
        if (mainHandItemId != null) {
            this.ysm$shootItemId = mainHandItemId.toString();
        }
    }

    @Unique
    @Override
    public String ysm$getShootItemId() {
        return this.ysm$shootItemId;
    }

    /*
    对旧存档的兼容
     */

    @Unique
    private static final String MODEL_ID_TAG = "YsmArrowModelId";

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    private void readAdditionalSaveData(NBTTagCompound pCompound, CallbackInfo callbackInfo) {
        if (pCompound.hasKey(MODEL_ID_TAG)) {
            EntityArrow self = (EntityArrow) (Object) this;
            CapabilityEvent.getArrowModelCap(self).ifPresent(cap -> cap.setModelId(pCompound.getString(MODEL_ID_TAG)));
            pCompound.removeTag(MODEL_ID_TAG);
        }
    }
}
