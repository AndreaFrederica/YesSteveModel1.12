package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.client.animation.condition.*;
import com.elfmcys.yesstevemodel.client.compat.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AnimationManager {
    //private final static String TAC_ID = "tac";
    private static AnimationManager MANAGER;
    @SuppressWarnings("unchecked")
    private final ReferenceArrayList<AnimationState>[] data = new ReferenceArrayList[Priority.LOWEST + 1];

    public AnimationManager() {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = new ReferenceArrayList<>(6);
        }
    }

    public static AnimationManager getInstance() {
        if (MANAGER == null) {
            MANAGER = new AnimationManager();
        }
        return MANAGER;
    }

    @Nonnull
    public static PlayState playLoopAnimation(AnimationEvent<?> event, String animationName) {
        return playAnimation(event, animationName, ILoopType.EDefaultLoopTypes.LOOP);
    }

    @Nonnull
    private static PlayState playAnimation(AnimationEvent<?> event, String animationName, ILoopType loopType) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, loopType));
        return PlayState.CONTINUE;
    }

    @Nonnull
    private static PlayState playAnimation(AnimationEvent<?> event, String animationName) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName));
        return PlayState.CONTINUE;
    }

    public void register(AnimationState state) {
        this.data[state.priority()].add(state);
    }

    @Nonnull
    public PlayState predicateParallel(AnimationEvent<?> event, String animationName) {
        if (Minecraft.getMinecraft().isGamePaused()) {
            return PlayState.STOP;
        }
        return playLoopAnimation(event, animationName);
    }

    @Nonnull
    public PlayState predicateCap(AnimationEvent<CustomPlayerEntity> event) {
        CustomPlayerEntity animatable = event.getAnimatableEntity();
        EntityPlayer player = animatable.getEntity();
        if (player == null) {
            if (animatable.hasPreviewAnimation()) {
                return playLoopAnimation(event, animatable.getPreviewAnimation());
            }
            return PlayState.STOP;
        }

        return CapabilityEvent.getModelInfoCap(player).map(cap -> {
            if (cap.isPlayAnimation()) {
                return playAnimation(event, cap.getAnimation());
            }
            return PlayState.STOP;
        }).orElse(PlayState.STOP);
    }

    @Nonnull
    public PlayState predicateMain(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        for (int i = Priority.HIGHEST; i <= Priority.LOWEST; i++) {
            // 载具动画单独检查
            if (i == Priority.HIGH) {
                PlayState vehicleAnimation = this.getVehicleAnimation(event);
                if (vehicleAnimation != null) {
                    return vehicleAnimation;
                }
            }
            for (AnimationState state : this.data[i]) {
                if (state.predicate().test(player, event)) {
                    String animationName = state.animationName();
                    ILoopType loopType = state.loopType();
//                    if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(player.getMainHandItem())) {
//                        return TacGunRenderer.playGunMainAnimation(event, animationName, loopType);
//                    }
                    return playAnimation(event, animationName, loopType);
                }
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateOffhandHold(AnimationEvent<CustomPlayerEntity> event) {
        CustomPlayerEntity animatable = event.getAnimatableEntity();
        EntityPlayer player = animatable.getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        if (!player.isSwingInProgress && !player.isHandActive()) {
            ItemStack offhandItem = player.getHeldItem(EnumHand.OFF_HAND);
            if (CrossbowCompat.isCharged(offhandItem)) {
                return playAnimation(event, "hold_offhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        if (checkSwingAndUse(player, EnumHand.OFF_HAND)) {
            ItemStack offhandItem = player.getHeldItem(EnumHand.OFF_HAND);
            if (!isSameItem(animatable, offhandItem, EnumHand.OFF_HAND)) {
                animatable.getHandItemsForAnimation()[EnumHand.OFF_HAND.ordinal()] = offhandItem;
                playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.LOOP);
            }

            ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
            ConditionalHold conditionalHold = ConditionManager.getHoldOffhand(id);
            if (conditionalHold != null) {
                String name = conditionalHold.doTest(player, EnumHand.OFF_HAND);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateMainhandHold(AnimationEvent<CustomPlayerEntity> event) {
        CustomPlayerEntity animatable = event.getAnimatableEntity();
        EntityPlayer player = animatable.getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        if (CarryOnCompat.isInstalled()) {
            String carryName = CarryOnCompat.getCarryOnString(player);
            if (carryName != null) {
                return playAnimation(event, "carryon:" + carryName, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        if (!player.isSwingInProgress && !player.isHandActive()) {
            ItemStack mainHandItem = player.getHeldItem(EnumHand.MAIN_HAND);
//            PlayState gunHoldAnimation = GunClientUtil.playGunHoldAnimation(mainHandItem, event);
//            if (gunHoldAnimation != null) {
//                return gunHoldAnimation;
//            }
            if (CrossbowCompat.isCharged(mainHandItem)) {
                return playAnimation(event, "hold_mainhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP);
            }
            if (player.fishEntity != null) {
                return playAnimation(event, "hold_mainhand:fishing", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }

        if (checkSwingAndUse(player, EnumHand.MAIN_HAND)) {
            ItemStack mainHandItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (!isSameItem(animatable, mainHandItem, EnumHand.MAIN_HAND)) {
                animatable.getHandItemsForAnimation()[EnumHand.MAIN_HAND.ordinal()] = mainHandItem;
                playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.LOOP);
            }

            ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
            ConditionalHold conditionalHold = ConditionManager.getHoldMainhand(id);
            if (conditionalHold != null) {
                String name = conditionalHold.doTest(player, EnumHand.MAIN_HAND);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateSwing(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        if (player.isSwingInProgress && !player.isPlayerSleeping()) {
            if (player.swingProgressInt == 0) {
                // 空动画用于重置 PLAY_ONCE 动画
                playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            EnumHand swingingHand = player.swingingHand;
            ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
            ConditionalSwing conditionalSwing = (player.swingingHand == EnumHand.MAIN_HAND) ? ConditionManager.getSwingMainhand(id) : ConditionManager.getSwingOffhand(id);
            if (conditionalSwing != null) {
                String name = conditionalSwing.doTest(player, swingingHand);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.PLAY_ONCE);
                }
            }
            String defaultSwing = (swingingHand == EnumHand.MAIN_HAND) ? "swing_hand" : "swing_offhand";
            return playAnimation(event, defaultSwing, ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        return PlayState.CONTINUE;
    }

    @Nonnull
    public PlayState predicateUse(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        if (player.isHandActive() && !player.isPlayerSleeping()) {
            if (player.getItemInUseMaxCount() == 1) {
                playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            if (player.getActiveHand() == EnumHand.MAIN_HAND) {
                ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
                ConditionalUse conditionalUse = ConditionManager.getUseMainhand(id);
                if (conditionalUse != null) {
                    String name = conditionalUse.doTest(player, EnumHand.MAIN_HAND);
                    if (StringUtils.isNoneBlank(name)) {
                        return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                    }
                }
                return playAnimation(event, "use_mainhand", ILoopType.EDefaultLoopTypes.LOOP);
            } else {
                ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
                ConditionalUse conditionalUse = ConditionManager.getUseOffhand(id);
                if (conditionalUse != null) {
                    String name = conditionalUse.doTest(player, EnumHand.OFF_HAND);
                    if (StringUtils.isNoneBlank(name)) {
                        return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                    }
                }
                return playAnimation(event, "use_offhand", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateArmor(AnimationEvent<CustomPlayerEntity> event, EntityEquipmentSlot slot) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        ItemStack itemBySlot = player.getItemStackFromSlot(slot);
        if (itemBySlot.isEmpty()) {
            return PlayState.STOP;
        }

        ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
        ConditionArmor conditionArmor = ConditionManager.getArmor(id);
        if (conditionArmor != null) {
            String name = conditionArmor.doTest(player, slot);
            if (StringUtils.isNoneBlank(name)) {
                return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }

        ResourceLocation animation = event.getAnimatableEntity().getAnimationFileLocation();
        String defaultName = slot.getName() + ":default";
        if (GeckoLibCache.getInstance().getAnimations().get(animation).animations().containsKey(defaultName)) {
            return playAnimation(event, defaultName, ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }

    @Nullable
    public PlayState getVehicleAnimation(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null) {
            return null;
        }
        Entity vehicle = player.getRidingEntity();
        if (vehicle == null || !vehicle.isEntityAlive()) {
            return null;
        }
        ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();

        // 其他情况
        ConditionalVehicle vehicleCondition = ConditionManager.getVehicle(id);
        if (vehicleCondition != null) {
            String name = vehicleCondition.doTest(player);
            if (StringUtils.isNoneBlank(name)) {
                return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        return null;
    }

    @Nonnull
    public PlayState predicatePassengerAnimation(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatableEntity().getEntity();
        if (player == null) {
            return PlayState.STOP;
        }
        Entity passenger = player.getControllingPassenger();
        if (passenger == null || !passenger.isEntityAlive()) {
            return PlayState.STOP;
        }

        ResourceLocation id = event.getAnimatableEntity().getAnimationFileLocation();
        ConditionalPassenger conditionalPassenger = ConditionManager.getPassenger(id);
        if (conditionalPassenger != null) {
            String name = conditionalPassenger.doTest(player);
            if (StringUtils.isNoneBlank(name)) {
                return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        return PlayState.STOP;
    }

    /// @return 不在使用且不在挥动
    private static boolean checkSwingAndUse(EntityPlayer player, EnumHand hand) {
        if (player.isSwingInProgress && player.swingingHand == hand) {
            return false;
        }
        return !player.isHandActive() || player.getActiveHand() != hand;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isSameItem(CustomPlayerEntity animatable, ItemStack itemStack, EnumHand hand) {
        ItemStack preItem = animatable.getHandItemsForAnimation()[hand.ordinal()];
        if (preItem.isItemDamaged()) {
            return ItemStack.areItemsEqual(itemStack, preItem);
        }
        return ItemStack.areItemStacksEqual(itemStack, preItem);
    }
}
