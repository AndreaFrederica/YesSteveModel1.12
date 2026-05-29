package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.util.accessors.ProjectileStateAccessor;
import net.minecraft.entity.Entity;

public class ProjectileAnimationPredicate implements IAnimationPredicate<GeckoProjectileEntity<?>> {

    public static final String[] ENVIRONMENT_STATES = {"water", "ground", "fly", "fire"};

    @Override
    public PlayState predicate(AnimationEvent<GeckoProjectileEntity<?>> event, ExpressionEvaluator<?> evaluator) {
        Entity projectile = event.getAnimatableEntity().getEntity();
        if (projectile == null) {
            return PlayState.STOP;
        }
        if (projectile.isInWater()) {
            return IAnimationPredicate.predicate(event, "water");
        }
        if (projectile.isBurning()) {
            return IAnimationPredicate.predicate(event, "fire");
        }
        if ((projectile instanceof ProjectileStateAccessor) && ((ProjectileStateAccessor) projectile).isInGround()) {
            return IAnimationPredicate.predicate(event, "ground");
        }
        return IAnimationPredicate.predicate(event, "air");
    }
}
