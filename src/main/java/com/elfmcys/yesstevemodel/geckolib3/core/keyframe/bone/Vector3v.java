package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.vecmath.Vector3f;

public class Vector3v {
    private final IValue x;
    private final IValue y;
    private final IValue z;

    public Vector3v(IValue x, IValue y, IValue z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f eval(ExpressionEvaluator<?> evaluator) {
        return new Vector3f((float) this.x.evalAsDouble(evaluator),
                (float) this.y.evalAsDouble(evaluator),
                (float) this.z.evalAsDouble(evaluator));
    }
}
