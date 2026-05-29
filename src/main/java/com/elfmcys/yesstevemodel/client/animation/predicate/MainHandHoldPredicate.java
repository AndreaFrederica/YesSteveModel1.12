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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.apache.commons.lang3.StringUtils;

public class MainHandHoldPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase entity = event.getAnimatableEntity().getEntity();
        if (entity == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (!checkSwingAndUse(entity, EnumHand.MAIN_HAND)) {
            return PlayState.PAUSE;
        }
        ItemStack mainHandItem = entity.getHeldItem(EnumHand.MAIN_HAND);
        boolean isFishing = (entity instanceof EntityPlayer) && ((EntityPlayer) entity).fishEntity != null;
        if (isFishing) {
            return IAnimationPredicate.playAnimationWithValid(event, "hold_mainhand:fishing", ILoopType.EDefaultLoopTypes.LOOP, 0);
        }
        LivingEntityFrameState<?> positionTracker = event.getAnimatableEntity().getPositionTracker();
        if (positionTracker != null && !isSameItem(mainHandItem, positionTracker, EnumHand.MAIN_HAND)) {
            positionTracker.setHandItemsForAnimation(mainHandItem, EnumHand.MAIN_HAND);
            event.getController().stopTransition();
        }
        ConditionManager conditionManager = event.getAnimatableEntity().getModelConfig();
        if (conditionManager != null) {
            ConditionalHold conditionHold = conditionManager.getHoldMainhand();
            if (conditionHold != null) {
                String str = conditionHold.doTest(entity, EnumHand.MAIN_HAND);
                if (StringUtils.isNoneBlank(str)) {
                    return IAnimationPredicate.playAnimationWithValid(event, str, ILoopType.EDefaultLoopTypes.LOOP, 0);
                }
            }
        }
        return PlayState.STOP;
    }

    private boolean isSameItem(ItemStack itemStack, LivingEntityFrameState<?> frameState, EnumHand hand) {
        ItemStack preItem = frameState.getHandItemsForAnimation(hand);
        if (preItem.isItemDamaged()) {
            return ItemStack.areItemsEqual(itemStack, preItem);
        }
        return ItemStack.areItemStacksEqual(itemStack, preItem);
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
