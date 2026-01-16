package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.animation.condition.*;
import com.elfmcys.yesstevemodel.client.compat.TacGunRenderer;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.LinkedList;

public final class AnimationManager {
    private final static String TAC_ID = "tac";
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

    public PlayState predicateParallel(AnimationEvent<CustomPlayerEntity> event, String animationName) {
        if (Minecraft.getInstance().isPaused()) {
            return PlayState.STOP;
        }
        return playLoopAnimation(event, animationName);
    }

    public PlayState predicateCap(AnimationEvent<CustomPlayerEntity> event) {
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
            return PlayState.STOP;
        }).orElse(PlayState.STOP);
    }

    @Nonnull
    public PlayState predicateMain(AnimationEvent<CustomPlayerEntity> event) {
        PlayerEntity player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        for (int i = Priority.HIGHEST; i <= Priority.LOWEST; i++) {
            if (!data.containsKey(i)) {
                continue;
            }
            LinkedList<AnimationState> states = data.get(i);
            for (AnimationState state : states) {
                if (state.getPredicate().test(player, event)) {
                    String animationName = state.getAnimationName();
                    ILoopType loopType = state.getLoopType();
                    if (ModList.get().isLoaded(TAC_ID) && TacGunRenderer.isGun(player.getMainHandItem())) {
                        return TacGunRenderer.playGunMainAnimation(event, animationName, loopType);
                    }
                    return playAnimation(event, animationName, loopType);
                }
            }
        }
        return PlayState.STOP;
    }

    public PlayState predicateOffhandHold(AnimationEvent<CustomPlayerEntity> event) {
        PlayerEntity player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (!player.getOffhandItem().isEmpty() && checkSwingAndUse(player, Hand.OFF_HAND)) {
            ResourceLocation id = event.getAnimatable().getAnimation();
            ConditionalHold conditionalHold = ConditionManager.getHoldOffhand(id);
            if (conditionalHold != null) {
                String name = conditionalHold.doTest(player, Hand.OFF_HAND);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        return PlayState.STOP;
    }

    public PlayState predicateMainhandHold(AnimationEvent<CustomPlayerEntity> event) {
        PlayerEntity player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (!player.swinging && !player.isUsingItem()) {
            ItemStack mainHandItem = player.getItemInHand(Hand.MAIN_HAND);
            if (mainHandItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(mainHandItem)) {
                return playAnimation(event, "hold_mainhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP);
            }
            if (ModList.get().isLoaded(TAC_ID) && TacGunRenderer.isGun(mainHandItem)) {
                return TacGunRenderer.playGunHoldAnimation(event, mainHandItem);
            }
            ItemStack offhandItem = player.getItemInHand(Hand.OFF_HAND);
            if (offhandItem.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(offhandItem)) {
                return playAnimation(event, "hold_offhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP);
            }
            if (player.fishing != null) {
                return playAnimation(event, "hold_mainhand:fishing", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }

        if (!player.getMainHandItem().isEmpty() && checkSwingAndUse(player, Hand.MAIN_HAND)) {
            ResourceLocation id = event.getAnimatable().getAnimation();
            ConditionalHold conditionalHold = ConditionManager.getHoldMainhand(id);
            if (conditionalHold != null) {
                String name = conditionalHold.doTest(player, Hand.MAIN_HAND);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        return PlayState.STOP;
    }

    public PlayState predicateSwing(AnimationEvent<CustomPlayerEntity> event) {
        PlayerEntity player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (player.swinging && !player.isSleeping()) {
            if (player.swingTime == 0) {
                event.getController().shouldResetTick = true;
                event.getController().adjustTick(0);
            }
            ResourceLocation id = event.getAnimatable().getAnimation();
            ConditionalSwing conditionalSwing = ConditionManager.getSwing(id);
            if (conditionalSwing != null) {
                String name = conditionalSwing.doTest(player, player.swingingArm);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
            return playAnimation(event, "swing_hand", ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }

    public PlayState predicateUse(AnimationEvent<CustomPlayerEntity> event) {
        PlayerEntity player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (player.isUsingItem() && !player.isSleeping()) {
            if (player.getTicksUsingItem() == 1) {
                event.getController().shouldResetTick = true;
                event.getController().adjustTick(0);
            }
            if (ModList.get().isLoaded(TAC_ID) && TacGunRenderer.isGrenade(player.getUseItem())) {
                return TacGunRenderer.playGrenadeAnimation(event, player.getUsedItemHand());
            }
            if (player.getUsedItemHand() == Hand.MAIN_HAND) {
                ResourceLocation id = event.getAnimatable().getAnimation();
                ConditionalUse conditionalUse = ConditionManager.getUseMainhand(id);
                if (conditionalUse != null) {
                    String name = conditionalUse.doTest(player, Hand.MAIN_HAND);
                    if (StringUtils.isNoneBlank(name)) {
                        return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                    }
                }
                return playAnimation(event, "use_mainhand", ILoopType.EDefaultLoopTypes.LOOP);
            } else {
                ResourceLocation id = event.getAnimatable().getAnimation();
                ConditionalUse conditionalUse = ConditionManager.getUseOffhand(id);
                if (conditionalUse != null) {
                    String name = conditionalUse.doTest(player, Hand.OFF_HAND);
                    if (StringUtils.isNoneBlank(name)) {
                        return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                    }
                }
                return playAnimation(event, "use_offhand", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        return PlayState.STOP;
    }

    public PlayState predicateArmor(AnimationEvent<CustomPlayerEntity> event, EquipmentSlotType slot) {
        PlayerEntity player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        ItemStack itemBySlot = player.getItemBySlot(slot);
        if (itemBySlot.isEmpty()) {
            return PlayState.STOP;
        }

        ResourceLocation id = event.getAnimatable().getAnimation();
        ConditionArmor conditionArmor = ConditionManager.getArmor(id);
        if (conditionArmor != null) {
            String name = conditionArmor.doTest(player, slot);
            if (StringUtils.isNoneBlank(name)) {
                return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }

        ResourceLocation animation = event.getAnimatable().getAnimation();
        String defaultName = slot.getName() + ":default";
        if (GeckoLibCache.getInstance().getAnimations().get(animation).animations().containsKey(defaultName)) {
            return playAnimation(event, defaultName, ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }

    private boolean checkSwingAndUse(PlayerEntity player, Hand hand) {
        if (player.swinging && player.swingingArm == hand) {
            return false;
        }
        return !player.isUsingItem() || player.getUsedItemHand() != hand;
    }
}
