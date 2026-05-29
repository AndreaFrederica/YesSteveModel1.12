package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.vecmath.Vector3f;

public class ConstantPoint extends AnimationPoint {

    public final Vector3f value;

    public ConstantPoint(double currentTick, double totalTick, Vector3f value, AnimationControllerContext context) {
        super(currentTick, totalTick, context);
        this.value = value;
    }

    @Override
    public double getPercentCompleted() {
        if (this.totalTick == 0.0) {
            return this.currentTick == 0.0 ? 0.0 : 1.0;
        }
        return this.currentTick / this.totalTick;
    }

    @Override
    public Vector3f getLerpPoint(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        if (this.cachedValue == null) {
            this.cachedValue = new Vector3f(this.value);
        } else {
            this.cachedValue.set(this.value);
        }
        return this.value;
    }
}
