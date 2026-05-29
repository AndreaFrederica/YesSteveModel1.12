package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.entity.EntityLivingBase;

public class ConditionalVehicle extends AbstractConditionEntity {
    public ConditionalVehicle() {
        super("vehicle");
    }

    @Override
    public String doTest(EntityLivingBase entity) {
        return this.doTest(entity.getRidingEntity());
    }
}
