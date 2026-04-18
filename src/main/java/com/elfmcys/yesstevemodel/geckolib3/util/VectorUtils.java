package com.elfmcys.yesstevemodel.geckolib3.util;

import javax.vecmath.*;

@SuppressWarnings("unused")
public final class VectorUtils {
    public static Vector3f convertDoubleToFloat(double[] array) {
        return new Vector3f((float) array[0], (float) array[1], (float) array[2]);
    }

    public static Vector3d convertFloatToDouble(float[] array) {
        return new Vector3d(array[0], array[1], array[2]);
    }

    public static Vector3f scale(Vector3f vector, float x, float y, float z) {
        vector.x *= x;
        vector.y *= y;
        vector.z *= z;
        return vector;
    }

    public static Vector3d scale(Vector3d vector, double x, double y, double z) {
        vector.x *= x;
        vector.y *= y;
        vector.z *= z;
        return vector;
    }

    private static final ThreadLocal<Matrix4f> TEMP_MATRIX = ThreadLocal.withInitial(Matrix4f::new);
    private static final ThreadLocal<Vector3f> TEMP_VEC = ThreadLocal.withInitial(Vector3f::new);

    public static void rotateAround(Matrix4f matrix, Quat4f quat, float px, float py, float pz) {
        Matrix4f tempM = TEMP_MATRIX.get();
        Vector3f tempV = TEMP_VEC.get();

        tempM.setIdentity();
        tempV.set(px, py, pz);
        tempM.setTranslation(tempV);
        matrix.mul(tempM);

        tempM.setIdentity();
        tempM.setRotation(quat);
        matrix.mul(tempM);

        tempM.setIdentity();
        tempV.set(-px, -py, -pz);
        tempM.setTranslation(tempV);
        matrix.mul(tempM);
    }

    private static final ThreadLocal<AxisAngle4f> TEMP_AXIS = ThreadLocal.withInitial(AxisAngle4f::new);
    private static final ThreadLocal<Quat4f> TEMP_QUAT = ThreadLocal.withInitial(Quat4f::new);

    public static void rotateZYX(Quat4f dest, float rz, float ry, float rx) {
        AxisAngle4f tempA = TEMP_AXIS.get();
        Quat4f tempQ = TEMP_QUAT.get();

        tempA.set(0, 0, 1, rz);
        dest.set(tempA);

        tempA.set(0, 1, 0, ry);
        tempQ.set(tempA);
        dest.mul(tempQ);

        tempA.set(1, 0, 0, rx);
        tempQ.set(tempA);
        dest.mul(tempQ);
    }

    private static final ThreadLocal<Point3f> TEMP_POINT = ThreadLocal.withInitial(Point3f::new);

    public static void mulPosition(Matrix4f matrix, Vector3f src, Vector3f dest) {
        Point3f tempP = TEMP_POINT.get();
        tempP.set(src);
        matrix.transform(tempP);
        dest.set(tempP);
    }

    public static void mulPosition(Matrix4f matrix, Vector3f vector) {
        mulPosition(matrix, vector, vector);
    }
}
