package com.elfmcys.yesstevemodel.geckolib3.geo.render.built;

import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.*;
import com.elfmcys.yesstevemodel.geckolib3.util.VectorUtils;
import com.github.bsideup.jabel.Desugar;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

@Desugar
public record GeoMesh(
        int cubeCount, int[] faces,
        Vector3f[] position, Vector3f[] dx, Vector3f[] dy, Vector3f[] dz,
        float[] u0, float[] v0, float[] u1, float[] v1
) {
    /**
     * Down, Up, North, South, West, East
     */
    public static final int FACE_COUNT = 6;

    public static class GeoMeshBuilder {
        private static final float DEGREES_TO_RADIANS = 0.017453292519943295f;

        private final Matrix4f poseMatrix = new Matrix4f();
        private final Quat4f tempQuat = new Quat4f();
        private final int cubeCount;

        private final int[] FACES;
        private final Vector3f[] POSITION;
        private final Vector3f[] DX;
        private final Vector3f[] DY;
        private final Vector3f[] DZ;
        private final float[] U0;
        private final float[] V0;
        private final float[] U1;
        private final float[] V1;

        private int index = 0;

        public GeoMeshBuilder(int cubeCount) {
            this.cubeCount = cubeCount;
            this.FACES = new int[cubeCount];
            this.POSITION = new Vector3f[cubeCount];
            this.DX = new Vector3f[cubeCount];
            this.DY = new Vector3f[cubeCount];
            this.DZ = new Vector3f[cubeCount];
            this.U0 = new float[cubeCount * FACE_COUNT];
            this.V0 = new float[cubeCount * FACE_COUNT];
            this.U1 = new float[cubeCount * FACE_COUNT];
            this.V1 = new float[cubeCount * FACE_COUNT];

            for (int i = 0; i < cubeCount; i++) {
                this.POSITION[i] = new Vector3f();
                this.DX[i] = new Vector3f();
                this.DY[i] = new Vector3f();
                this.DZ[i] = new Vector3f();
            }
        }

        public void addCube(Cube cubeIn, ModelProperties properties, Double boneInflate, Boolean mirror) {
            float textureHeight = properties.getTextureHeight().floatValue();
            float textureWidth = properties.getTextureWidth().floatValue();

            float inflate = cubeIn.getInflate() == null ? boneInflate.floatValue() : cubeIn.getInflate().floatValue();
            inflate /= 16f;

            Vector3f size = VectorUtils.convertDoubleToFloat(cubeIn.getSize());
            size.scale(0.0625f);
            Vector3f origin = VectorUtils.convertDoubleToFloat(cubeIn.getOrigin());
            origin.scale(0.0625f);
            float diff = Math.max(0.001f, size.x + inflate * 2);

            Vector3f P1 = new Vector3f(-(origin.x + size.x) - inflate, origin.y - inflate, origin.z - inflate);
            boolean finallyMirror = false;
            if (cubeIn.getMirror() != null) {
                finallyMirror = cubeIn.getMirror();
            } else if (mirror != null) {
                finallyMirror = mirror;
            }
            if (finallyMirror) {
                P1.x += diff;
                diff = -diff;
            }

            Vector3f rotation = VectorUtils.convertDoubleToFloat(cubeIn.getRotation());

            VectorUtils.scale(rotation, -DEGREES_TO_RADIANS, -DEGREES_TO_RADIANS, DEGREES_TO_RADIANS);

            Vector3f pivot = VectorUtils.convertDoubleToFloat(cubeIn.getPivot());
            VectorUtils.scale(pivot, -0.0625f, 0.0625f, 0.0625f);

            this.poseMatrix.setIdentity();
            VectorUtils.rotateZYX(this.tempQuat, rotation.getZ(), rotation.getY(), rotation.getX());
            VectorUtils.rotateAround(this.poseMatrix, this.tempQuat, pivot.getX(), pivot.getY(), pivot.getZ());
            VectorUtils.mulPosition(this.poseMatrix, P1);

            this.POSITION[this.index].set(P1);

            this.DX[this.index].set(this.poseMatrix.getM00() * diff, this.poseMatrix.getM10() * diff, this.poseMatrix.getM20() * diff);

            diff = Math.max(0.001f, size.y + inflate * 2);
            this.DY[this.index].set(this.poseMatrix.getM01() * diff, this.poseMatrix.getM11() * diff, this.poseMatrix.getM21() * diff);

            diff = Math.max(0.001f, size.z + inflate * 2);
            this.DZ[this.index].set(this.poseMatrix.getM02() * diff, this.poseMatrix.getM12() * diff, this.poseMatrix.getM22() * diff);

            UvUnion uvUnion = cubeIn.getUv();
            boolean isBoxUV = uvUnion.isBoxUV;

            int faces = finallyMirror ? 0b1000000 : 0;
            int faceIndex = this.index * FACE_COUNT;
            if (!isBoxUV) {
                UvFaces faceUV = uvUnion.faceUV;
                FaceUv west = faceUV.getWest();
                FaceUv east = faceUV.getEast();
                FaceUv north = faceUV.getNorth();
                FaceUv south = faceUV.getSouth();
                FaceUv up = faceUV.getUp();
                FaceUv down = faceUV.getDown();

                if (down != null) {
                    faces |= 0b000001;
                    double[] uv = down.getUv();
                    double[] uvSize = down.getUvSize();

                    this.U0[faceIndex] = (float) uv[0] / textureWidth;
                    this.V0[faceIndex] = (float) uv[1] / textureHeight;
                    this.U1[faceIndex] = ((float) uv[0] + (float) uvSize[0]) / textureWidth;
                    this.V1[faceIndex] = ((float) uv[1] + (float) uvSize[1]) / textureHeight;
                }
                if (up != null) {
                    faces |= 0b000010;
                    double[] uv = up.getUv();
                    double[] uvSize = up.getUvSize();

                    this.U0[faceIndex + 1] = (float) uv[0] / textureWidth;
                    this.V0[faceIndex + 1] = (float) uv[1] / textureHeight;
                    this.U1[faceIndex + 1] = ((float) uv[0] + (float) uvSize[0]) / textureWidth;
                    this.V1[faceIndex + 1] = ((float) uv[1] + (float) uvSize[1]) / textureHeight;
                }
                if (north != null) {
                    faces |= 0b000100;
                    double[] uv = north.getUv();
                    double[] uvSize = north.getUvSize();

                    this.U0[faceIndex + 2] = (float) uv[0] / textureWidth;
                    this.V0[faceIndex + 2] = (float) uv[1] / textureHeight;
                    this.U1[faceIndex + 2] = ((float) uv[0] + (float) uvSize[0]) / textureWidth;
                    this.V1[faceIndex + 2] = ((float) uv[1] + (float) uvSize[1]) / textureHeight;
                }
                if (south != null) {
                    faces |= 0b001000;
                    double[] uv = south.getUv();
                    double[] uvSize = south.getUvSize();

                    this.U0[faceIndex + 3] = (float) uv[0] / textureWidth;
                    this.V0[faceIndex + 3] = (float) uv[1] / textureHeight;
                    this.U1[faceIndex + 3] = ((float) uv[0] + (float) uvSize[0]) / textureWidth;
                    this.V1[faceIndex + 3] = ((float) uv[1] + (float) uvSize[1]) / textureHeight;
                }
                if (west != null) {
                    faces |= 0b010000;
                    double[] uv = west.getUv();
                    double[] uvSize = west.getUvSize();

                    this.U0[faceIndex + 4] = (float) uv[0] / textureWidth;
                    this.V0[faceIndex + 4] = (float) uv[1] / textureHeight;
                    this.U1[faceIndex + 4] = ((float) uv[0] + (float) uvSize[0]) / textureWidth;
                    this.V1[faceIndex + 4] = ((float) uv[1] + (float) uvSize[1]) / textureHeight;
                }
                if (east != null) {
                    faces |= 0b100000;
                    double[] uv = east.getUv();
                    double[] uvSize = east.getUvSize();

                    this.U0[faceIndex + 5] = (float) uv[0] / textureWidth;
                    this.V0[faceIndex + 5] = (float) uv[1] / textureHeight;
                    this.U1[faceIndex + 5] = ((float) uv[0] + (float) uvSize[0]) / textureWidth;
                    this.V1[faceIndex + 5] = ((float) uv[1] + (float) uvSize[1]) / textureHeight;
                }
            } else {
                faces |= 0b111111;
                double[] uv = cubeIn.getUv().boxUVCoords;
                double[] rawSize = cubeIn.getSize();

                double sizeX = Math.floor(rawSize[0]);
                double sizeY = Math.floor(rawSize[1]);
                double sizeZ = Math.floor(rawSize[2]);

                float u0 = (float) (uv[0] + sizeZ + sizeX);
                float v0 = (float) (uv[1] + sizeZ);
                this.U0[faceIndex] = u0 / textureWidth;
                this.V0[faceIndex] = v0 / textureHeight;
                this.U1[faceIndex] = (u0 + (float) sizeX) / textureWidth;
                this.V1[faceIndex] = (v0 - (float) sizeZ) / textureHeight;

                u0 = (float) (uv[0] + sizeZ);
                v0 = (float) (uv[1]);
                this.U0[faceIndex + 1] = u0 / textureWidth;
                this.V0[faceIndex + 1] = v0 / textureHeight;
                this.U1[faceIndex + 1] = (u0 + (float) sizeX) / textureWidth;
                this.V1[faceIndex + 1] = (v0 + (float) sizeZ) / textureHeight;

                u0 = (float) (uv[0] + sizeZ);
                v0 = (float) (uv[1] + sizeZ);
                this.U0[faceIndex + 2] = u0 / textureWidth;
                this.V0[faceIndex + 2] = v0 / textureHeight;
                this.U1[faceIndex + 2] = (u0 + (float) sizeX) / textureWidth;
                this.V1[faceIndex + 2] = (v0 + (float) sizeY) / textureHeight;

                u0 = (float) (uv[0] + sizeZ + sizeX + sizeZ);
                v0 = (float) (uv[1] + sizeZ);
                this.U0[faceIndex + 3] = u0 / textureWidth;
                this.V0[faceIndex + 3] = v0 / textureHeight;
                this.U1[faceIndex + 3] = (u0 + (float) sizeX) / textureWidth;
                this.V1[faceIndex + 3] = (v0 + (float) sizeY) / textureHeight;

                u0 = (float) (uv[0] + sizeZ + sizeX);
                v0 = (float) (uv[1] + sizeZ);
                this.U0[faceIndex + 4] = u0 / textureWidth;
                this.V0[faceIndex + 4] = v0 / textureHeight;
                this.U1[faceIndex + 4] = (u0 + (float) sizeZ) / textureWidth;
                this.V1[faceIndex + 4] = (v0 + (float) sizeY) / textureHeight;

                u0 = (float) (uv[0]);
                v0 = (float) (uv[1] + sizeZ);
                this.U0[faceIndex + 5] = u0 / textureWidth;
                this.V0[faceIndex + 5] = v0 / textureHeight;
                this.U1[faceIndex + 5] = (u0 + (float) sizeZ) / textureWidth;
                this.V1[faceIndex + 5] = (v0 + (float) sizeY) / textureHeight;
            }
            this.FACES[this.index] = faces;

            this.index++;
        }

        public GeoMesh build() {
            return new GeoMesh(this.cubeCount, this.FACES, this.POSITION, this.DX, this.DY, this.DZ, this.U0, this.V0, this.U1, this.V1);
        }
    }

    public int faces(int index) {
        return this.faces[index];
    }

    public Vector3f position(int index) {
        return this.position[index];
    }

    public Vector3f dx(int index) {
        return this.dx[index];
    }

    public Vector3f dy(int index) {
        return this.dy[index];
    }

    public Vector3f dz(int index) {
        return this.dz[index];
    }

    public float u0(int index, int face) {
        return this.u0[index * FACE_COUNT + face];
    }

    public float v0(int index, int face) {
        return this.v0[index * FACE_COUNT + face];
    }

    public float u1(int index, int face) {
        return this.u1[index * FACE_COUNT + face];
    }

    public float v1(int index, int face) {
        return this.v1[index * FACE_COUNT + face];
    }

    public float downU0(int index) {
        return this.u0(index, 0);
    }

    public float downV0(int index) {
        return this.v0(index, 0);
    }

    public float downU1(int index) {
        return this.u1(index, 0);
    }

    public float downV1(int index) {
        return this.v1(index, 0);
    }

    public float upU0(int index) {
        return this.u0(index, 1);
    }

    public float upV0(int index) {
        return this.v0(index, 1);
    }

    public float upU1(int index) {
        return this.u1(index, 1);
    }

    public float upV1(int index) {
        return this.v1(index, 1);
    }

    public float northU0(int index) {
        return this.u0(index, 2);
    }

    public float northV0(int index) {
        return this.v0(index, 2);
    }

    public float northU1(int index) {
        return this.u1(index, 2);
    }

    public float northV1(int index) {
        return this.v1(index, 2);
    }

    public float southU0(int index) {
        return this.u0(index, 3);
    }

    public float southV0(int index) {
        return this.v0(index, 3);
    }

    public float southU1(int index) {
        return this.u1(index, 3);
    }

    public float southV1(int index) {
        return this.v1(index, 3);
    }

    public float westU0(int index) {
        return this.u0(index, 4);
    }

    public float westV0(int index) {
        return this.v0(index, 4);
    }

    public float westU1(int index) {
        return this.u1(index, 4);
    }

    public float westV1(int index) {
        return this.v1(index, 4);
    }

    public float eastU0(int index) {
        return this.u0(index, 5);
    }

    public float eastV0(int index) {
        return this.v0(index, 5);
    }

    public float eastU1(int index) {
        return this.u1(index, 5);
    }

    public float eastV1(int index) {
        return this.v1(index, 5);
    }
}
