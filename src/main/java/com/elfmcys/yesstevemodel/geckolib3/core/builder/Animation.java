/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */
package com.elfmcys.yesstevemodel.geckolib3.core.builder;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.github.bsideup.jabel.Desugar;

import java.util.List;

@Desugar
public record Animation(
        String animationName,
        double animationLength,
        ILoopType loop,
        List<BoneAnimation> boneAnimations,
        List<EventKeyFrame<IValue[]>> customInstructionKeyframes
) {
}
