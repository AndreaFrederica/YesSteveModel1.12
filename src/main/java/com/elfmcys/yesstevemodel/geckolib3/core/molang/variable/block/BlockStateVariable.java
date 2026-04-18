package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable;
import net.minecraft.block.state.IBlockState;

public class BlockStateVariable extends LambdaVariable<IBlockState> {
    public BlockStateVariable(IValueEvaluator<?, IContext<IBlockState>> evaluator) {
        super(evaluator);
    }

    @Override
    protected boolean validateContext(IContext<?> context) {
        return context.entity() instanceof IBlockState;
    }
}
