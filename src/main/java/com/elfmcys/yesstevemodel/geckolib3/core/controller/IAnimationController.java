package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public interface IAnimationController<T extends AnimatableEntity<?>> {
    String getName();

    @Nullable
    Animation getCurrentAnimation();

    default void setAnimation(AnimationBuilder builder) {
        if (builder == null || builder.getRawAnimationList().isEmpty()) {
            clearAnimation();
            return;
        }
        var animation = builder.getRawAnimationList().get(0);
        setAnimation(animation.animationName(), animation.loopType());
    }

    default void setAnimation(String animationName, ILoopType loopType) {
    }

    default void setAnimation(String animationName) {
    }

    default void setTransitionLengthTicks(float ticks) {
    }

    default void clearAnimation() {
    }

    default void reset() {
    }

    default void forEachTransform(Consumer<BoneTransformProvider> consumer) {
    }

    default boolean isDeprecatedMode() {
        return false;
    }

    default void init(List<BoneTopLevelSnapshot> list, Object2ReferenceMap<String, List<IValue>> object2ReferenceMap) {
    }

    default void process(AnimationEvent<T> event, ExpressionEvaluator<AnimationContext<?>> evaluator, boolean z) {
    }

    default String getCurrentAnimationName() {
        Animation anim = getCurrentAnimation();
        return anim != null ? anim.animationName : "Coded";
    }

    default void stopTransition() {
    }

    default void markDirty() {
    }

    default boolean isPlaying() {
        return getCurrentAnimation() != null;
    }
}
