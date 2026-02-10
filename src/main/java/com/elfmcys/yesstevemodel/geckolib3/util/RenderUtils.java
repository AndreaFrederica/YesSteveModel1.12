package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

public final class RenderUtils {
    public static void translateMatrixToBone(GeoBone bone) {
        GlStateManager.translate(-bone.getPositionX() / 16f, bone.getPositionY() / 16f, bone.getPositionZ() / 16f);
    }

    public static void rotateMatrixAroundBone(GeoBone bone) {
        if (bone.getRotationZ() != 0.0F) {
            GlStateManager.rotate(bone.getRotationZ() * (180F / (float) Math.PI), 0, 0, 1);
        }
        if (bone.getRotationY() != 0.0F) {
            GlStateManager.rotate(bone.getRotationY() * (180F / (float) Math.PI), 0, 1, 0);
        }
        if (bone.getRotationX() != 0.0F) {
            GlStateManager.rotate(bone.getRotationX() * (180F / (float) Math.PI), 1, 0, 0);
        }
    }

    public static void scaleMatrixForBone(GeoBone bone) {
        GlStateManager.scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
    }

    public static void translateToPivotPoint(GeoBone bone) {
        GlStateManager.translate(bone.rotationPointX / 16f, bone.rotationPointY / 16f, bone.rotationPointZ / 16f);
    }

    public static void translateAwayFromPivotPoint(GeoBone bone) {
        GlStateManager.translate(-bone.rotationPointX / 16f, -bone.rotationPointY / 16f, -bone.rotationPointZ / 16f);
    }

    public static void prepMatrixForBone(GeoBone bone) {
        translateMatrixToBone(bone);
        translateToPivotPoint(bone);
        rotateMatrixAroundBone(bone);
        scaleMatrixForBone(bone);
        translateAwayFromPivotPoint(bone);
    }

    public static void translateToBones(List<GeoBone> bones) {
        int size = bones.size();
        for (int i = 0; i < size - 1; i++) {
            RenderUtils.prepMatrixForBone(bones.get(i));
        }
        GeoBone lastBone = bones.get(size - 1);
        RenderUtils.translateMatrixToBone(lastBone);
        RenderUtils.translateToPivotPoint(lastBone);
        RenderUtils.rotateMatrixAroundBone(lastBone);
        RenderUtils.scaleMatrixForBone(lastBone);
    }
}
