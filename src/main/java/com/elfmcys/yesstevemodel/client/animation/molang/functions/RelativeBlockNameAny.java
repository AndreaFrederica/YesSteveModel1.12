package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RelativeBlockNameAny extends EntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> ctx, ArgumentCollection arguments) {
        IBlockState block = MolangUtils.getRelativeBlock(ctx, arguments);
        if (block == null) {
            return null;
        }

        ResourceLocation blockId = block.getBlock().getRegistryName();
        if (blockId == null) {
            return null;
        }

        for (int i = 3; i < arguments.size(); ++i) {
            if (blockId.equals(MolangUtils.parseResourceLocation(ctx.entity(), arguments.getAsString(ctx, i)))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size > 3;
    }
}
