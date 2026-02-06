package com.elfmcys.yesstevemodel.client.compat;


import mekanism.api.mixninapi.ElytraMixinHelp;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

public class ElytraCompat {
    private static final String MekMixin = "mekmixinhelp";
    private static boolean MekMixin_INSTALLED = false;

    public static void init() {
        MekMixin_INSTALLED = Loader.isModLoaded(MekMixin);
    }

    public static boolean isElytra(EntityLivingBase player) {
        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return (MekMixin_INSTALLED && hasElytra(stack, player)) || stack.getItem() instanceof ItemElytra;
    }


    @Optional.Method(modid = MekMixin)
    private static boolean hasElytra(ItemStack stack, EntityLivingBase player) {
        if (stack.getItem() instanceof ElytraMixinHelp help) {
            return help.canElytraFly(stack, player);
        }
        return false;
    }

}
