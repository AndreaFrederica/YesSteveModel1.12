package com.elfmcys.yesstevemodel.client.compat;

import com.deeperdepths.common.items.ItemSpyglass;
import com.sirsquidly.oe.Main;
import git.jbredwards.crossbow.api.ICrossbow;
import net.minecraft.item.ItemStack;
import net.minecraft.trident.Trident;
import net.minecraftforge.fml.common.Optional;
import net.smileycorp.crossbows.common.item.ItemCrossbow;
import thedarkcolour.futuremc.item.TridentItem;

public enum ExtraAction {
    SPEAR {
        @Override
        public boolean isAction(ItemStack stack) {
            return (Mods.OE_INSTALLED && isOESpear(stack)) ||
                    (Mods.TM_INSTALLED && isTMSpear(stack)) ||
                    (Mods.FMC_INSTALLED && isFMCSpear(stack));
        }

        @Optional.Method(modid = Mods.OCEANIC_EXPANSE)
        private static boolean isOESpear(ItemStack stack) {
            return stack.getItemUseAction() == Main.SPEAR;
        }

        @Optional.Method(modid = Mods.TRIDENT_MOD)
        private static boolean isTMSpear(ItemStack stack) {
            return stack.getItemUseAction() == Trident.SPEAR;
        }

        @Optional.Method(modid = Mods.FMC)
        private static boolean isFMCSpear(ItemStack stack) {
            return stack.getItemUseAction() == TridentItem.getTRIDENT_USE_ACTION();
        }
    },
    CROSSBOW {
        @Override
        public boolean isAction(ItemStack stack) {
            return (Mods.JCROSSBOW_INSTALLED && isJCrossbowAction(stack)) ||
                    (Mods.SCROSSBOWS_INSTALLED && isSCrossbowsAction(stack));
        }

        @Optional.Method(modid = Mods.J_CROSSBOW)
        private static boolean isJCrossbowAction(ItemStack stack) {
            return stack.getItemUseAction() == ICrossbow.CROSSBOW_ACTION;
        }

        @Optional.Method(modid = Mods.S_CROSSBOWS)
        private static boolean isSCrossbowsAction(ItemStack stack) {
            return stack.getItem() instanceof ItemCrossbow;
        }
    },
    SPYGLASS {
        @Override
        public boolean isAction(ItemStack stack) {
            return (Mods.DD_INSTALLED && isDDAction(stack));
        }

        @Optional.Method(modid = Mods.DEEPER_DEPTHS)
        private static boolean isDDAction(ItemStack stack) {
            return stack.getItem() instanceof ItemSpyglass;
        }
    };

    public abstract boolean isAction(ItemStack stack);
}
