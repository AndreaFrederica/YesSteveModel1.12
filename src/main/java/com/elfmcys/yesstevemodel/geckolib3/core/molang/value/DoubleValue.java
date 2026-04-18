package com.elfmcys.yesstevemodel.geckolib3.core.molang.value;

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.github.bsideup.jabel.Desugar;

@Desugar
public record DoubleValue(double value) implements IValue {
    public static final DoubleValue ONE = new DoubleValue(1);
    public static final DoubleValue ZERO = new DoubleValue(0);

    @Override
    public double evalAsDouble(ExpressionEvaluator<?> evaluator) {
        return this.value;
    }

    @Override
    public boolean evalAsBoolean(ExpressionEvaluator<?> evaluator) {
        return this.value != 0;
    }

    @Override
    public Object evalUnsafe(ExpressionEvaluator<?> evaluator) {
        return this.value;
    }
}
