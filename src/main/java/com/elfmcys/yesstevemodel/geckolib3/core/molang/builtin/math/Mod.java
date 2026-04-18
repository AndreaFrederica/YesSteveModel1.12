package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.math;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;

import javax.annotation.Nonnull;

public class Mod implements Function {
    @Override
    public Object evaluate(@Nonnull ExecutionContext<?> context, @Nonnull ArgumentCollection arguments) {
        return arguments.getAsDouble(context, 0) % arguments.getAsDouble(context, 1);
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 2;
    }
}
