package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionChair;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalVehicle;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.compat.CarryOnCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityBoat;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class LivingMovementAnimationPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        PlayState result = renderRidingAnimation(event);
        return result != null ? result : PlayState.STOP;
    }

    @Nullable
    public PlayState renderRidingAnimation(AnimationEvent<CustomPlayerEntity> event) {
        Entity vehicle;
        EntityPlayer player = (EntityPlayer) event.getAnimatableEntity().getEntity();
        if (player == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable) || (vehicle = player.getRidingEntity()) == null || !vehicle.isEntityAlive()) {
            return null;
        }
        ConditionManager conditionManager = event.getAnimatableEntity().getModelConfig();
        if (conditionManager != null) {
            ConditionalVehicle conditionVehicle = conditionManager.getVehicle();
            if (conditionVehicle != null) {
                String str3 = conditionVehicle.doTest(player);
                if (StringUtils.isNoneBlank(str3)) {
                    return IAnimationPredicate.playAnimationWithLoop(event, str3, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        if (vehicle instanceof EntityPig) {
            return IAnimationPredicate.playAnimationWithLoop(event, "ride_pig", ILoopType.EDefaultLoopTypes.LOOP);
        }
        if (vehicle instanceof EntityBoat) {
            return IAnimationPredicate.playAnimationWithLoop(event, "boat", ILoopType.EDefaultLoopTypes.LOOP);
        }
        boolean isCarrying = CarryOnCompat.isPlayerCarrying(player);
        if (isCarrying) {
            return IAnimationPredicate.playAnimationWithLoop(event, "carryon:princess", ILoopType.EDefaultLoopTypes.LOOP);
        }
        return IAnimationPredicate.playAnimationWithLoop(event, "sit", ILoopType.EDefaultLoopTypes.LOOP);
    }
}
