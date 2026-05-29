package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.EntityLivingBase;

public class StopAnimationPredicate implements IAnimationPredicate<AnimatableEntity<? extends EntityLivingBase>> {

    public static final StopAnimationPredicate INSTANCE = new StopAnimationPredicate();

    @Override
    public PlayState predicate(AnimationEvent<AnimatableEntity<? extends EntityLivingBase>> event, ExpressionEvaluator<?> evaluator) {
        return PlayState.STOP;
    }
}
