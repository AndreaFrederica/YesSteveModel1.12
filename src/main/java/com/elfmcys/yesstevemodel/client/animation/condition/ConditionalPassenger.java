package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.entity.EntityLivingBase;

public class ConditionalPassenger extends AbstractConditionEntity {
    public ConditionalPassenger() {
        super("passenger");
    }

    @Override
    public String doTest(EntityLivingBase entity) {
        return this.doTest(entity.getControllingPassenger());
    }
}
