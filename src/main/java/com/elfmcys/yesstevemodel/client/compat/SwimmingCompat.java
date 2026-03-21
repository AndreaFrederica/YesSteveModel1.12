package com.elfmcys.yesstevemodel.client.compat;

import com.fuzs.aquaacrobatics.entity.Pose;
import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Optional;

public class SwimmingCompat {
    public static boolean hasSwimming() {
        return Mods.AA_INSTALLED;
    }

    public static boolean isSwimming(EntityPlayer player) {
        return Mods.AA_INSTALLED && isAASwimming(player);
    }

    public static boolean isSwimmingPose(EntityPlayer player) {
        return Mods.AA_INSTALLED && isAASwimmingPose(player);
    }

    @Optional.Method(modid = Mods.AQUA_ACROBATICS)
    private static boolean isAASwimming(EntityPlayer player) {
        if (player instanceof IPlayerResizeable resizeable) {
            return resizeable.isSwimming();
        }
        return false;
    }

    @Optional.Method(modid = Mods.AQUA_ACROBATICS)
    private static boolean isAASwimmingPose(EntityPlayer player) {
        if (player instanceof IPlayerResizeable resizeable) {
            return resizeable.getPose() == Pose.SWIMMING;
        }
        return false;
    }
}
