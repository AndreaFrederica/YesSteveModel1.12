package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.StringUtils;

public class EquipmentSlotAnimationPredicate implements IAnimationPredicate<PlayerGeoEntity> {

    private final EntityEquipmentSlot slot;

    public EquipmentSlotAnimationPredicate(EntityEquipmentSlot slot) {
        this.slot = slot;
    }

    @Override
    public PlayState predicate(AnimationEvent<PlayerGeoEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase entity = event.getAnimatableEntity().getEntity();
        if (entity == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (entity.getItemStackFromSlot(this.slot).isEmpty()) {
            return PlayState.STOP;
        }
        ConditionArmor conditionArmor = event.getAnimatableEntity().getArmModelProcessor();
        if (conditionArmor != null) {
            String name = conditionArmor.doTest(entity, this.slot);
            if (StringUtils.isNoneBlank(name)) {
                return IAnimationPredicate.playAnimationWithLoop(event, name, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        String str = this.slot.getName() + ":default";
        if (event.getAnimatableEntity().getAnimation(str) != null) {
            return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }
}
