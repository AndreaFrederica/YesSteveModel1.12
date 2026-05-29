package com.elfmcys.yesstevemodel.geckolib3.core.snapshot;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.PointData;

public class BoneTopLevelSnapshot extends BoneSnapshot {
    public final IBone bone;
    public final PointData cachedPointData = new PointData();

    public float mostRecentResetRotationTick = 0;
    public float mostRecentResetPositionTick = 0;
    public float mostRecentResetScaleTick = 0;
    public boolean isCurrentlyRunningAnimation = false;
    public boolean isCurrentlyRunningRotationAnimation = true;
    public boolean isCurrentlyRunningPositionAnimation = true;
    public boolean isCurrentlyRunningScaleAnimation = true;

    public BoneTopLevelSnapshot(IBone bone) {
        super(bone.getName());
        this.bone = bone;
        applyTransform(bone);
    }

    public BoneTopLevelSnapshot(IBone bone, boolean dontSaveRotations) {
        this(bone);
        if (dontSaveRotations) {
            this.rotationValueX = 0;
            this.rotationValueY = 0;
            this.rotationValueZ = 0;
            this.rotation.set(0, 0, 0);
        }
    }

    public void commit() {
        this.bone.setHidden(this.hidden, this.childrenHidden);

        this.bone.setRotationX(this.rotationValueX);
        this.bone.setRotationY(this.rotationValueY);
        this.bone.setRotationZ(this.rotationValueZ);

        this.bone.setPositionX(this.positionOffsetX);
        this.bone.setPositionY(this.positionOffsetY);
        this.bone.setPositionZ(this.positionOffsetZ);

        this.bone.setScaleX(this.scaleValueX);
        this.bone.setScaleY(this.scaleValueY);
        this.bone.setScaleZ(this.scaleValueZ);

        this.cachedPointData.rotationValueX = 0;
        this.cachedPointData.rotationValueY = 0;
        this.cachedPointData.rotationValueZ = 0;
    }

    public void reset() {
        this.bone.setHidden(this.hidden, this.childrenHidden);
        this.bone.setRotationX(this.rotation.x);
        this.bone.setRotationY(this.rotation.y);
        this.bone.setRotationZ(this.rotation.z);
        this.bone.setPositionX(this.position.x);
        this.bone.setPositionY(this.position.y);
        this.bone.setPositionZ(this.position.z);
        this.bone.setScaleX(this.scale.x);
        this.bone.setScaleY(this.scale.y);
        this.bone.setScaleZ(this.scale.z);
    }
}
