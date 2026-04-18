package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.client.compat.SwimmingCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiPredicate;

public class AnimationRegister {
    private static final double MIN_SPEED = 0.05;

    public static void registerAnimationState() {
        register("death", ILoopType.EDefaultLoopTypes.PLAY_ONCE, Priority.HIGHEST, (player, event) -> !player.isEntityAlive());
        register("riptide", Priority.HIGHEST, (player, event) -> TridentCompat.isAutoSpinAttack(player));
        register("sleep", Priority.HIGHEST, (player, event) -> player.isPlayerSleeping());
        register("swim", Priority.HIGHEST, (player, event) -> SwimmingCompat.isSwimming(player));
        register("climb", Priority.HIGHEST, (player, event) -> SwimmingCompat.isSwimmingPose(player) && isMoving(player));
        register("climbing", Priority.HIGHEST, (player, event) -> SwimmingCompat.isSwimmingPose(player));

        register("ride_pig", Priority.HIGH, (player, event) -> player.getRidingEntity() instanceof EntityPig);
        register("ride", Priority.HIGH, (player, event) -> player.getRidingEntity() instanceof AbstractHorse);
        register("boat", Priority.HIGH, (player, event) -> player.getRidingEntity() instanceof EntityBoat);
        register("sit", Priority.HIGH, (player, event) -> player.isRiding());
//        register("ladder_up", Priority.HIGHEST, (player, event) -> player.isOnLadder() && EntityUtil.getVerticalSpeed(player) > 0);
//        register("ladder_stillness", Priority.HIGHEST, (player, event) -> player.isOnLadder() && EntityUtil.getVerticalSpeed(player) == 0);
//        register("ladder_down", Priority.HIGHEST, (player, event) -> player.isOnLadder() && EntityUtil.getVerticalSpeed(player) < 0);

        register("fly", Priority.HIGH, (player, event) -> player.capabilities.isFlying);
        register("elytra_fly", Priority.HIGH, (player, event) -> player.isElytraFlying());

        register("swim_stand", Priority.NORMAL, (player, event) -> player.isInWater());
        register("attacked", ILoopType.EDefaultLoopTypes.PLAY_ONCE, Priority.NORMAL, (player, event) -> player.hurtTime > 0);
        register("jump", Priority.NORMAL, (player, event) -> !player.onGround && !player.isInWater());
        register("sneak", Priority.NORMAL, (player, event) -> player.onGround && player.isSneaking() && isMoving(player));
        register("sneaking", Priority.NORMAL, (player, event) -> player.onGround && player.isSneaking());

        register("run", Priority.LOW, (player, event) -> player.onGround && player.isSprinting());
        register("walk", Priority.LOW, (player, event) -> player.onGround && isMoving(player));

        register("idle", Priority.LOWEST, (player, event) -> true);
    }

    private static void register(String animationName, ILoopType loopType, int priority, BiPredicate<EntityPlayer, AnimationEvent<?>> predicate) {
        AnimationManager manager = AnimationManager.getInstance();
        manager.register(new AnimationState(animationName, loopType, priority, predicate));
    }

    private static void register(String animationName, int priority, BiPredicate<EntityPlayer, AnimationEvent<?>> predicate) {
        register(animationName, ILoopType.EDefaultLoopTypes.LOOP, priority, predicate);
    }

    private static boolean isMoving(EntityLivingBase entity) {
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        float speed = Interpolations.lerp(entity.prevLimbSwingAmount, entity.limbSwingAmount, partialTick);
        return Math.abs(speed) > MIN_SPEED;
    }
}
