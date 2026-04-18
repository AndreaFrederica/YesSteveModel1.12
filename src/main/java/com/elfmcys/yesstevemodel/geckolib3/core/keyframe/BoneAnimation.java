/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.github.bsideup.jabel.Desugar;

import java.util.List;

@Desugar
public record BoneAnimation(
        String boneName,
        List<BoneKeyFrame> rotationKeyFrames,
        List<BoneKeyFrame> positionKeyFrames,
        List<BoneKeyFrame> scaleKeyFrames
) {
}
