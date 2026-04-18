package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.util.ComponentUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.oredict.OreDictionary;

public class DumpRelativeBlock extends EntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        IContext<Entity> ctx = context.entity();
        if (!ctx.hasDebugOutput()) {
            return null;
        }

        IBlockState blockState = MolangUtils.getRelativeBlock(context, arguments);
        if (blockState == null) {
            return null;
        }

        Block block = blockState.getBlock();
        ResourceLocation blockId = block.getRegistryName();
        if (blockId == null) {
            return null;
        }

        ctx.debugOutput(new TextComponentString("Display ").appendSibling(ComponentUtils.copyOnClickText(block.getLocalizedName())));
        ctx.debugOutput(new TextComponentString("Name ").appendSibling(ComponentUtils.copyOnClickText(blockId.toString())));
        ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(blockState));
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            ctx.debugOutput(new TextComponentString("OreDict: name ").appendSibling(ComponentUtils.copyOnClickText(OreDictionary.getOreName(oreId)))
                    .appendText("  id ").appendSibling(ComponentUtils.copyOnClickText(String.valueOf(oreId))));
        }
        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 3;
    }
}
