package com.elfmcys.yesstevemodel.client.compat.gun.common;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.gun.swarfare.SWarfareCompat;
import com.elfmcys.yesstevemodel.client.compat.gun.tacz.TacCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class ItemUseAnimationPredicate implements IAnimationPredicate<CustomPlayerEntity> {
    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase entity = event.getAnimatableEntity().getEntity();
        if (entity == null || event.getAnimatableEntity() instanceof IPreviewAnimatable) {
            return PlayState.STOP;
        }
        if (entity.isSwingInProgress || entity.isHandActive()) {
            return PlayState.STOP;
        }
        ItemStack stack = entity.getHeldItemMainhand();
        PlayState state = TacCompat.handleGunActionAnimState(stack, event);
        if (state == null) {
            state = SWarfareCompat.handleGunActionAnim(stack, event);
        }
        return state != null ? state : PlayState.STOP;
    }

    public static boolean isLoaded() {
        return TacCompat.isLoaded() || SWarfareCompat.isLoaded();
    }
}
