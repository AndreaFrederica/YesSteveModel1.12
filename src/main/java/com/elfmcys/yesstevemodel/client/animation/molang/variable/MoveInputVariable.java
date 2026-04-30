package com.elfmcys.yesstevemodel.client.animation.molang.variable;

import com.elfmcys.yesstevemodel.client.util.EntityUtil;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class MoveInputVariable {
    public static double getVertical(IContext<Entity> context) {
        Entity entity = context.entity();
        float partialTick = context.animationEvent().getPartialTick();

        // 求出当前移动的水平分量
        double x = Interpolations.lerp(entity.prevPosX, entity.posX, partialTick) - entity.prevPosX;
        double z = Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTick) - entity.prevPosZ;

        // 如果移动数值过小，那么认为没有一点，返回 0
        if (Math.sqrt(x * x + z * z) < 1.0E-4) {
            return 0;
        }

        // 计算移动角度和实体偏航角度，并作差计算出相对角度
        float moveAngleDeg = MathUtil.radiansToDegrees((float) MathHelper.atan2(z, x));
        float entityYawDeg = 90 - MathHelper.wrapDegrees(-EntityUtil.getViewYRot(entity, partialTick));
        float relativeAnglesDeg = MathHelper.wrapDegrees(moveAngleDeg - entityYawDeg);

        return MathHelper.cos(MathUtil.degreesToRadians(relativeAnglesDeg));
    }

    public static double getHorizontal(IContext<Entity> context) {
        Entity entity = context.entity();
        float partialTick = context.animationEvent().getPartialTick();

        // 求出当前移动的水平分量
        double x = Interpolations.lerp(entity.prevPosX, entity.posX, partialTick) - entity.prevPosX;
        double z = Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTick) - entity.prevPosZ;

        // 如果移动数值过小，那么认为没有一点，返回 0
        if (Math.sqrt(x * x + z * z) < 1.0E-4) {
            return 0;
        }

        // 计算移动角度和实体偏航角度，并作差计算出相对角度
        float moveAngleDeg = MathUtil.radiansToDegrees((float) MathHelper.atan2(z, x));
        float entityYawDeg = 90 - MathHelper.wrapDegrees(-EntityUtil.getViewYRot(entity, partialTick));
        float relativeAnglesDeg = MathHelper.wrapDegrees(moveAngleDeg - entityYawDeg);

        return MathHelper.sin(MathUtil.degreesToRadians(relativeAnglesDeg));
    }
}
