package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.TransitionKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.vecmath.Vector3f;

public class TransitionPoint extends AnimationPoint {

    public final float lerpFactor;

    public final Vector3f offsetPoint;

    public final TransitionKeyFrame dstKeyframe;

    public TransitionPoint(double currentTick, float lerpFactor, double totalTick, Vector3f offsetPoint, TransitionKeyFrame dstKeyframe, AnimationControllerContext context) {
        super(currentTick, totalTick, context);
        this.lerpFactor = lerpFactor;
        this.offsetPoint = offsetPoint;
        this.dstKeyframe = dstKeyframe;
    }

    // Backward-compatible constructor for old AnimationController
    public TransitionPoint(double currentTick, double totalTick, Vector3f offsetPoint, BoneKeyFrame dstKeyframe, AnimationControllerContext context) {
        super(currentTick, totalTick, context);
        this.lerpFactor = 1.0f;
        this.offsetPoint = offsetPoint;
        this.dstKeyframe = (TransitionKeyFrame) dstKeyframe;
    }

    @Override
    public Vector3f getLerpPoint(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        setupControllerContext(evaluator);
        Vector3f vector3f = this.dstKeyframe.evaluate(evaluator);
        MathUtil.lerpValues(this.lerpFactor, this.offsetPoint, vector3f, vector3f);
        if (this.cachedValue == null) {
            this.cachedValue = new Vector3f(vector3f);
        } else {
            this.cachedValue.set(vector3f);
        }
        return vector3f;
    }

    public Vector3f evaluateRaw(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        setupControllerContext(evaluator);
        return this.dstKeyframe.evaluate(evaluator);
    }

    public Vector3f getOffsetPoint() {
        return this.offsetPoint;
    }

    public float getLerpFactor() {
        return this.lerpFactor;
    }
}
