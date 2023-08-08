package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.compat.FirstPersonCompat;
import com.elfmcys.yesstevemodel.client.event.ReloadResourceEvent;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.function.DoubleSupplier;


@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class DebugAnimationKey {
    public static boolean DEBUG = false;

    public static final KeyBinding DEBUG_ANIMATION_KEY = new KeyBinding("key.yes_steve_model.debug_animation.desc", KeyConflictContext.IN_GAME,
            KeyModifier.ALT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (DEBUG_ANIMATION_KEY.isDown()) {
            DEBUG = !DEBUG;
            ClientPlayerEntity player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            if (DEBUG) {
                Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("message.yes_steve_model.model.debug_animation.true"), Util.NIL_UUID);
            } else {
                Minecraft.getInstance().player.sendMessage(new TranslationTextComponent("message.yes_steve_model.model.debug_animation.false"), Util.NIL_UUID);
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderGameOverlayEvent.Text event) {
        if (!DEBUG) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug) {
            return;
        }
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }
        if (!(mc.gui instanceof ForgeIngameGui)) {
            return;
        }
        ForgeIngameGui gui = (ForgeIngameGui) mc.gui;
        MatrixStack poseStack = event.getMatrixStack();
        float partialTick = event.getPartialTicks();

        ClientPlayerEntity player = mc.player;
        if (mc.level == null || player == null) {
            return;
        }

        float lerpBodyRot = MathHelper.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float lerpHeadRot = MathHelper.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;
        boolean shouldSit = player.isPassenger() && (player.getVehicle() != null && player.getVehicle().shouldRiderSit());

        if (shouldSit && player.getVehicle() instanceof LivingEntity) {
            LivingEntity vehicle = (LivingEntity) player.getVehicle();
            lerpBodyRot = MathHelper.rotLerp(partialTick, vehicle.yBodyRotO, vehicle.yBodyRot);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;
            if (clampedHeadYaw * clampedHeadYaw > 2500f) {
                lerpBodyRot += clampedHeadYaw * 0.2f;
            }
            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }
        float headPitch = MathHelper.lerp(partialTick, player.xRotO, player.xRot);
        final float outputHeadPitch = -headPitch;
        final float outputNetHeadYaw = -netHeadYaw;

        int[] y = {5};

        renderText(gui, poseStack, y, "PI", String.format("§7%.4f", Math.PI));
        renderText(gui, poseStack, y, "E", String.format("§7%.4f", Math.E));

        renderText(gui, poseStack, y, "query.actor_count", mc.level.getEntityCount());
        renderText(gui, poseStack, y, "query.anim_time", () -> 0.0);

        renderText(gui, poseStack, y, "query.body_x_rotation", () -> player.xRot);
        renderText(gui, poseStack, y, "query.body_y_rotation", () -> MathHelper.wrapDegrees(player.yRot));
        renderText(gui, poseStack, y, "query.cardinal_facing_2d", player.getDirection().get3DDataValue());
        renderText(gui, poseStack, y, "query.distance_from_camera", () -> mc.gameRenderer.getMainCamera().getPosition().distanceTo(player.position()));
        renderText(gui, poseStack, y, "query.equipment_count", getEquipmentCount(player));
        renderText(gui, poseStack, y, "query.eye_target_x_rotation", () -> player.getViewXRot(partialTick));
        renderText(gui, poseStack, y, "query.eye_target_y_rotation", () -> player.getViewYRot(partialTick));
        renderText(gui, poseStack, y, "query.ground_speed", () -> getGroundSpeed(player));

        renderText(gui, poseStack, y, "query.has_cape", hasCape(player));
        renderText(gui, poseStack, y, "query.has_rider", player.isVehicle());
        renderText(gui, poseStack, y, "query.head_x_rotation", () -> outputNetHeadYaw);
        renderText(gui, poseStack, y, "query.head_y_rotation", () -> outputHeadPitch);
        renderText(gui, poseStack, y, "query.health", player::getHealth);
        renderText(gui, poseStack, y, "query.hurt_time", player.hurtTime);

        renderText(gui, poseStack, y, "query.is_eating", player.getUseItem().getUseAnimation() == UseAction.EAT);
        renderText(gui, poseStack, y, "query.is_first_person", mc.options.getCameraType() == PointOfView.FIRST_PERSON);
        renderText(gui, poseStack, y, "query.is_in_water", player.isInWater());
        renderText(gui, poseStack, y, "query.is_in_water_or_rain", player.isInWaterRainOrBubble());
        renderText(gui, poseStack, y, "query.is_jumping", !player.abilities.flying && !player.isPassenger() && !player.isOnGround() && !player.isInWater());
        renderText(gui, poseStack, y, "query.is_on_fire", player.isOnFire());
        renderText(gui, poseStack, y, "query.is_on_ground", player.isOnGround());
        renderText(gui, poseStack, y, "query.is_playing_dead", player.isDeadOrDying());
        renderText(gui, poseStack, y, "query.is_riding", player.isPassenger());
        renderText(gui, poseStack, y, "query.is_sleeping", player.isSleeping());
        renderText(gui, poseStack, y, "query.is_sneaking", player.isOnGround() && player.getPose() == Pose.CROUCHING);
        renderText(gui, poseStack, y, "query.is_spectator", player.isSpectator());
        renderText(gui, poseStack, y, "query.is_sprinting", player.isSprinting());
        renderText(gui, poseStack, y, "query.is_swimming", player.isSwimming());
        renderText(gui, poseStack, y, "query.is_using_item", player.isUsingItem());
        renderText(gui, poseStack, y, "query.item_in_use_duration", () -> player.getTicksUsingItem() / 20.0);
        renderText(gui, poseStack, y, "query.item_max_use_duration", () -> getMaxUseDuration(player) / 20.0);
        renderText(gui, poseStack, y, "query.item_remaining_use_duration", () -> player.getUseItemRemainingTicks() / 20.0);

        renderText(gui, poseStack, y, "query.life_time", () -> (player.tickCount + partialTick) / 20.0);
        renderText(gui, poseStack, y, "query.max_health", player::getMaxHealth);
        renderText(gui, poseStack, y, "query.modified_distance_moved", () -> player.walkDist);
        renderText(gui, poseStack, y, "query.moon_phase", mc.level.getMoonPhase());

        renderText(gui, poseStack, y, "query.player_level", player.experienceLevel);
        renderText(gui, poseStack, y, "query.time_of_day", () -> MolangUtils.normalizeTime(mc.level.getDayTime()));
        renderText(gui, poseStack, y, "query.time_stamp", mc.level.getDayTime());
        renderText(gui, poseStack, y, "query.vertical_speed", () -> getVerticalSpeed(player));
        renderText(gui, poseStack, y, "query.walk_distance", () -> player.moveDist);
        renderText(gui, poseStack, y, "query.yaw_speed", () -> getYawSpeed(partialTick, player));

        renderText(gui, poseStack, y, "ysm.armor_value", player.getArmorValue());

        renderText(gui, poseStack, y, "ysm.has_helmet", getSlotValue(player, EquipmentSlotType.HEAD));
        renderText(gui, poseStack, y, "ysm.has_chest_plate", getSlotValue(player, EquipmentSlotType.CHEST));
        renderText(gui, poseStack, y, "ysm.has_leggings", getSlotValue(player, EquipmentSlotType.LEGS));
        renderText(gui, poseStack, y, "ysm.has_boots", getSlotValue(player, EquipmentSlotType.FEET));
        renderText(gui, poseStack, y, "ysm.has_mainhand", getSlotValue(player, EquipmentSlotType.MAINHAND));
        renderText(gui, poseStack, y, "ysm.has_offhand", getSlotValue(player, EquipmentSlotType.OFFHAND));

        renderText(gui, poseStack, y, "ysm.has_elytra", player.getItemBySlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA);
        renderText(gui, poseStack, y, "ysm.elytra_rot_x", () -> Math.toDegrees(player.elytraRotX));
        renderText(gui, poseStack, y, "ysm.elytra_rot_y", () -> Math.toDegrees(player.elytraRotY));
        renderText(gui, poseStack, y, "ysm.elytra_rot_z", () -> Math.toDegrees(player.elytraRotZ));

        renderText(gui, poseStack, y, "ysm.is_close_eyes", getEyeCloseState(partialTick, player));
        renderText(gui, poseStack, y, "ysm.is_riptide", player.isAutoSpinAttack());
        renderText(gui, poseStack, y, "ysm.food_level", player.getFoodData().getFoodLevel());

        if (FirstPersonCompat.isInstalled()) {
            renderText(gui, poseStack, y, "ysm.first_person_mod_hide", FirstPersonCompat.isHeadHide());
        }
    }

    private static boolean hasCape(ClientPlayerEntity player) {
        return player.isCapeLoaded() && !player.isInvisible() && player.isModelPartShown(PlayerModelPart.CAPE) && player.getCloakTextureLocation() != null;
    }

    private static int getEquipmentCount(ClientPlayerEntity player) {
        int count = 0;
        for (ItemStack s : player.getArmorSlots()) {
            if (!s.isEmpty()) {
                count += 1;
            }
        }
        return count;
    }

    private static double getMaxUseDuration(ClientPlayerEntity player) {
        ItemStack useItem = player.getUseItem();
        if (useItem.isEmpty()) {
            return 0.0;
        } else {
            return useItem.getUseDuration();
        }
    }

    private static void renderText(ForgeIngameGui gui, MatrixStack poseStack, int[] y, String name, String data) {
        FontRenderer font = gui.getFont();
        String s = I18n.get("molang.yes_steve_model.bg_width");
        if ((y[0] - 5) % 20 == 0) {
            Screen.fill(poseStack, 2, y[0] - 1, ReloadResourceEvent.DEBUG_BG_WIDTH, y[0] + 9, 0xc0505050);
        } else {
            Screen.fill(poseStack, 2, y[0] - 1, ReloadResourceEvent.DEBUG_BG_WIDTH, y[0] + 9, 0xc0506050);
        }
        font.draw(poseStack, name, 5, y[0], 0xffffff);
        font.draw(poseStack, data, 200, y[0], 0xffffff);
        font.draw(poseStack, new TranslationTextComponent(String.format("molang.yes_steve_model.%s.desc", name.toLowerCase(Locale.US))), 260, y[0], TextFormatting.GRAY.getColor());
        y[0] = y[0] + 10;
    }

    private static void renderText(ForgeIngameGui gui, MatrixStack poseStack, int[] y, String name, DoubleSupplier supplier) {
        renderText(gui, poseStack, y, name, String.format("§b%.4f", supplier.getAsDouble()));
    }

    private static void renderText(ForgeIngameGui gui, MatrixStack poseStack, int[] y, String name, int number) {
        renderText(gui, poseStack, y, name, String.format("§6%d", number));
    }

    private static void renderText(ForgeIngameGui gui, MatrixStack poseStack, int[] y, String name, long number) {
        renderText(gui, poseStack, y, name, String.format("§1%d", number));
    }

    private static void renderText(ForgeIngameGui gui, MatrixStack poseStack, int[] y, String name, boolean data) {
        String str = data ? String.format("§c%b", data) : String.format("§a%b", data);
        renderText(gui, poseStack, y, name, str);
    }

    private static float getYawSpeed(float partialTick, ClientPlayerEntity player) {
        double seekTime = player.tickCount + partialTick;
        return player.getViewYRot((float) seekTime - player.getViewYRot((float) seekTime - 0.1f));
    }

    private static float getGroundSpeed(ClientPlayerEntity player) {
        Vector3d velocity = player.getDeltaMovement();
        return 20 * MathHelper.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));
    }

    private static float getVerticalSpeed(ClientPlayerEntity player) {
        return 20 * (float) (player.position().y - player.yo);
    }

    private static boolean getEyeCloseState(float partialTick, ClientPlayerEntity player) {
        double remainder = (player.tickCount + partialTick + Math.abs(player.getUUID().getLeastSignificantBits()) % 10) % 90;
        boolean isBlinkTime = 85 < remainder && remainder < 90;
        return player.isSleeping() || isBlinkTime;
    }

    private static boolean getSlotValue(ClientPlayerEntity player, EquipmentSlotType slot) {
        return !player.getItemBySlot(slot).isEmpty();
    }
}
