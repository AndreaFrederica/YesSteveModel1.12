package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import javax.annotation.Nonnull;
import java.util.LinkedList;

public final class AnimationManager {
    private static AnimationManager MANAGER;
    private final Int2ObjectOpenHashMap<LinkedList<AnimationState>> data = new Int2ObjectOpenHashMap<>();

    public static AnimationManager getInstance() {
        if (MANAGER == null) {
            MANAGER = new AnimationManager();
        }
        return MANAGER;
    }

    @Nonnull
    private static <P extends IAnimatable> PlayState playLoopAnimation(AnimationEvent<P> event, String animationName) {
        return playAnimation(event, animationName, ILoopType.EDefaultLoopTypes.LOOP);
    }

    @Nonnull
    private static <P extends IAnimatable> PlayState playAnimation(AnimationEvent<P> event, String animationName, ILoopType loopType) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, loopType));
        return PlayState.CONTINUE;
    }

    @Nonnull
    private static <P extends IAnimatable> PlayState playAnimation(AnimationEvent<P> event, String animationName) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName));
        return PlayState.CONTINUE;
    }

    public void register(AnimationState state) {
        if (data.containsKey(state.getPriority())) {
            data.get(state.getPriority()).add(state);
        } else {
            LinkedList<AnimationState> states = Lists.newLinkedList();
            states.add(state);
            data.put(state.getPriority(), states);
        }
    }

    public PlayState predicate(AnimationEvent<CustomPlayerEntity> event) {
        CustomPlayerEntity animatable = event.getAnimatable();
        PlayerEntity player = animatable.getPlayer();
        if (player == null) {
            if (animatable.hasPreviewAnimation()) {
                return playLoopAnimation(event, animatable.getPreviewAnimation());
            }
            return PlayState.STOP;
        }

        return player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).map(cap -> {
            if (cap.isPlayAnimation()) {
                return playAnimation(event, cap.getAnimation());
            }
            return predicateMain(event, player);
        }).orElse(PlayState.STOP);
    }

    @Nonnull
    private PlayState predicateMain(AnimationEvent<CustomPlayerEntity> event, PlayerEntity player) {
        for (int i = Priority.HIGHEST; i <= Priority.LOWEST; i++) {
            if (!data.containsKey(i)) {
                continue;
            }
            LinkedList<AnimationState> states = data.get(i);
            for (AnimationState state : states) {
                if (state.getPredicate().test(player, event)) {
                    String animationName = state.getAnimationName();
                    ILoopType loopType = state.getLoopType();
                    return playAnimation(event, animationName, loopType);
                }
            }
        }
        return PlayState.STOP;
    }

    public PlayState predicateUse(AnimationEvent<CustomPlayerEntity> event) {
        PlayerEntity player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (player.swinging && !player.isSleeping()) {
            if (player.swingingArm == Hand.MAIN_HAND) {
                return playAnimation(event, "use_righthand", ILoopType.EDefaultLoopTypes.LOOP);
            } else {
                return playAnimation(event, "use_lefthand", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        return PlayState.CONTINUE;
    }
}