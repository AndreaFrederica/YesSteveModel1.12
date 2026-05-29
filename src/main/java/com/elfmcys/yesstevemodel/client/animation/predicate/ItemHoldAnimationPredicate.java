package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalSwing;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import org.apache.commons.lang3.StringUtils;

public class ItemHoldAnimationPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase livingEntity = event.getAnimatableEntity().getEntity();
        if (livingEntity == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (livingEntity.isSwingInProgress && !livingEntity.isPlayerSleeping()) {
            if (livingEntity.swingProgressInt == 0 && event.getAnimatableEntity().getPositionTracker().markProcessed(1)) {
                event.getController().stopTransition();
            }
            ConditionManager conditionManager = event.getAnimatableEntity().getModelConfig();
            EnumHand swingHand = livingEntity.getHeldItemOffhand() != null ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            if (conditionManager != null) {
                ConditionalSwing conditionSwing = swingHand == EnumHand.MAIN_HAND ? conditionManager.getSwingMainhand() : conditionManager.getSwingOffhand();
                if (conditionSwing != null) {
                    String str2 = conditionSwing.doTest(livingEntity, swingHand);
                    if (StringUtils.isNoneBlank(str2)) {
                        return IAnimationPredicate.playAnimationWithValid(event, str2, ILoopType.EDefaultLoopTypes.PLAY_ONCE, 0);
                    }
                }
            }
            return IAnimationPredicate.playAnimationWithValid(event, swingHand == EnumHand.MAIN_HAND ? "swing_hand" : "swing_offhand", ILoopType.EDefaultLoopTypes.PLAY_ONCE, 0);
        }
        return PlayState.CONTINUE;
    }
}
