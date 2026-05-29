package com.elfmcys.yesstevemodel.client.compat;

import com.fuzs.aquaacrobatics.entity.Pose;
import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Optional;

public class SwimmingCompat {
    public static boolean hasSwimming() {
        return Mods.AA_INSTALLED;
    }

    public static boolean isSwimming(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return Mods.AA_INSTALLED && isAASwimming((EntityPlayer) entity);
        }
        return false;
    }

    public static boolean isSwimmingPose(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return Mods.AA_INSTALLED && isAASwimmingPose((EntityPlayer) entity);
        }
        return false;
    }

    @Optional.Method(modid = Mods.AQUA_ACROBATICS)
    private static boolean isAASwimming(EntityPlayer player) {
        if (player instanceof IPlayerResizeable) {
            return ((IPlayerResizeable) player).isSwimming();
        }
        return false;
    }

    @Optional.Method(modid = Mods.AQUA_ACROBATICS)
    private static boolean isAASwimmingPose(EntityPlayer player) {
        if (player instanceof IPlayerResizeable) {
            return ((IPlayerResizeable) player).getPose() == Pose.SWIMMING;
        }
        return false;
    }
}
