package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.vecmath.Vector3f;

public class TransitionKeyFrame extends BoneKeyFrame {
    private final Vector3v postPoint;

    public TransitionKeyFrame(double firstStartTick, Vector3v firstPoint, Vector3v postPoint) {
        super(0, firstStartTick, firstPoint);
        this.postPoint = postPoint;
    }

    @Override
    public Vector3f getLerpPoint(ExpressionEvaluator<?> evaluator, double percentCompleted) {
        if (!isEnd(percentCompleted)) {
            return this.beginPoint.eval(evaluator);
        } else {
            return this.postPoint.eval(evaluator);
        }
    }

    public Vector3f evaluate(ExpressionEvaluator<?> evaluator) {
        return this.beginPoint.eval(evaluator);
    }
}
