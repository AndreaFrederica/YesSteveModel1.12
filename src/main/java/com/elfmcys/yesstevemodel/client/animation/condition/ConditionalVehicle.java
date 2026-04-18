package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.entity.player.EntityPlayer;

public class ConditionalVehicle extends AbstractConditionEntity {
    public ConditionalVehicle() {
        super("vehicle");
    }

    @Override
    public String doTest(EntityPlayer player) {
        return this.doTest(player.getRidingEntity());
    }
}
