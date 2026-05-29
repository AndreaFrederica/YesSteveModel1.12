package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;

public final class AnimationFormatValidator {

    private AnimationFormatValidator() {
    }

    public static boolean validate(AnimationEvent<?> event, String animationName, int version) {
        if (version >= 19) {
            return true;
        }
        Animation animation = event.getAnimatableEntity().getAnimation(animationName);
        return animation != null && animation.isFromPrimaryAssembly;
    }
}