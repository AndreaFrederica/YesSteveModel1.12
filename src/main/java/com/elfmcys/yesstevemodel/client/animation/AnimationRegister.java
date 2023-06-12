package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.LazyVariable;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.IEquipable;
import net.minecraft.entity.Pose;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.function.BiPredicate;

public class AnimationRegister {
    private static final double MIN_SPEED = 0.15;

    public static void registerAnimationState() {
        register("sleep", Priority.HIGHEST, (player, event) -> player.getPose() == Pose.SLEEPING);
        register("swim", Priority.HIGHEST, (player, event) -> player.isSwimming());
        register("climbing", Priority.HIGHEST, (player, event) -> player.getPose() == Pose.SWIMMING);
        register("fly", Priority.HIGHEST, (player, event) -> player.abilities.flying);

        register("elytra_fly", Priority.HIGH, (player, event) -> player.getPose() == Pose.FALL_FLYING && player.isFallFlying());
        register("ride_pig", Priority.HIGH, (player, event) -> player.getVehicle() instanceof PigEntity);
        register("ride", Priority.HIGH, (player, event) -> player.getVehicle() instanceof IEquipable);
        register("boat", Priority.HIGH, (player, event) -> player.getVehicle() instanceof BoatEntity);
        register("sit", Priority.HIGH, (player, event) -> player.isPassenger());

        register("swim_stand", Priority.NORMAL, (player, event) -> player.isInWater() && event.getLimbSwingAmount() > MIN_SPEED);
        register("attacked", ILoopType.EDefaultLoopTypes.PLAY_ONCE, Priority.NORMAL, (player, event) -> player.hurtTime > 0);
        register("jump", Priority.NORMAL, (player, event) -> !player.isOnGround() && !player.isInWater());
        register("sneak", Priority.NORMAL, (player, event) -> player.isOnGround() && player.getPose() == Pose.CROUCHING && Math.abs(event.getLimbSwingAmount()) > MIN_SPEED);
        register("sneaking", Priority.NORMAL, (player, event) -> player.isOnGround() && player.getPose() == Pose.CROUCHING);

        register("run", Priority.LOW, (player, event) -> player.isOnGround() && player.isSprinting());
        register("walk", Priority.LOW, (player, event) -> player.isOnGround() && event.getLimbSwingAmount() > MIN_SPEED);

        register("idle", Priority.LOWEST, (player, event) -> true);
    }

    @SuppressWarnings("deprecation")
    public static void registerVariables() {
        MolangParser parser = GeckoLibCache.getInstance().parser;

        parser.register(new LazyVariable("query.anim_time", 0));
        parser.register(new LazyVariable("query.actor_count", 0));
        parser.register(new LazyVariable("query.health", 0));
        parser.register(new LazyVariable("query.max_health", 0));
        parser.register(new LazyVariable("query.distance_from_camera", 0));
        parser.register(new LazyVariable("query.yaw_speed", 0));
        parser.register(new LazyVariable("query.is_in_water_or_rain", 0));
        parser.register(new LazyVariable("query.is_in_water", 0));
        parser.register(new LazyVariable("query.is_on_ground", 0));
        parser.register(new LazyVariable("query.time_of_day", 0));
        parser.register(new LazyVariable("query.is_on_fire", 0));
        parser.register(new LazyVariable("query.ground_speed", 0));

        parser.register(new LazyVariable("ysm.head_yaw", 0));
        parser.register(new LazyVariable("ysm.head_pitch", 0));

        parser.register(new LazyVariable("ysm.has_helmet", MolangUtils.FALSE));
        parser.register(new LazyVariable("ysm.has_chest_plate", MolangUtils.FALSE));
        parser.register(new LazyVariable("ysm.has_leggings", MolangUtils.FALSE));
        parser.register(new LazyVariable("ysm.has_boots", MolangUtils.FALSE));

        parser.register(new LazyVariable("ysm.has_mainhand", MolangUtils.FALSE));
        parser.register(new LazyVariable("ysm.has_offhand", MolangUtils.FALSE));

        parser.register(new LazyVariable("ysm.is_close_eyes", MolangUtils.FALSE));
        parser.register(new LazyVariable("ysm.is_passenger", MolangUtils.FALSE));
        parser.register(new LazyVariable("ysm.is_sleep", MolangUtils.FALSE));
        parser.register(new LazyVariable("ysm.is_sneak", MolangUtils.FALSE));

        parser.register(new LazyVariable("ysm.armor_value", 0));
        parser.register(new LazyVariable("ysm.hurt_time", 0));
    }

