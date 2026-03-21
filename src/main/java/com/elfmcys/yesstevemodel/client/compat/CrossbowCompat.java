package com.elfmcys.yesstevemodel.client.compat;

import git.jbredwards.crossbow.api.ICrossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.smileycorp.crossbows.common.item.ItemCrossbow;

public class CrossbowCompat {
    public static boolean isCharged(ItemStack stack) {
        return (Mods.JCROSSBOW_INSTALLED && isJCrossbowCharged(stack)) ||
                (Mods.SCROSSBOWS_INSTALLED && isSCrossbowsCharged(stack));
    }

    @Optional.Method(modid = Mods.J_CROSSBOW)
    private static boolean isJCrossbowCharged(ItemStack stack) {
        if (stack.getItem() instanceof ICrossbow) {
            ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            return cap != null && !cap.isEmpty();
        }
        return false;
    }

    @Optional.Method(modid = Mods.S_CROSSBOWS)
    private static boolean isSCrossbowsCharged(ItemStack stack) {
        if (stack.getItem() instanceof ItemCrossbow) {
            return ItemCrossbow.isCharged(stack);
        }
        return false;
    }
}
