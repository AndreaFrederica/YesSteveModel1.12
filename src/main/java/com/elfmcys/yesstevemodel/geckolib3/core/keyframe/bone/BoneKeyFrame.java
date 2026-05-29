package com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone;

import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.vecmath.Vector3f;

public abstract class BoneKeyFrame {
    protected final double startTick;
    protected final double totalTick;
    protected final double endTick;
    protected final Vector3v beginPoint;

    public BoneKeyFrame(double startTick, double totalTick, Vector3v beginPoint) {
        this.startTick = startTick;
        this.totalTick = totalTick;
        this.endTick = startTick + totalTick;
        this.beginPoint = beginPoint;
    }

    public double getStartTick() {
        return this.startTick;
    }

    public double getTotalTick() {
        return this.totalTick;
    }

    public float getEndTick() {
        return (float) this.endTick;
    }

    public abstract Vector3f getLerpPoint(ExpressionEvaluator<?> evaluator, double percentCompleted);

    public Vector3f getTransitionPoint(ExpressionEvaluator<?> evaluator, Vector3f offsetPoint, double percentCompleted) {
        if (isBegin(percentCompleted)) {
            return offsetPoint;
        }
        if (isEnd(percentCompleted)) {
            return this.beginPoint.eval(evaluator);
        }
        return MathUtil.lerpValues(percentCompleted, offsetPoint, this.beginPoint.eval(evaluator));
    }

    protected static boolean isBegin(double percentCompleted) {
        return percentCompleted < 0.00001;
    }

    protected static boolean isEnd(double percentCompleted) {
        return percentCompleted > 0.99999;
    }
}
