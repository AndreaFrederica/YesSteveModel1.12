package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.apache.commons.lang3.StringUtils;

public class ArmorPredicate implements IAnimationPredicate<CustomPlayerEntity> {

    private final EntityEquipmentSlot slot;

    public ArmorPredicate(EntityEquipmentSlot slot) {
        this.slot = slot;
    }

    @Override
    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event, ExpressionEvaluator<?> evaluator) {
        EntityLivingBase entity = event.getAnimatableEntity().getEntity();
        if (entity == null || (event.getAnimatableEntity() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (entity.getItemStackFromSlot(this.slot).isEmpty()) {
            return PlayState.STOP;
        }
        ConditionManager conditionManager = event.getAnimatableEntity().getModelConfig();
        if (conditionManager != null) {
            ConditionArmor conditionArmor = conditionManager.getArmor();
            if (conditionArmor != null) {
                String name = conditionArmor.doTest(entity, this.slot);
                if (StringUtils.isNoneBlank(name)) {
                    return IAnimationPredicate.playAnimationWithLoop(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        String defaultName = this.slot.getName() + ":default";
        if (event.getAnimatableEntity().getAnimation(defaultName) != null) {
            return IAnimationPredicate.playAnimationWithLoop(event, defaultName, ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }
}
