package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.entity.Entity;

public class PositionDelta extends EntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        int axis = arguments.getAsInt(context, 0);
        float partialTicks = context.entity().animationEvent().getPartialTick();
        Entity entity = context.entity().entity();
        return switch (axis) {
            case 0 -> Interpolations.lerp(entity.prevPosX, entity.posY, partialTicks) - entity.prevPosX;
            case 1 -> Interpolations.lerp(entity.prevPosY, entity.posY, partialTicks) - entity.prevPosY;
            case 2 -> Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTicks) - entity.prevPosZ;
            default -> null;
        };
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
