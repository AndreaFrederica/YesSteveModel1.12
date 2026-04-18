package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;

import javax.annotation.Nonnull;

public class MinAngle implements Function {
    @Override
    public Object evaluate(@Nonnull ExecutionContext<?> context, @Nonnull ArgumentCollection arguments) {
        double angle = arguments.getAsDouble(context, 0) % 360;
        if (angle >= 180) {
            return angle - 360;
        } else if (angle < -180) {
            return angle + 360;
        } else {
            return angle;
        }
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
