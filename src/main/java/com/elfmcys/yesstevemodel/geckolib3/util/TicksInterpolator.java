package com.elfmcys.yesstevemodel.geckolib3.util;

public class TicksInterpolator implements IInterpolable {
    private final float tickDuration;

    public TicksInterpolator(float duration) {
        this.tickDuration = duration * 20.0f;
    }

    @Override
    public float interpolate(float value) {
        if (this.tickDuration != 0.0f) {
            return value / this.tickDuration;
        }
        return 1.0f;
    }

    @Override
    public float getProgress() {
        return this.tickDuration;
    }
}