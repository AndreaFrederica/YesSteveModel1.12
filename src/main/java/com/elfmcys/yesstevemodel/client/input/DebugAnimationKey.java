package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.compat.ElytraCompat;
import com.elfmcys.yesstevemodel.client.compat.SwimmingCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.event.ReloadResourceEvent;
import com.elfmcys.yesstevemodel.client.util.EntityUtil;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.util.Locale;
import java.util.function.DoubleSupplier;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public class DebugAnimationKey {
    public static boolean DEBUG = false;

    public static final KeyBinding DEBUG_ANIMATION_KEY = new KeyBinding(
            "key.yes_steve_model.debug_animation.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            Keyboard.KEY_B,
            "key.category.yes_steve_model"
    );

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (DEBUG_ANIMATION_KEY.isPressed()) {
            DEBUG = !DEBUG;
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            if (player == null) {
                return;
            }
            if (DEBUG) {
                player.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.debug_animation.true"));
            } else {
                player.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.debug_animation.false"));
            }
        }
    }

    // TODO：改为类似新版的调试系统
    @SubscribeEvent
    public static void render(RenderGameOverlayEvent.Text event) {
        if (!DEBUG) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.showDebugInfo) {
            return;
        }
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }
        if (mc.ingameGUI == null) {
            return;
        }
        final GuiIngame gui = mc.ingameGUI;
        GlStateManager.pushMatrix();
        float partialTick = event.getPartialTicks();

        EntityPlayerSP player = mc.player;
        if (mc.world == null || player == null) {
            return;
        }

        /*
        这一段计算模拟的是 GeoReplacedEntityRenderer，需保持对等，以便于调试
         */
        float lerpBodyRot = Interpolations.lerpYaw(player.prevRenderYawOffset, player.renderYawOffset, partialTick);
        float lerpHeadRot = Interpolations.lerpYaw(player.prevRotationYawHead, player.rotationYawHead, partialTick);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;
        boolean shouldSit = player.isRiding() && (player.getRidingEntity() != null && player.getRidingEntity().shouldRiderSit());

        if (shouldSit && player.getRidingEntity() instanceof EntityLivingBase vehicle) {
            lerpBodyRot = Interpolations.lerpYaw(vehicle.prevRenderYawOffset, vehicle.renderYawOffset, partialTick);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;
            if (clampedHeadYaw * clampedHeadYaw > 2500f) {
                lerpBodyRot += clampedHeadYaw * 0.2f;
            }
            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }
        float headPitch = Interpolations.lerp(player.prevRotationPitch, player.rotationPitch, partialTick);
        final float outputHeadPitch = -headPitch;
        final float outputNetHeadYaw = -MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw), -85, 85);

        float limbSwingAmount = 0.0F;
        if (!shouldSit && player.isEntityAlive()) {
            limbSwingAmount = Math.min(1.0F, Interpolations.lerp(player.prevLimbSwingAmount, player.limbSwingAmount, partialTick));
        }
        CustomPlayerEntity animatable = CapabilityEvent.getCustomPlayerEntityCap(player).orElse(null);
        String expectedState = inferExpectedState(player, limbSwingAmount);
        String ctrlState = getCtrlState(animatable);
        String mainController = getControllerState(animatable, "player.main");
        final float debugLimbSwingAmount = limbSwingAmount;

        int[] y = {5};

        renderText(gui, y, "ysm.debug.summary", diagnoseState(expectedState, ctrlState, mainController, limbSwingAmount));
        renderText(gui, y, "ysm.debug.expected", expectedState);
        renderText(gui, y, "ysm.debug.ctrl_state", ctrlState);
        renderText(gui, y, "ysm.debug.player.main", mainController);
        renderText(gui, y, "ysm.debug.player.main.entry", getControllerEntryState(animatable, "player.main"));
        renderText(gui, y, "ysm.debug.limb_swing_amount", () -> debugLimbSwingAmount);
        renderText(gui, y, "ysm.debug.motion", String.format("§b%.4f, %.4f, %.4f", player.motionX, player.motionY, player.motionZ));
        renderText(gui, y, "ysm.debug.model", animatable != null ? animatable.getModelLocation().toString() : "§c<no animatable>");

        renderText(gui, y, "PI", String.format("§7%.4f", Math.PI));
        renderText(gui, y, "E", String.format("§7%.4f", Math.E));

        renderText(gui, y, "query.actor_count", mc.world.getLoadedEntityList().size());
        renderText(gui, y, "query.anim_time", () -> 0.0);

        renderText(gui, y, "query.body_x_rotation", () -> player.rotationPitch);
        renderText(gui, y, "query.body_y_rotation", () -> MathHelper.wrapDegrees(player.rotationYaw));
        renderText(gui, y, "query.cardinal_facing_2d", player.getHorizontalFacing().getHorizontalIndex());
        renderText(gui, y, "query.distance_from_camera", () -> EntityUtil.getCameraPosition(mc, partialTick).distanceTo(player.getPositionVector()));
        renderText(gui, y, "query.equipment_count", getEquipmentCount(player));
        renderText(gui, y, "query.eye_target_x_rotation", () -> EntityUtil.getViewXRot(player, partialTick));
        renderText(gui, y, "query.eye_target_y_rotation", () -> EntityUtil.getViewYRot(player, partialTick));
        renderText(gui, y, "query.ground_speed", () -> EntityUtil.getGroundSpeed(player));

        renderText(gui, y, "query.has_cape", hasCape(player));
        renderText(gui, y, "query.has_rider", !player.getPassengers().isEmpty());
        renderText(gui, y, "query.head_x_rotation", () -> outputNetHeadYaw);
        renderText(gui, y, "query.head_y_rotation", () -> outputHeadPitch);
        renderText(gui, y, "query.health", player::getHealth);
        renderText(gui, y, "query.hurt_time", player.hurtTime);

        renderText(gui, y, "query.is_eating", player.getActiveItemStack().getItemUseAction() == EnumAction.EAT);
        renderText(gui, y, "query.is_first_person", mc.gameSettings.thirdPersonView == 0);
        renderText(gui, y, "query.is_in_water", player.isInWater());
        renderText(gui, y, "query.is_in_water_or_rain", player.isWet());
        renderText(gui, y, "query.is_jumping", !player.capabilities.isFlying && !player.isRiding() && !player.onGround && !player.isInWater());
        renderText(gui, y, "query.is_on_fire", player.isBurning());
        renderText(gui, y, "query.is_on_ground", player.onGround);
        renderText(gui, y, "query.is_playing_dead", !player.isEntityAlive());
        renderText(gui, y, "query.is_riding", player.isRiding());
        renderText(gui, y, "query.is_sleeping", player.isPlayerSleeping());
        renderText(gui, y, "query.is_sneaking", player.onGround && player.isSneaking());
        renderText(gui, y, "query.is_spectator", player.isSpectator());
        renderText(gui, y, "query.is_sprinting", player.isSprinting());
        if (SwimmingCompat.hasSwimming()) {
            renderText(gui, y, "query.is_swimming", SwimmingCompat.isSwimming(player));
        }
        renderText(gui, y, "query.is_using_item", player.isHandActive());
        renderText(gui, y, "query.item_in_use_duration", () -> player.getItemInUseMaxCount() / 20.0);
        renderText(gui, y, "query.item_max_use_duration", () -> getMaxUseDuration(player) / 20.0);
        renderText(gui, y, "query.item_remaining_use_duration", () -> player.getItemInUseCount() / 20.0);

        renderText(gui, y, "query.life_time", () -> (player.ticksExisted + partialTick) / 20.0);
        renderText(gui, y, "query.max_health", player::getMaxHealth);
        renderText(gui, y, "query.modified_distance_moved", () -> player.distanceWalkedModified);
        renderText(gui, y, "query.moon_phase", mc.world.getMoonPhase());

        renderText(gui, y, "query.player_level", player.experienceLevel);
        renderText(gui, y, "query.time_of_day", () -> MolangUtils.normalizeTime(mc.world.getWorldTime()));
        renderText(gui, y, "query.time_stamp", mc.world.getWorldTime());
        renderText(gui, y, "query.vertical_speed", () -> EntityUtil.getVerticalSpeed(player));
        renderText(gui, y, "query.walk_distance", () -> player.distanceWalkedOnStepModified);
        renderText(gui, y, "query.yaw_speed", () -> getYawSpeed(partialTick, player));

        renderText(gui, y, "ysm.armor_value", player.getTotalArmorValue());

        renderText(gui, y, "ysm.has_helmet", getSlotValue(player, EntityEquipmentSlot.HEAD));
        renderText(gui, y, "ysm.has_chest_plate", getSlotValue(player, EntityEquipmentSlot.CHEST));
        renderText(gui, y, "ysm.has_leggings", getSlotValue(player, EntityEquipmentSlot.LEGS));
        renderText(gui, y, "ysm.has_boots", getSlotValue(player, EntityEquipmentSlot.FEET));
        renderText(gui, y, "ysm.has_mainhand", getSlotValue(player, EntityEquipmentSlot.MAINHAND));
        renderText(gui, y, "ysm.has_offhand", getSlotValue(player, EntityEquipmentSlot.OFFHAND));

        renderText(gui, y, "ysm.has_elytra", ElytraCompat.isWearingElytra(player));
        renderText(gui, y, "ysm.elytra_rot_x", () -> Math.toDegrees(player.rotateElytraX));
        renderText(gui, y, "ysm.elytra_rot_y", () -> Math.toDegrees(player.rotateElytraY));
        renderText(gui, y, "ysm.elytra_rot_z", () -> Math.toDegrees(player.rotateElytraZ));

        renderText(gui, y, "ysm.is_close_eyes", getEyeCloseState(partialTick, player));
        if (TridentCompat.hasRiptide()) {
            renderText(gui, y, "ysm.is_riptide", TridentCompat.isAutoSpinAttack(player));
        }
        renderText(gui, y, "ysm.food_level", player.getFoodStats().getFoodLevel());

