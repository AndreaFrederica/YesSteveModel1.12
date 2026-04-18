package com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable;
import net.minecraft.entity.projectile.EntityFishHook;

public class FishingHookEntityVariable extends LambdaVariable<EntityFishHook> {
    public FishingHookEntityVariable(IValueEvaluator<?, IContext<EntityFishHook>> evaluator) {
        super(evaluator);
    }

    @Override
    protected boolean validateContext(IContext<?> context) {
        return context.entity() instanceof EntityFishHook;
    }
}
