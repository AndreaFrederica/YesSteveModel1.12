package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.StringUtils;

public class PlayerAnimationPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null || event.getAnimatableEntity() instanceof IPreviewAnimatable || player.isElytraFlying() || player.isInWater()) {
            return PlayState.STOP;
        }
        String carryOnType = CarryOnCompat.getCarryOnString(player);
        if (StringUtils.isNoneBlank(carryOnType)) {
            return IAnimationPredicate.playAnimationWithLoop(event, "carryon:" + carryOnType, ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }
}
