package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.entity.EntityLivingBase;

public class ConditionChair extends AbstractConditionEntity {
    public ConditionChair() {
        super("chair");
    }

    @Override
    public String doTest(EntityLivingBase entity) {
        return this.doTest(entity.getRidingEntity());
    }
}
