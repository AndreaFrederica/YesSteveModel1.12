package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionalHold;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.LivingEntityFrameState;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.apache.commons.lang3.StringUtils;

public class OffHandHoldPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase entity = event.getAnimatableEntity().getEntity();
        if (entity == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (!checkSwingAndUse(entity, EnumHand.OFF_HAND)) {
            return PlayState.PAUSE;
        }
        ItemStack itemInHand = entity.getHeldItem(EnumHand.OFF_HAND);
        LivingEntityFrameState<?> positionTracker = event.getAnimatableEntity().getPositionTracker();
        if (positionTracker != null && !isSameItem(itemInHand, positionTracker, EnumHand.OFF_HAND)) {
            positionTracker.setHandItemsForAnimation(itemInHand, EnumHand.OFF_HAND);
            event.getController().stopTransition();
        }
        ConditionManager conditionManager = event.getAnimatableEntity().getModelConfig();
        if (conditionManager != null) {
            ConditionalHold conditionHold = conditionManager.getHoldOffhand();
            if (conditionHold != null) {
                String str = conditionHold.doTest(entity, EnumHand.OFF_HAND);
                if (StringUtils.isNoneBlank(str)) {
                    return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, 0);
                }
            }
        }
        return PlayState.STOP;
    }

    private boolean isSameItem(ItemStack stack, LivingEntityFrameState<?> frameState, EnumHand hand) {
        ItemStack preItem = frameState.getHandItemsForAnimation(hand);
        if (preItem.isItemDamaged()) {
            return ItemStack.areItemsEqual(stack, preItem);
        }
        return ItemStack.areItemStacksEqual(stack, preItem);
    }

    private boolean checkSwingAndUse(EntityLivingBase entity, EnumHand hand) {
        if (entity.isSwingInProgress && entity.getHeldItemOffhand() != null && hand == EnumHand.OFF_HAND) {
            return false;
        }
        if (entity.isSwingInProgress && hand == EnumHand.MAIN_HAND) {
            return false;
        }
        return !entity.isHandActive() || entity.getActiveHand() != hand;
    }
}
