package com.elfmcys.yesstevemodel.geckolib3.util;

public interface IInterpolable {
    float interpolate(float value);

    float getProgress();

    default IInterpolable asInterpolator() {
        return this;
    }
}