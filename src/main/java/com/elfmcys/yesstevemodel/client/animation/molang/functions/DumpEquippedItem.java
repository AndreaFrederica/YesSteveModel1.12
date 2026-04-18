package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.util.ComponentUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Map;

public class DumpEquippedItem extends LivingEntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<EntityLivingBase>> context, ArgumentCollection arguments) {
        IContext<EntityLivingBase> ctx = context.entity();
        if (!ctx.hasDebugOutput()) {
            return null;
        }

        String slotName = arguments.getAsString(context, 0);
        EntityEquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), slotName);
        if (slotType == null) {
            return null;
        }

        ItemStack stack = ctx.entity().getItemStackFromSlot(slotType);
        if (stack.isEmpty()) {
            return null;
        }

        Item item = stack.getItem();
        ResourceLocation itemId = item.getRegistryName();
        if (itemId == null) {
            return null;
        }

        ctx.debugOutput(new TextComponentString("Display ").appendSibling(ComponentUtils.copyOnClickText(item.getItemStackDisplayName(stack))));
        ctx.debugOutput(new TextComponentString("Name ").appendSibling(ComponentUtils.copyOnClickText(itemId.toString())));
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            ctx.debugOutput(new TextComponentString("OreDict: name ").appendSibling(ComponentUtils.copyOnClickText(OreDictionary.getOreName(oreId)))
                    .appendText("  id ").appendSibling(ComponentUtils.copyOnClickText(String.valueOf(oreId))));
        }
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            ResourceLocation enchantmentId = enchantment.getRegistryName();
            if (enchantmentId == null) {
                continue;
            }
            String displayName = enchantment.getTranslatedName(entry.getValue());
            ctx.debugOutput(new TextComponentString("Enchantment: display ").appendSibling(ComponentUtils.copyOnClickText(displayName))
                    .appendText("  name ").appendSibling(ComponentUtils.copyOnClickText(enchantmentId.toString())));
        }
        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
