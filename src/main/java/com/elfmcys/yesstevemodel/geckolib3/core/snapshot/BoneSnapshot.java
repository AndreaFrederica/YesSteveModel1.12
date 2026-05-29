/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.snapshot;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;

import javax.vecmath.Vector3f;

public class BoneSnapshot {
    public final String name;

    public float scaleValueX;
    public float scaleValueY;
    public float scaleValueZ;
    public float positionOffsetX;
    public float positionOffsetY;
    public float positionOffsetZ;
    public float rotationValueX;
    public float rotationValueY;
    public float rotationValueZ;

    public boolean hidden;
    public boolean childrenHidden;

    // Vector3f fields for new controller path (reference compatibility)
    public final Vector3f position = new Vector3f();
    public final Vector3f rotation = new Vector3f();
    public final Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);

    protected BoneSnapshot(String name) {
        this.name = name;
    }

    public BoneSnapshot(BoneSnapshot snapshot) {
        this.copyFrom(snapshot);
        this.name = snapshot.name;
    }

    public BoneSnapshot(IBone bone) {
        this.name = bone.getName();
        applyTransform(bone);
    }

    public void applyTransform(IBone bone) {
        Vector3f initialRotation = bone.getInitialRotation();
        this.position.set(bone.getPositionX(), bone.getPositionY(), bone.getPositionZ());
        this.rotation.set(bone.getRotationX() - initialRotation.x, bone.getRotationY() - initialRotation.y, bone.getRotationZ() - initialRotation.z);
        this.scale.set(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        this.hidden = bone.isHidden();
        this.childrenHidden = bone.childBonesAreHiddenToo();

        // Also update individual fields for backward compat
        this.rotationValueX = this.rotation.x;
        this.rotationValueY = this.rotation.y;
        this.rotationValueZ = this.rotation.z;
        this.positionOffsetX = this.position.x;
        this.positionOffsetY = this.position.y;
        this.positionOffsetZ = this.position.z;
        this.scaleValueX = this.scale.x;
        this.scaleValueY = this.scale.y;
        this.scaleValueZ = this.scale.z;
    }

    public void copyFrom(BoneSnapshot snapshot) {
        this.scaleValueX = snapshot.scaleValueX;
        this.scaleValueY = snapshot.scaleValueY;
        this.scaleValueZ = snapshot.scaleValueZ;

        this.positionOffsetX = snapshot.positionOffsetX;
        this.positionOffsetY = snapshot.positionOffsetY;
        this.positionOffsetZ = snapshot.positionOffsetZ;

        this.rotationValueX = snapshot.rotationValueX;
        this.rotationValueY = snapshot.rotationValueY;
        this.rotationValueZ = snapshot.rotationValueZ;

        this.hidden = snapshot.hidden;
        this.childrenHidden = snapshot.childrenHidden;

        this.position.set(snapshot.position);
        this.rotation.set(snapshot.rotation);
        this.scale.set(snapshot.scale);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof BoneSnapshot that) {
            return this.name.equals(that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
