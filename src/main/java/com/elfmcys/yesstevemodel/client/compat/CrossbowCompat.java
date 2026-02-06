package com.elfmcys.yesstevemodel.client.compat;

import git.jbredwards.crossbow.api.ICrossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.smileycorp.crossbows.common.item.ItemCrossbow;

public class CrossbowCompat {
    private static final String J_CROSSBOW = "crossbow";
    private static boolean JCROSSBOW_INSTALLED = false;
    private static final String S_CROSSBOWS = "crossbows";
    private static boolean SCROSSBOWS_INSTALLED = false;

    public static void init() {
        JCROSSBOW_INSTALLED = Loader.isModLoaded(J_CROSSBOW);
        SCROSSBOWS_INSTALLED = Loader.isModLoaded(S_CROSSBOWS);
    }

//    public static boolean isInstalled() {
//        return jCrossbowLoaded || sCrossbowsLoaded;
//    }

    public static boolean isCharged(ItemStack stack) {
        return (JCROSSBOW_INSTALLED && isJCrossbowCharged(stack)) ||
                (SCROSSBOWS_INSTALLED && isSCrossbowsCharged(stack));
    }

    @Optional.Method(modid = J_CROSSBOW)
    private static boolean isJCrossbowCharged(ItemStack stack) {
        if (stack.getItem() instanceof ICrossbow) {
            ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            return cap != null && !cap.isEmpty();
        }
        return false;
    }

    @Optional.Method(modid = S_CROSSBOWS)
    private static boolean isSCrossbowsCharged(ItemStack stack) {
        if (stack.getItem() instanceof ItemCrossbow) {
            return ItemCrossbow.isCharged(stack);
        }
        return false;
    }
}
