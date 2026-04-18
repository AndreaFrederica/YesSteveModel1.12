package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.ILocationBone;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import net.minecraft.client.renderer.GlStateManager;

import java.util.List;

/**
 * 注意：这里都是操控 GL Matrix 的！<br>
 * 不要和 {@link com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer#MATRIX_STACK} 混为一谈！
 */
public final class RenderUtils {
    public static void translateMatrixToBone(ILocationBone bone) {
        GlStateManager.translate(-bone.getPositionX() / 16f, bone.getPositionY() / 16f, bone.getPositionZ() / 16f);
    }

    public static void rotateMatrixAroundBone(ILocationBone bone) {
        if (bone.getRotationZ() != 0.0F) {
            GlStateManager.rotate(MathUtil.radiansToDegrees(bone.getRotationZ()), 0, 0, 1);
        }
        if (bone.getRotationY() != 0.0F) {
            GlStateManager.rotate(MathUtil.radiansToDegrees(bone.getRotationY()), 0, 1, 0);
        }
        if (bone.getRotationX() != 0.0F) {
            GlStateManager.rotate(MathUtil.radiansToDegrees(bone.getRotationX()), 1, 0, 0);
        }
    }

    /**
     * 如果缩放全为 0，则返回 true
     */
    public static boolean scaleMatrixForBone(ILocationBone bone) {
        float scaleX = bone.getScaleX();
        float scaleY = bone.getScaleY();
        float scaleZ = bone.getScaleZ();
        GlStateManager.scale(scaleX, scaleY, scaleZ);
        return scaleX == 0 && scaleY == 0 && scaleZ == 0;
    }

    public static void translateToPivotPoint(ILocationBone bone) {
        GlStateManager.translate(bone.getPivotX() / 16f, bone.getPivotY() / 16f, bone.getPivotZ() / 16f);
    }

    public static void translateAwayFromPivotPoint(ILocationBone bone) {
        GlStateManager.translate(-bone.getPivotX() / 16f, -bone.getPivotY() / 16f, -bone.getPivotZ() / 16f);
    }

    public static void translateAndRotateMatrixForBone(ILocationBone bone) {
        translateToPivotPoint(bone);
        rotateMatrixAroundBone(bone);
    }

    /**
     * 如果缩放为 0，则返回 true
     */
    public static boolean prepMatrixForBone(ILocationBone bone) {
        translateMatrixToBone(bone);
        translateToPivotPoint(bone);
        rotateMatrixAroundBone(bone);
        boolean scaleAllIsZero = scaleMatrixForBone(bone);
        translateAwayFromPivotPoint(bone);
        return scaleAllIsZero;
    }

    public static boolean prepMatrixForLocator(List<? extends ILocationBone> locatorHierarchy) {
        boolean scaleCheck = false;
        for (int i = 0; i < locatorHierarchy.size() - 1; i++) {
            boolean result = RenderUtils.prepMatrixForBone(locatorHierarchy.get(i));
            if (result) {
                scaleCheck = true;
            }
        }
        ILocationBone lastBone = locatorHierarchy.get(locatorHierarchy.size() - 1);
        RenderUtils.translateMatrixToBone(lastBone);
        RenderUtils.translateToPivotPoint(lastBone);
        RenderUtils.rotateMatrixAroundBone(lastBone);
        RenderUtils.scaleMatrixForBone(lastBone);
        return scaleCheck;
    }
}
