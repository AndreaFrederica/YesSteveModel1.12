package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class PositionDelta extends EntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        int axis = arguments.getAsInt(context, 0);
        Vec3d delta = context.entity().geoInstance().getPositionTracker().getPositionDelta();
        return switch (axis) {
            case 0 -> delta.x;
            case 1 -> delta.y;
            case 2 -> delta.z;
            default -> null;
        };
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
