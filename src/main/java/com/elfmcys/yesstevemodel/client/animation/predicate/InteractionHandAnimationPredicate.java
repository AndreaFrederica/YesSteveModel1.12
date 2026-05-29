package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalUse;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import org.apache.commons.lang3.StringUtils;

public class InteractionHandAnimationPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase livingEntity = event.getAnimatableEntity().getEntity();
        if (livingEntity == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (livingEntity.isHandActive() && !livingEntity.isPlayerSleeping()) {
            ConditionManager conditionManager = event.getAnimatableEntity().getModelConfig();
            if (livingEntity.getActiveHand() == EnumHand.MAIN_HAND) {
                if (conditionManager != null) {
                    ConditionalUse conditionUse = conditionManager.getUseMainhand();
                    if (conditionUse != null) {
                        String str = conditionUse.doTest(livingEntity, EnumHand.MAIN_HAND);
                        if (StringUtils.isNoneBlank(str)) {
                            return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, 0);
                        }
                    }
                }
                return IAnimationPredicate.playAnimationWithValid(event, "use_mainhand", ILoopType.EDefaultLoopTypes.LOOP, 0);
            }
            if (conditionManager != null) {
                ConditionalUse conditionUse2 = conditionManager.getUseOffhand();
                if (conditionUse2 != null) {
                    String str2 = conditionUse2.doTest(livingEntity, EnumHand.OFF_HAND);
                    if (StringUtils.isNoneBlank(str2)) {
                        return IAnimationPredicate.playAnimationWithValid(event, str2, ILoopType.EDefaultLoopTypes.LOOP, 0);
                    }
                }
            }
            return IAnimationPredicate.playAnimationWithValid(event, "use_offhand", ILoopType.EDefaultLoopTypes.LOOP, 0);
        }
        return PlayState.STOP;
    }
}
