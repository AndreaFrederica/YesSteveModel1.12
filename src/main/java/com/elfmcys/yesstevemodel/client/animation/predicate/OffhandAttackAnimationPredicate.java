package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalPassenger;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.apache.commons.lang3.StringUtils;

public class OffhandAttackAnimationPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase livingEntity = event.getAnimatableEntity().getEntity();
        if (livingEntity == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        Entity firstPassenger = livingEntity.getPassengers().isEmpty() ? null : livingEntity.getPassengers().get(0);
        if (firstPassenger == null || !firstPassenger.isEntityAlive()) {
            return PlayState.STOP;
        }
        if (event.getAnimatableEntity().getModelConfig() != null) {
            ConditionalPassenger conditionPassenger = event.getAnimatableEntity().getModelConfig().getPassenger();
            if (conditionPassenger != null) {
                String str = conditionPassenger.doTest(livingEntity);
                if (StringUtils.isNoneBlank(str)) {
                    return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        return PlayState.STOP;
    }
}
