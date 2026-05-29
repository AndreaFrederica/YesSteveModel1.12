package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.Entity;

public class MovementAnimationPredicate implements IAnimationPredicate<GeckoVehicleEntity> {

    public static final String[] ANIMATION_NAMES = {"forward", "idle"};

    @Override
    public PlayState predicate(AnimationEvent<GeckoVehicleEntity> event, ExpressionEvaluator<?> evaluator) {
        Entity entity = event.getAnimatableEntity().getEntity();
        if (entity == null) {
            return PlayState.STOP;
        }
        double dx = entity.motionX;
        double dz = entity.motionZ;
        if (Math.sqrt(dx * dx + dz * dz) > 0.05d) {
            return IAnimationPredicate.predicate(event, "forward");
        }
        return IAnimationPredicate.predicate(event, "idle");
    }
}
