package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.vecmath.Vector3f;

public class LinearKeyFrame extends BoneKeyFrame {
    private final Vector3v endPoint;
    private final Vector3v postPoint;

    public LinearKeyFrame(double startTick, double totalTick, Vector3v beginPoint, Vector3v endPoint, Vector3v postPoint) {
        super(startTick, totalTick, beginPoint);
        this.endPoint = endPoint;
        this.postPoint = postPoint;
    }

    @Override
    public Vector3f getLerpPoint(ExpressionEvaluator<?> evaluator, double percentCompleted) {
        if (isBegin(percentCompleted)) {
            return this.beginPoint.eval(evaluator);
        }
        if (isEnd(percentCompleted)) {
            return this.postPoint.eval(evaluator);
        }
        Vector3f begin = this.beginPoint.eval(evaluator);
        Vector3f end = this.endPoint.eval(evaluator);
        return MathUtil.lerpValues(percentCompleted, begin, end);
    }
}
