package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import javax.annotation.Nonnull;

public class AnimationManagerPredicate implements IAnimationPredicate<CustomPlayerEntity> {

    public static final AnimationManagerPredicate INSTANCE = new AnimationManagerPredicate();

    @Nonnull
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        return AnimationManager.getInstance().predicateMain(event);
    }
}
