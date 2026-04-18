package com.elfmcys.yesstevemodel.geckolib3.core.molang.value;

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.github.bsideup.jabel.Desugar;

@Desugar
public record RotationValue(IValue value, boolean flip) implements IValue {
    public static float processValue(double value, boolean flip) {
        float ret = (float) Math.toRadians(value);
        if (flip) {
            ret = -ret;
        }
        return ret;
    }

    @Override
    public double evalAsDouble(ExpressionEvaluator<?> evaluator) {
        return processValue(this.value.evalAsDouble(evaluator), this.flip);
    }

    @Override
    public Object evalUnsafe(ExpressionEvaluator<?> evaluator) {
        return this.evalAsDouble(evaluator);
    }
}
