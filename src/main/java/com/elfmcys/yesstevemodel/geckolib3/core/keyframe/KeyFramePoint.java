package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.vecmath.Vector3f;

public class KeyFramePoint extends AnimationPoint {
    public final BoneKeyFrame keyframe;

    public KeyFramePoint(double currentTick, BoneKeyFrame keyframe, AnimationControllerContext context) {
        super(currentTick, keyframe.getTotalTick(), context);
        this.keyframe = keyframe;
    }

    @Override
    public Vector3f getLerpPoint(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        this.setupControllerContext(evaluator);
        Vector3f vector3f = this.keyframe.getLerpPoint(evaluator, this.getPercentCompleted());
        if (this.cachedValue == null) {
            this.cachedValue = new Vector3f(vector3f);
        } else {
            this.cachedValue.set(vector3f);
        }
        return vector3f;
    }
}
