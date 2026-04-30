package com.elfmcys.yesstevemodel.client.util;

import com.elfmcys.yesstevemodel.client.compat.CameraCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

/**
 * 用于补全一部分旧版缺失的内容
 */
public final class EntityUtil {
    // 旧版没有分离的视角旋转获取方法，但是在 getLook 对 getVectorForRotation 的调用中可见踪迹

    public static float getViewXRot(Entity entity, float partialTick) {
        /// {@link EntityPlayerSP#getLook(float)}
        if (entity instanceof EntityPlayerSP) {
            return entity.rotationPitch;
        }
        /// {@link Entity#getLook(float)}
        return partialTick == 1.0F ? entity.rotationPitch : Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTick);
    }

    public static float getViewYRot(Entity entity, float partialTick) {
        /// {@link EntityPlayerSP#getLook(float)}
        if (entity instanceof EntityPlayerSP clientPlayer) {
            return clientPlayer.rotationYaw;
        }
        /// {@link EntityLivingBase#getLook(float)}
        if (entity instanceof EntityLivingBase livingEntity) {
            return partialTick == 1.0F ? livingEntity.rotationYawHead : Interpolations.lerp(livingEntity.prevRotationYawHead, livingEntity.rotationYawHead, partialTick);
        }
        /// {@link Entity#getLook(float)}
        return partialTick == 1.0F ? entity.rotationYaw : Interpolations.lerp(entity.prevRotationYaw, entity.rotationYaw, partialTick);
    }

    public static float getCameraXRot(Minecraft mc, float partialTick) {
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return 0.0F;

        float xRot = getViewXRot(entity, partialTick);
        return mc.gameSettings.thirdPersonView == 2 && !mc.gameSettings.debugCamEnable ? -xRot : xRot;
    }

    public static float getCameraYRot(Minecraft mc, float partialTick) {
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return 0.0F;

        float yRot = getViewYRot(entity, partialTick);
        return mc.gameSettings.thirdPersonView == 2 && !mc.gameSettings.debugCamEnable ? yRot + 180.0F : yRot;
    }

    public static float getYawSpeed(Entity entity) {
        return 20 * (entity.rotationYaw - entity.prevRotationYaw);
    }

    public static float getGroundSpeed(Entity entity) {
        return 20 * MathHelper.sqrt((float) (entity.motionX * entity.motionX + entity.motionZ * entity.motionZ));
    }

    public static float getVerticalSpeed(Entity entity) {
        return 20 * (float) (entity.posY - entity.prevPosY);
    }

    /// {@link net.minecraft.client.renderer.EntityRenderer#orientCamera(float)}
    @SuppressWarnings("JavadocReference")
    public static Vec3d getCameraPosition(Minecraft mc, float partialTicks) {
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return Vec3d.ZERO;

        double eyeX = Interpolations.lerp(entity.prevPosX, entity.posX, partialTicks);
        double eyeY = Interpolations.lerp(entity.prevPosY, entity.posY, partialTicks) + entity.getEyeHeight();
        double eyeZ = Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTicks);
        // 睡觉
        if (entity instanceof EntityLivingBase living && living.isPlayerSleeping()) {
            eyeY += 1.0D;
        }
        Vec3d eyePos = new Vec3d(eyeX, eyeY, eyeZ);

        /*
        第一人称
         */

        if (mc.gameSettings.thirdPersonView == 0) {
            return eyePos;
        }

        // 似乎是没有用的分支，保留对齐原版
        if (mc.gameSettings.debugCamEnable) {
            // 在调试模式下，原版会跳过所有防穿墙和视角旋转
            // 镜头会死死地固定在玩家眼部绝对坐标的 Z 轴正方向 +4.0 的位置
            return new Vec3d(eyeX, eyeY, eyeZ + 4.0D);
        }

        /*
        第三人称
         */

        float yaw = entity.rotationYaw;
        float pitch = entity.rotationPitch;
        // 正面视角
        if (mc.gameSettings.thirdPersonView == 2) {
            pitch += 180.0F;
        }
        float yawRadians = MathUtil.degreesToRadians(yaw);
        float pitchRadians = MathUtil.degreesToRadians(pitch);

        // 最大拉远距离
        double cameraDistance = 4.0D;

        // 计算第三人称向外拉远的基准方向向量
        double offsetX = (double) (-MathHelper.sin(yawRadians) * MathHelper.cos(pitchRadians)) * cameraDistance;
        double offsetY = (double) (-MathHelper.sin(pitchRadians)) * cameraDistance;
        double offsetZ = (double) (MathHelper.cos(yawRadians) * MathHelper.cos(pitchRadians)) * cameraDistance;

        // 射线防穿墙检测
        for (int i = 0; i < 8; ++i) {
            // 生成一个边长为 0.2 的立方体的 8 个角的偏移量 (-0.1 或 0.1)
            float boxOffsetX = (float) ((i & 1) * 2 - 1) * 0.1F;
            float boxOffsetY = (float) ((i >> 1 & 1) * 2 - 1) * 0.1F;
            float boxOffsetZ = (float) ((i >> 2 & 1) * 2 - 1) * 0.1F;

            // 起点：眼部的边角
            Vec3d startPos = new Vec3d(
                    eyeX + (double) boxOffsetX,
                    eyeY + (double) boxOffsetY,
                    eyeZ + (double) boxOffsetZ
            );

            // 终点：拉远后相机的边角
            Vec3d endPos = new Vec3d(
                    CameraCompat.cameraOrientation() ? eyeX - offsetX + (double) boxOffsetX :
                            eyeX - offsetX + (double) boxOffsetX + (double) boxOffsetZ, // 原版的一处 BUG，为了还原也加上了
                    eyeY - offsetY + (double) boxOffsetY,
                    eyeZ - offsetZ + (double) boxOffsetZ
            );

            // 发射检测射线
            RayTraceResult rayTrace = CameraCompat.thirdPersonIgnoresNonSolidBlocks() ?
                    /// {@link mod.acgaming.universaltweaks.tweaks.entities.playerf5.mixin.UTEntityRendererMixin}
                    mc.world.rayTraceBlocks(startPos, endPos, false, true, true) :
                    mc.world.rayTraceBlocks(startPos, endPos);
            if (rayTrace != null) {
                // 如果撞墙，算出眼部中心点到撞墙点的距离
                double hitDistance = rayTrace.hitVec.distanceTo(eyePos);
                // 将相机推进，直到刚好不穿墙
                cameraDistance = Math.min(hitDistance, cameraDistance);
            }
        }

        // 最终坐标
        return new Vec3d(
                eyeX - (double) (-MathHelper.sin(yawRadians) * MathHelper.cos(pitchRadians)) * cameraDistance,
                eyeY - (double) (-MathHelper.sin(pitchRadians)) * cameraDistance,
                eyeZ - (double) (MathHelper.cos(yawRadians) * MathHelper.cos(pitchRadians)) * cameraDistance
        );
    }
}
