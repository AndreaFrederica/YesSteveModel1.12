package com.elfmcys.yesstevemodel.geckolib3.core.processor;

import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;

import javax.vecmath.Vector3f;

public interface IBone extends ILocationBone {
    void setRotationX(float value);

    void setRotationY(float value);

    void setRotationZ(float value);

    void setPositionX(float value);

    void setPositionY(float value);

    void setPositionZ(float value);

    void setScaleX(float value);

    void setScaleY(float value);

    void setScaleZ(float value);

    boolean isHidden();

    void setHidden(boolean hidden);

    boolean cubesAreHidden();

    boolean childBonesAreHiddenToo();

    void setCubesHidden(boolean hidden);

    void setHidden(boolean selfHidden, boolean skipChildRendering);

    BoneSnapshot getInitialSnapshot();

    String getName();

    Vector3f getInitialRotation();

    int getBoneId();

    default boolean isTrackingXform() {
        return false;
    }

    default void setTrackXform(boolean track) {
    }

    default float getPivotAbsX() {
        return getPivotX();
    }

    default float getPivotAbsY() {
        return getPivotY();
    }

    default float getPivotAbsZ() {
        return getPivotZ();
    }
}