//        if (FirstPersonCompat.isInstalled()) {
//            renderText(gui, y, "ysm.first_person_mod_hide", FirstPersonCompat.isHeadHide());
//        }
        GlStateManager.popMatrix();
    }

    private static String inferExpectedState(EntityPlayerSP player, float limbSwingAmount) {
        if (!player.isEntityAlive()) {
            return "death";
        }
        if (TridentCompat.hasRiptide() && TridentCompat.isAutoSpinAttack(player)) {
            return "riptide";
        }
        if (player.isPlayerSleeping()) {
            return "sleep";
        }
        if (SwimmingCompat.hasSwimming() && SwimmingCompat.isSwimming(player)) {
            return "swim";
        }
        if (player.isOnLadder() && isMoving(player, limbSwingAmount)) {
            return "climb";
        }
        if (player.isOnLadder()) {
            return "climbing";
        }
        if (player.capabilities.isFlying) {
            return "fly";
        }
        if (player.isElytraFlying()) {
            return "elytra_fly";
        }
        if (player.isInWater() && !player.onGround) {
            return "swim_stand";
        }
        if (player.hurtTime > 0) {
            return "attacked";
        }
        if (!player.onGround && !player.isInWater() && !player.isOnLadder()) {
            return "jump";
        }
        if (player.onGround && player.isSneaking() && isMoving(player, limbSwingAmount)) {
            return "sneak";
        }
        if (player.onGround && player.isSneaking()) {
            return "sneaking";
        }
        if (player.onGround && player.isSprinting()) {
            return "run";
        }
        if (player.onGround && isMoving(player, limbSwingAmount)) {
            return "walk";
        }
        return "idle";
    }

    private static boolean isMoving(EntityPlayerSP player, float limbSwingAmount) {
        if (limbSwingAmount > 0.01f) {
            return true;
        }
        double dx = player.posX - player.prevPosX;
        double dz = player.posZ - player.prevPosZ;
        return dx * dx + dz * dz > 0.0025D;
    }

    private static String getCtrlState(CustomPlayerEntity animatable) {
        if (animatable == null) {
            return "§c<no animatable>";
        }
        String state = animatable.getPositionTracker().getCachedModelId();
        return StringUtils.isBlank(state) ? "§7<empty>" : state;
    }

    private static String getControllerState(CustomPlayerEntity animatable, String name) {
        if (animatable == null) {
            return "§c<no animatable>";
        }
        IAnimationController<?> controller = animatable.getAnimationData().getAnimationControllerByName(name);
        if (controller == null) {
            return "§c<missing>";
        }
        return controller.getCurrentAnimationName();
    }

    private static String getControllerEntryState(CustomPlayerEntity animatable, String name) {
        if (animatable == null) {
            return "§c<no animatable>";
        }
        if (animatable.getAnimationEntries(name) != null) {
            return "§a" + name;
        }
        if (animatable.getAnimationEntries("main") != null) {
            return "§emain";
        }
        if (animatable.getModelAssembly() == null) {
            return "§c<no assembly>";
        }
        StringBuilder builder = new StringBuilder("§c<missing>");
        int shown = 0;
        for (String key : animatable.getModelAssembly().getAnimationBundle().getAnimationControllers().keySet()) {
            if (shown >= 3) {
                builder.append(" …");
                break;
            }
            builder.append(shown == 0 ? " " : ", ").append(key);
            shown += 1;
        }
        return builder.toString();
    }

    private static String diagnoseState(String expectedState, String ctrlState, String mainController, float limbSwingAmount) {
        if (ctrlState.contains("no animatable") || mainController.contains("missing")) {
            return "§c没有拿到玩家动画实例或 player.main 控制器";
        }
        if (!expectedState.equals(ctrlState) && limbSwingAmount > 0.01f) {
            return "§e实体已经在移动，但 ctrl 主状态没有切到预期状态";
        }
        if (expectedState.equals(ctrlState) && mainController.contains("idle") && !"idle".equals(expectedState)) {
            return "§e状态判定正确，但 player.main 仍停在 idle/内置动画";
        }
        return "§a状态判定链路正常，若仍罚站就查骨骼变换输出";
    }

    private static boolean hasCape(EntityPlayerSP player) {
        return player.hasPlayerInfo() && !player.isInvisible() && player.isWearing(net.minecraft.entity.player.EnumPlayerModelParts.CAPE) && player.getLocationCape() != null;
    }

    private static int getEquipmentCount(EntityPlayerSP player) {
        int count = 0;
        for (ItemStack s : player.getArmorInventoryList()) {
            if (!s.isEmpty()) {
                count += 1;
            }
        }
        return count;
    }

    private static double getMaxUseDuration(EntityPlayerSP player) {
        ItemStack useItem = player.getActiveItemStack();
        if (useItem.isEmpty()) {
            return 0.0;
        } else {
            return useItem.getMaxItemUseDuration();
        }
    }

    private static void renderText(GuiIngame gui, int[] y, String name, String data) {
        FontRenderer font = gui.getFontRenderer();
        if ((y[0] - 5) % 20 == 0) {
            Gui.drawRect(2, y[0] - 1, ReloadResourceEvent.DEBUG_BG_WIDTH, y[0] + 9, 0xc0505050);
        } else {
            Gui.drawRect(2, y[0] - 1, ReloadResourceEvent.DEBUG_BG_WIDTH, y[0] + 9, 0xc0506050);
        }
        gui.drawString(font, name, 5, y[0], 0xffffff);
        gui.drawString(font, data, 200, y[0], 0xffffff);
        gui.drawString(font, TextFormatting.GRAY + I18n.format(String.format("molang.yes_steve_model.%s.desc", name.toLowerCase(Locale.US))), 260, y[0], 0xFFFFFFFF);
        y[0] = y[0] + 10;
    }

    private static void renderText(GuiIngame gui, int[] y, String name, DoubleSupplier supplier) {
        renderText(gui, y, name, String.format("§b%.4f", supplier.getAsDouble()));
    }

    private static void renderText(GuiIngame gui, int[] y, String name, int number) {
        renderText(gui, y, name, String.format("§6%d", number));
    }

    private static void renderText(GuiIngame gui, int[] y, String name, long number) {
        renderText(gui, y, name, String.format("§1%d", number));
    }

    private static void renderText(GuiIngame gui, int[] y, String name, boolean data) {
        String str = data ? String.format("§c%b", data) : String.format("§a%b", data);
        renderText(gui, y, name, str);
    }

    private static float getYawSpeed(float partialTick, EntityPlayerSP player) {
        double seekTime = player.ticksExisted + partialTick;
        return EntityUtil.getViewYRot(player, (float) seekTime) - EntityUtil.getViewYRot(player, (float) seekTime - 0.1f);
    }

    private static boolean getEyeCloseState(float partialTick, EntityPlayerSP player) {
        double remainder = (player.ticksExisted + partialTick + Math.abs(player.getUniqueID().getLeastSignificantBits()) % 10) % 90;
        boolean isBlinkTime = 85 < remainder && remainder < 90;
        return player.isPlayerSleeping() || isBlinkTime;
    }

    private static boolean getSlotValue(EntityPlayerSP player, EntityEquipmentSlot slot) {
        return !player.getItemStackFromSlot(slot).isEmpty();
    }
}
