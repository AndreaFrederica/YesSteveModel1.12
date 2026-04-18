package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable;
import net.minecraft.client.entity.EntityPlayerSP;

public class LocalPlayerEntityVariable extends LambdaVariable<EntityPlayerSP> {
    public LocalPlayerEntityVariable(IValueEvaluator<?, IContext<EntityPlayerSP>> evaluator) {
        super(evaluator);
    }

    @Override
    protected boolean validateContext(IContext<?> context) {
        return context.entity() instanceof EntityPlayerSP;
    }
}
