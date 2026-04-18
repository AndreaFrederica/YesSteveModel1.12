package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RelativeBlockName extends EntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> ctx, Function.ArgumentCollection arguments) {
        IBlockState block = MolangUtils.getRelativeBlock(ctx, arguments);
        if (block == null) {
            return null;
        }

        ResourceLocation blockId = block.getBlock().getRegistryName();
        if (blockId == null) {
            return null;
        }

        return blockId.toString();
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 3;
    }
}
