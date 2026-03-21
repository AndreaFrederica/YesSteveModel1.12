package com.elfmcys.yesstevemodel.client.compat;

import mekanism.api.mixninapi.ElytraMixinHelp;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;

public class ElytraCompat {
    public static boolean isWearingElytra(EntityLivingBase player) {
        ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        return (Mods.MEK_MIXIN_INSTALLED && isMekMixinElytra(stack, player)) ||
                stack.getItem() instanceof ItemElytra;
    }

    @Optional.Method(modid = Mods.MEK_MIXIN)
    private static boolean isMekMixinElytra(ItemStack stack, EntityLivingBase player) {
        if (stack.getItem() instanceof ElytraMixinHelp help) {
            return help.canElytraFly(stack, player);
        }
        return false;
    }

    @GameRegistry.ObjectHolder("mekanism:hdpe_elytra")
    private static final Item HDPE_REINFORCED_ELYTRA = Items.AIR;
    private static final ResourceLocation WINGS_LOCATION = new ResourceLocation("textures/entity/elytra.png");
    private static final ResourceLocation HDPE_WINGS_LOCATION = new ResourceLocation("mekanism", "textures/entities/hdpe_elytra.png");

    @Nonnull
    public static ResourceLocation getDefaultTexture(Item elytra) {
        return elytra != Items.AIR && elytra == HDPE_REINFORCED_ELYTRA ? HDPE_WINGS_LOCATION : WINGS_LOCATION;
    }
}
