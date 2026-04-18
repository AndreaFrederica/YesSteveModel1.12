package com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.ContextFunction;
import net.minecraft.entity.EntityLivingBase;

public abstract class LivingEntityFunction extends ContextFunction<EntityLivingBase> {
    @Override
    protected boolean validateContext(IContext<?> context) {
        return context.entity() instanceof EntityLivingBase;
    }
}
