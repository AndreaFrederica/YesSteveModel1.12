/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */
package com.elfmcys.yesstevemodel.geckolib3.core.builder;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.ParticleEventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import javax.annotation.Nullable;
import java.util.List;

public class Animation {

    public final String animationName;

    public final float animationLength;

    public final ILoopType loop;

    @Nullable
    public final IValue unKnowData1;

    @Nullable
    public final IValue unKnowData2;

    @Nullable
    public final IValue blendWeight;

    @Nullable
    public final Boolean override;

    public final List<BoneAnimation> boneAnimations;

    public final List<EventKeyFrame<String>> soundKeyFrames;

    public final List<ParticleEventKeyFrame> particleKeyFrames;

    public final List<EventKeyFrame<IValue[]>> customInstructionKeyframes;

    public boolean isFromPrimaryAssembly = false;

    public Animation(String animationName, double animationLength, ILoopType loop,
                     @Nullable IValue unKnowData1, @Nullable IValue unKnowData2,
                     @Nullable IValue blendWeight, @Nullable Boolean overridePrevAnim,
                     BoneAnimation[] boneAnimations,
                     EventKeyFrame<String>[] soundKeyFrames,
                     ParticleEventKeyFrame[] particleKeyFrames,
                     EventKeyFrame<IValue[]>[] customInstructionKeyframes) {
        this.animationName = animationName;
        this.animationLength = (float) animationLength;
        this.loop = loop;
        this.unKnowData1 = unKnowData1;
        this.unKnowData2 = unKnowData2;
        this.blendWeight = blendWeight;
        this.override = overridePrevAnim;
        this.boneAnimations = ReferenceArrayList.wrap(boneAnimations);
        this.soundKeyFrames = ReferenceArrayList.wrap(soundKeyFrames);
        this.particleKeyFrames = ReferenceArrayList.wrap(particleKeyFrames);
        this.customInstructionKeyframes = ReferenceArrayList.wrap(customInstructionKeyframes);
    }

    public boolean isEmpty() {
        return this.boneAnimations.isEmpty() && this.soundKeyFrames.isEmpty() && this.particleKeyFrames.isEmpty() && this.customInstructionKeyframes.isEmpty();
    }

    // Record-style accessor methods for backward compatibility
    public String animationName() {
        return this.animationName;
    }

    public float animationLength() {
        return this.animationLength;
    }

    public ILoopType loop() {
        return this.loop;
    }

    public List<BoneAnimation> boneAnimations() {
        return this.boneAnimations;
    }

    public List<EventKeyFrame<IValue[]>> customInstructionKeyframes() {
        return this.customInstructionKeyframes;
    }
}