    public static void setParserValue(AnimationEvent<CustomPlayerEntity> animationEvent, MolangParser parser, EntityModelData data, PlayerEntity player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            parser.setValue("query.actor_count", mc.level::getEntityCount);
            parser.setValue("query.time_of_day", () -> MolangUtils.normalizeTime(mc.level.getDayTime()));
            parser.setValue("query.moon_phase", mc.level::getMoonPhase);
        }

        parser.setValue("query.distance_from_camera", () -> mc.gameRenderer.getMainCamera().getPosition().distanceTo(player.position()));
        parser.setValue("query.is_on_ground", () -> MolangUtils.booleanToFloat(player.isOnGround()));
        parser.setValue("query.is_in_water", () -> MolangUtils.booleanToFloat(player.isInWater()));
        parser.setValue("query.is_in_water_or_rain", () -> MolangUtils.booleanToFloat(player.isInWaterRainOrBubble()));

        parser.setValue("query.health", player::getHealth);
        parser.setValue("query.max_health", player::getMaxHealth);
        parser.setValue("query.is_on_fire", () -> MolangUtils.booleanToFloat(player.isOnFire()));
        parser.setValue("query.ground_speed", () -> getGroundSpeed(player));
        parser.setValue("query.yaw_speed", () -> getYawSpeed(animationEvent, player));

        parser.setValue("ysm.head_yaw", () -> data.netHeadYaw);
        parser.setValue("ysm.head_pitch", () -> data.headPitch);

        parser.setValue("ysm.has_helmet", () -> getSlotValue(player, EquipmentSlotType.HEAD));
        parser.setValue("ysm.has_chest_plate", () -> getSlotValue(player, EquipmentSlotType.CHEST));
        parser.setValue("ysm.has_leggings", () -> getSlotValue(player, EquipmentSlotType.LEGS));
        parser.setValue("ysm.has_boots", () -> getSlotValue(player, EquipmentSlotType.FEET));

        parser.setValue("ysm.has_mainhand", () -> getSlotValue(player, EquipmentSlotType.MAINHAND));
        parser.setValue("ysm.has_offhand", () -> getSlotValue(player, EquipmentSlotType.OFFHAND));

        parser.setValue("ysm.is_close_eyes", () -> getEyeCloseState(animationEvent, player));
        parser.setValue("ysm.is_passenger", () -> MolangUtils.booleanToFloat(player.isPassenger()));
        parser.setValue("ysm.is_sleep", () -> MolangUtils.booleanToFloat(player.getPose() == Pose.SLEEPING));
        parser.setValue("ysm.is_sneak", () -> MolangUtils.booleanToFloat(player.isOnGround() && player.getPose() == Pose.CROUCHING));

        parser.setValue("ysm.armor_value", player::getArmorValue);
        parser.setValue("ysm.hurt_time", () -> player.hurtTime);
    }

    private static float getYawSpeed(AnimationEvent<CustomPlayerEntity> animationEvent, PlayerEntity player) {
        double seekTime = animationEvent.getAnimationTick();
        return player.getViewYRot((float) seekTime - player.getViewYRot((float) seekTime - 0.1f));
    }

    private static float getGroundSpeed(PlayerEntity player) {
        Vector3d velocity = player.getDeltaMovement();
        return MathHelper.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));
    }

    private static void register(String animationName, ILoopType loopType, int priority, BiPredicate<PlayerEntity, AnimationEvent<CustomPlayerEntity>> predicate) {
        AnimationManager manager = AnimationManager.getInstance();
        manager.register(new AnimationState(animationName, loopType, priority, predicate));
    }

    private static void register(String animationName, int priority, BiPredicate<PlayerEntity, AnimationEvent<CustomPlayerEntity>> predicate) {
        register(animationName, ILoopType.EDefaultLoopTypes.LOOP, priority, predicate);
    }

    private static double getEyeCloseState(AnimationEvent<CustomPlayerEntity> animationEvent, PlayerEntity player) {
        double remainder = (animationEvent.getAnimationTick() + Math.abs(player.getUUID().getLeastSignificantBits()) % 10) % 90;
        boolean isBlinkTime = 85 < remainder && remainder < 90;
        return MolangUtils.booleanToFloat(player.isSleeping() || isBlinkTime);
    }

    private static double getSlotValue(PlayerEntity player, EquipmentSlotType slot) {
        return MolangUtils.booleanToFloat(!player.getItemBySlot(slot).isEmpty());
    }
}