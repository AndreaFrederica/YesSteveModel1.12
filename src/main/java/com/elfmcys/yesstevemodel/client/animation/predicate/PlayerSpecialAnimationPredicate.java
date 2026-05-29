package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerSpecialAnimationPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return null;
        }
        // Parcool compat - stub for 1.12
        String str = null;
        if (str != null && event.getAnimatableEntity().getAnimation(str) != null) {
            return IAnimationPredicate.predicate(event, str);
        }
        return PlayState.STOP;
    }
}
