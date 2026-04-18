/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.keyframe;

import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;

import javax.annotation.Nullable;

public class BoneAnimationQueue {
    public final BoneTopLevelSnapshot topLevelSnapshot;
    public final BoneSnapshot controllerSnapshot;
    @Nullable
    public BoneAnimation animation;

    public AnimationPointQueue rotationQueue = new AnimationPointQueue();
    public AnimationPointQueue positionQueue = new AnimationPointQueue();
    public AnimationPointQueue scaleQueue = new AnimationPointQueue();

    public BoneAnimationQueue(BoneTopLevelSnapshot snapshot) {
        this.topLevelSnapshot = snapshot;
        this.controllerSnapshot = new BoneSnapshot(snapshot);
    }

    public BoneSnapshot snapshot() {
        return this.controllerSnapshot;
    }

    public AnimationPointQueue rotationQueue() {
        return this.rotationQueue;
    }

    public AnimationPointQueue positionQueue() {
        return this.positionQueue;
    }

    public AnimationPointQueue scaleQueue() {
        return this.scaleQueue;
    }

    public void updateSnapshot() {
        this.controllerSnapshot.copyFrom(this.topLevelSnapshot);
    }

    // 链表重开比 clear() 快
    public void resetQueues() {
        this.rotationQueue = new AnimationPointQueue();
        this.positionQueue = new AnimationPointQueue();
        this.scaleQueue = new AnimationPointQueue();
    }
}
