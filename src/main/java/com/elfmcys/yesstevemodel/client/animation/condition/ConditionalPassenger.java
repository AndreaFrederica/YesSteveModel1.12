package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.entity.player.EntityPlayer;

public class ConditionalPassenger extends AbstractConditionEntity {
    public ConditionalPassenger() {
        super("passenger");
    }

    @Override
    public String doTest(EntityPlayer player) {
        return this.doTest(player.getControllingPassenger());
    }
}
