package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin;

import com.elfmcys.yesstevemodel.client.compat.SwimmingCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query.*;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.util.EntityUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class QueryBinding extends ContextBinding {
    public static final QueryBinding INSTANCE = new QueryBinding();

    // TODO：补全
    private QueryBinding() {
        this.function("debug_output", new DebugOutputFunction());
        this.function("biome_has_all_tags", new EmptyFunction());
        this.function("biome_has_any_tag", new EmptyFunction());
        this.function("relative_block_has_all_tags", new EmptyFunction());
        this.function("relative_block_has_any_tag", new EmptyFunction());
        this.function("is_item_name_any", new ItemNameAny());
        this.function("equipped_item_all_tags", new EmptyFunction());
        this.function("equipped_item_any_tag", new EmptyFunction());
        this.function("position", new Position());
        this.function("position_delta", new PositionDelta());
        this.function("rotation_to_camera", new EmptyFunction());

        this.function("max_durability", new ItemMaxDurability());
        this.function("remaining_durability", new ItemRemainingDurability());

        this.var("actor_count", ctx -> ctx.level().getLoadedEntityList().size());
        this.var("anim_time", ctx -> ctx.animationControllerContext().animTime());
        this.var("all_animations_finished", ctx -> false);
        this.var("any_animation_finished", ctx -> false);
        this.var("life_time", ctx -> ctx.geoInstance().getSeekTime() / 20.0);
        this.var("head_x_rotation", ctx -> ctx.data().netHeadYaw);
        this.var("head_y_rotation", ctx -> ctx.data().headPitch);
        this.var("moon_phase", ctx -> ctx.level().getMoonPhase());
        this.var("time_of_day", ctx -> MolangUtils.normalizeTime(ctx.level().getWorldTime()));
        this.var("time_stamp", ctx -> ctx.level().getWorldTime());
        this.var("delta_time", ctx -> 0.0);

        this.entityVar("yaw_speed", ctx -> EntityUtil.getYawSpeed(ctx.entity()));
        this.entityVar("cardinal_facing_2d", ctx -> ctx.entity().getHorizontalFacing().getIndex());
        this.entityVar("distance_from_camera", ctx -> EntityUtil.getCameraPosition(ctx.mc(), ctx.animationEvent().getPartialTick()).distanceTo(ctx.entity().getPositionVector()));
        this.entityVar("eye_target_x_rotation", ctx -> EntityUtil.getViewXRot(ctx.entity(), ctx.animationEvent().getPartialTick()));
        this.entityVar("eye_target_y_rotation", ctx -> EntityUtil.getViewYRot(ctx.entity(), ctx.animationEvent().getPartialTick()));
        this.entityVar("ground_speed", ctx -> EntityUtil.getGroundSpeed(ctx.entity()));
        this.entityVar("modified_distance_moved", ctx -> ctx.entity().distanceWalkedModified);
        this.entityVar("vertical_speed", ctx -> EntityUtil.getVerticalSpeed(ctx.entity()));
        this.entityVar("walk_distance", ctx -> ctx.entity().distanceWalkedOnStepModified);
        this.entityVar("has_rider", ctx -> ctx.entity().isBeingRidden());
        this.entityVar("is_first_person", ctx -> ctx.mc().gameSettings.thirdPersonView == 0);
        this.entityVar("is_in_water", ctx -> ctx.entity().isInWater());
        this.entityVar("is_in_water_or_rain", ctx -> ctx.entity().isWet());
        this.entityVar("is_on_fire", ctx -> ctx.entity().isBurning());
        this.entityVar("is_on_ground", ctx -> ctx.entity().onGround);
        this.entityVar("is_riding", ctx -> ctx.entity().isRiding());
        this.entityVar("is_sneaking", ctx -> ctx.entity().onGround && ctx.entity().isSneaking());
        this.playerEntityVar("is_spectator", ctx -> ctx.entity().isSpectator());
        this.entityVar("is_sprinting", ctx -> ctx.entity().isSprinting());
        this.playerEntityVar("is_swimming", ctx -> SwimmingCompat.isSwimming(ctx.entity()));

        this.livingEntityVar("body_x_rotation", ctx -> Interpolations.lerp(ctx.entity().prevRotationPitch, ctx.entity().rotationPitch, ctx.animationEvent().getPartialTick()));
        this.livingEntityVar("body_y_rotation", ctx -> MathHelper.wrapDegrees(Interpolations.lerp(ctx.entity().prevRotationYaw, ctx.entity().rotationYaw, ctx.animationEvent().getPartialTick())));
        this.livingEntityVar("health", ctx -> ctx.entity().getHealth());
        this.livingEntityVar("max_health", ctx -> ctx.entity().getMaxHealth());
        this.livingEntityVar("hurt_time", ctx -> ctx.entity().hurtTime);
        this.livingEntityVar("is_eating", ctx -> ctx.entity().getActiveItemStack().getItemUseAction() == EnumAction.EAT);
        this.livingEntityVar("is_playing_dead", ctx -> !ctx.entity().isEntityAlive());
        this.livingEntityVar("is_sleeping", ctx -> ctx.entity().isPlayerSleeping());
        this.livingEntityVar("is_using_item", ctx -> ctx.entity().isHandActive());
        this.livingEntityVar("item_in_use_duration", ctx -> ctx.entity().getItemInUseMaxCount() / 20.0);
        this.livingEntityVar("item_max_use_duration", ctx -> getMaxUseDuration(ctx.entity()) / 20.0);
        this.livingEntityVar("item_remaining_use_duration", ctx -> ctx.entity().getItemInUseCount() / 20.0);
        this.livingEntityVar("equipment_count", ctx -> getEquipmentCount(ctx.entity()));

        this.playerEntityVar("cape_flap_amount", ctx -> getCapeFlapAmount(ctx.entity(), ctx.animationEvent().getPartialTick()));
        this.playerEntityVar("player_level", ctx -> ctx.entity().experienceLevel);
        this.playerEntityVar("is_jumping", ctx -> !ctx.entity().capabilities.isFlying && !ctx.entity().isRiding() && !ctx.entity().onGround && !ctx.entity().isInWater());
        this.abstractClientPlayerVar("has_cape", ctx -> hasCape(ctx.entity()));
    }

    private static boolean hasCape(AbstractClientPlayer player) {
        return player.hasPlayerInfo() && !player.isInvisible() && player.isWearing(EnumPlayerModelParts.CAPE) && player.getLocationCape() != null;
    }

    private static int getEquipmentCount(EntityLivingBase entity) {
        int count = 0;
        for (var slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR) {
                continue;
            }
            var stack = entity.getItemStackFromSlot(slot);
            if (!stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static double getMaxUseDuration(EntityLivingBase player) {
        ItemStack useItem = player.getActiveItemStack();
        if (useItem.isEmpty()) {
            return 0.0;
        } else {
            return useItem.getMaxItemUseDuration();
        }
    }

    /// {@link net.minecraft.client.renderer.entity.layers.LayerCape#doRenderLayer(AbstractClientPlayer, float, float, float, float, float, float, float)}
    private static float getCapeFlapAmount(EntityPlayer player, float partialTick) {
        double deltaX = Interpolations.lerp(player.prevChasingPosX, player.chasingPosX, partialTick) - Interpolations.lerp(player.prevPosX, player.posX, partialTick);
        double deltaY = Interpolations.lerp(player.prevChasingPosY, player.chasingPosY, partialTick) - Interpolations.lerp(player.prevPosY, player.posY, partialTick);
        double deltaZ = Interpolations.lerp(player.prevChasingPosZ, player.chasingPosZ, partialTick) - Interpolations.lerp(player.prevPosZ, player.posZ, partialTick);

        float bodyYaw = Interpolations.lerp(player.prevRenderYawOffset, player.renderYawOffset, partialTick);
        double sinYaw = MathHelper.sin(bodyYaw * ((float) Math.PI / 180F));
        double cosYaw = -MathHelper.cos(bodyYaw * ((float) Math.PI / 180F));

        float verticalFlap = (float) (deltaY * 10.0F);
        verticalFlap = MathHelper.clamp(verticalFlap, -6.0F, 32.0F);

        float forwardMovement = (float) ((deltaX * sinYaw + deltaZ * cosYaw) * 100.0F);
        forwardMovement = MathHelper.clamp(forwardMovement, 0.0F, 150.0F);
        forwardMovement = Math.min(forwardMovement, 0.0F);

        float walkBobbingAmplitude = Interpolations.lerp(player.prevCameraYaw, player.cameraYaw, partialTick);
        float walkDistance = Interpolations.lerp(player.prevDistanceWalkedModified, player.distanceWalkedModified, partialTick);

        verticalFlap += MathHelper.sin(walkDistance * 6.0F) * 32.0F * walkBobbingAmplitude;
        if (player.isSneaking()) {
            verticalFlap += 25.0F;
        }

        return MathHelper.clamp((6.0F + forwardMovement / 2.0F + verticalFlap) / 108.0F, 0.0F, 1.0F);
    }
}
