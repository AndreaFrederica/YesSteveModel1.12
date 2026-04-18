package com.elfmcys.yesstevemodel.geckolib3.core.util;

import javax.vecmath.Vector3f;

public final class MathUtil {
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180F;
    private static final float RADIANS_TO_DEGREES = 180F / (float) Math.PI;

    public static Vector3f lerpValues(double percentCompleted, Vector3f begin, Vector3f end) {
        return new Vector3f(lerpValues(percentCompleted, begin.getX(), end.getX()),
                lerpValues(percentCompleted, begin.getY(), end.getY()),
                lerpValues(percentCompleted, begin.getZ(), end.getZ()));
    }

    public static float lerpValues(double percentCompleted, double startValue, double endValue) {
        return (float) (startValue + percentCompleted * (endValue - startValue));
    }

    public static Vector3f catmullRom(double percentCompleted, Vector3f left, Vector3f begin, Vector3f end, Vector3f right) {
        return new Vector3f(catmullRom(percentCompleted, left.getX(), begin.getX(), end.getX(), right.getX()),
                catmullRom(percentCompleted, left.getY(), begin.getY(), end.getY(), right.getY()),
                catmullRom(percentCompleted, left.getZ(), begin.getZ(), end.getZ(), right.getZ()));
    }

    public static float catmullRom(double percent, double left, double begin, double end, double right) {
        double v0 = (end - left) * 0.5;
        double v1 = (right - begin) * 0.5;
        double t2 = percent * percent;
        double t3 = percent * t2;
        return (float) ((2 * begin - 2 * end + v0 + v1) * t3 + (-3 * begin + 3 * end - 2 * v0 - v1) * t2 + v0 * percent + begin);
    }

    public static float degreesToRadians(float degrees) {
        return degrees * DEGREES_TO_RADIANS;
    }

    public static float radiansToDegrees(float degrees) {
        return degrees * RADIANS_TO_DEGREES;
    }
}
