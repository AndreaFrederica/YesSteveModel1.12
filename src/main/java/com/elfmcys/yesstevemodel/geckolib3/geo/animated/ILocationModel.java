package com.elfmcys.yesstevemodel.geckolib3.geo.animated;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.ILocationBone;

import java.util.Collections;
import java.util.List;

public interface ILocationModel {
    default List<? extends ILocationBone> leftHandBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> rightHandBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> leftShoulderBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> rightShoulderBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> leftWaistBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> rightWaistBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> elytraBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> backpackBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> tacPistolBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> tacRifleBones() {
        return Collections.emptyList();
    }

    default List<? extends ILocationBone> headBones() {
        return Collections.emptyList();
    }
}
