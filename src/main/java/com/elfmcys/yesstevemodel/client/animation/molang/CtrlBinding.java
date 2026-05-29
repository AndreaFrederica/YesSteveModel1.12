package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.client.animation.Priority;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.ContextFunction;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import rip.ysm.compat.immersivemelodies.ImmersiveMelodiesCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.Locale;
import java.util.function.Predicate;

public class CtrlBinding extends ContextBinding {
    public static final CtrlBinding INSTANCE = new CtrlBinding();

    private static final String PREFIX_ITEM_ID = "$";
    private static final String TYPE_PREFIX = ":";
    private static ReferenceArrayList<AnimationStatePredicate>[] data;

    private CtrlBinding() {
        this.registerLivingEntityState("death", Priority.HIGHEST, entity -> !entity.isEntityAlive());
        this.registerLivingEntityState("riptide", Priority.HIGHEST, entity -> false);
        this.registerLivingEntityState("sleep", Priority.HIGHEST, EntityLivingBase::isPlayerSleeping);
        this.registerState("swim", Priority.HIGHEST, ctx -> ctx.entity().isInWater() && isWalking(ctx));
        this.registerState("climb", Priority.HIGHEST, ctx -> ctx.entity().isOnLadder() && isWalking(ctx));
        this.registerLivingEntityState("climbing", Priority.HIGHEST, EntityLivingBase::isOnLadder);
        this.registerLivingEntityState("ladder_up", Priority.HIGHEST, entity -> entity.isOnLadder() && getVerticalVelocity(entity) > 0.0D);
        this.registerLivingEntityState("ladder_stillness", Priority.HIGHEST, entity -> entity.isOnLadder() && getVerticalVelocity(entity) == 0.0D);
        this.registerLivingEntityState("ladder_down", Priority.HIGHEST, entity -> entity.isOnLadder() && getVerticalVelocity(entity) < 0.0D);
        this.registerState("fly", Priority.HIGH, CtrlBinding::isFlying);
        this.registerLivingEntityState("elytra_fly", Priority.HIGH, EntityLivingBase::isElytraFlying);
        this.registerLivingEntityState("swim_stand", Priority.NORMAL, entity -> entity.isInWater() && !entity.onGround);
        this.registerLivingEntityState("attacked", Priority.NORMAL, entity -> entity.hurtTime > 0);
        this.registerLivingEntityState("jump", Priority.NORMAL, entity -> !entity.onGround && !entity.isInWater() && !entity.isOnLadder());
        this.registerState("sneak", Priority.NORMAL, ctx -> ctx.entity().onGround && ctx.entity().isSneaking() && isWalking(ctx));
        this.registerLivingEntityState("sneaking", Priority.NORMAL, entity -> entity.onGround && entity.isSneaking());
        this.registerLivingEntityState("run", Priority.LOW, entity -> entity.onGround && entity.isSprinting());
        this.registerState("walk", Priority.LOW, ctx -> ctx.entity().onGround && isWalking(ctx));
        this.registerLivingEntityState("idle", Priority.LOWEST, entity -> true);

        this.var("playing_extra_animation", CtrlBinding::isPlayingExtraAnimation);
        this.function("hold", HandFunction.createAlways());
        this.function("swing", HandFunction.createWhenSwinging());
        this.function("use", HandFunction.createWhenUsing());
        this.function("armor", new ArmorFunction());
        this.function("ride", new RideFunction());
        ImmersiveMelodiesCompat.registerBindings(this);

        this.constValue("state_continue", 2);
        this.constValue("state_stop", 3);
        this.constValue("state_pause", 4);
        this.constValue("state_bypass", 5);
        this.constValue("loop", 10);
        this.constValue("play_once", 11);
        this.constValue("hold_on_last_frame", 12);
        this.function("set_animation", new SetAnimationFunction());
        this.function("set_beginning_transition_length", new SetTransitionSpeedFunction());
        this.function("reset", new ResetFunction());
        this.function("indicate_reload", new IndicateReloadFunction());
    }

    private static boolean isFlying(IContext<EntityLivingBase> ctx) {
        EntityLivingBase entity = ctx.entity();
        return entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying;
    }

    private static boolean isPlayingExtraAnimation(IContext<Object> context) {
        if (!(context.geoInstance() instanceof CustomPlayerEntity customPlayerEntity)) {
            return false;
        }
        return customPlayerEntity.isModelSwitching()
                && customPlayerEntity.getAnimationState(PlayerAnimationController.CAP_CONTROLLER_KEY) != AnimationState.IDLE;
    }

    @SuppressWarnings("unchecked")
    private void registerState(String name, int priority, Predicate<IContext<EntityLivingBase>> predicate) {
        if (data == null) {
            data = new ReferenceArrayList[Priority.LOWEST + 1];
            for (int i = 0; i < data.length; i++) {
                data[i] = new ReferenceArrayList<>(6);
            }
        }
        data[priority].add(new AnimationStatePredicate(name, predicate));
        this.livingEntityVar(name, ctx -> evaluateState(name, ctx));
    }

    private void registerLivingEntityState(String name, int priority, EntityCondition predicate) {
        this.registerState(name, priority, predicate);
    }

    private static boolean evaluateState(String name, IContext<EntityLivingBase> context) {
        var tracker = context.geoInstance().getPositionTracker();
        if (tracker.getCachedModelId() != null) {
            return name.equals(tracker.getCachedModelId());
        }
        if (context.geoInstance() instanceof IPreviewAnimatable) {
            tracker.setCachedModelId("");
            return false;
        }
        Entity vehicle = context.entity().getRidingEntity();
        if (vehicle != null && vehicle.isEntityAlive()) {
            tracker.setCachedModelId("");
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            for (AnimationStatePredicate statePredicate : data[i]) {
                if (statePredicate.predicate.test(context)) {
                    tracker.setCachedModelId(statePredicate.name);
                    return statePredicate.name.equals(name);
                }
            }
        }
        tracker.setCachedModelId("");
        return false;
    }

    private static boolean isWalking(EntityLivingBase entity) {
        float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        float speed = Interpolations.lerp(entity.prevLimbSwingAmount, entity.limbSwingAmount, partialTick);
        if (Math.abs(speed) > 0.05f) {
            return true;
        }
        double dx = entity.posX - entity.prevPosX;
        double dz = entity.posZ - entity.prevPosZ;
        return dx * dx + dz * dz > 0.0025D;
    }

    private static double getVerticalVelocity(EntityLivingBase entity) {
        return 20.0D * (entity.posY - entity.prevPosY);
    }

    private static boolean isWalking(IContext<? extends EntityLivingBase> ctx) {
        if (ctx.animationEvent() != null && ctx.animationEvent().getLimbSwingAmount() > 0.01f) {
            return true;
        }
        float timeDelta = ctx.geoInstance().getPositionTracker().getTimeDelta();
        if (timeDelta > 0.0f) {
            Vec3d delta = ctx.geoInstance().getPositionTracker().getPositionDelta();
            double dx = delta.x / timeDelta;
            double dz = delta.z / timeDelta;
            if (dx * dx + dz * dz > 0.0025D) {
                return true;
            }
        }
        return isWalking(ctx.entity());
    }

    private static class AnimationStatePredicate {
        private final String name;
        private final Predicate<IContext<EntityLivingBase>> predicate;

        private AnimationStatePredicate(String name, Predicate<IContext<EntityLivingBase>> predicate) {
            this.name = name;
            this.predicate = predicate;
        }
    }

    private interface EntityCondition extends Predicate<IContext<EntityLivingBase>> {
        boolean check(EntityLivingBase entity);

        @Override
        default boolean test(IContext<EntityLivingBase> context) {
            return this.check(context.entity());
        }
    }

    private static EntityEquipmentSlot parseHandSlot(ExecutionContext<IContext<EntityLivingBase>> context, Function.ArgumentCollection arguments) {
        EntityEquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0));
        if (slotType != EntityEquipmentSlot.MAINHAND && slotType != EntityEquipmentSlot.OFFHAND) {
            return null;
        }
        return slotType;
    }

    private static boolean matchesItem(ItemStack stack, String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        if (stack.isEmpty()) {
            return "empty".equals(id);
        }
        if (id.startsWith(PREFIX_ITEM_ID)) {
            ResourceLocation actualId = ForgeRegistries.ITEMS.getKey(stack.getItem());
            return actualId != null && id.substring(1).equals(actualId.toString());
        }
        if (id.startsWith(TYPE_PREFIX)) {
            String expectedType = id.substring(1).toLowerCase(Locale.ENGLISH);
            return expectedType.equals(getItemType(stack));
        }
        ResourceLocation actualId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return actualId != null && id.equals(actualId.toString());
    }

    private static String getItemType(ItemStack stack) {
        Item item = stack.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ENGLISH);
        if (item instanceof ItemSword || path.contains("sword")) {
            return "sword";
        }
        if (item instanceof ItemAxe || path.contains("axe")) {
            return "axe";
        }
        if (item instanceof ItemPickaxe || path.contains("pickaxe")) {
            return "pickaxe";
        }
        if (item instanceof ItemSpade || path.contains("shovel")) {
            return "shovel";
        }
        if (item instanceof ItemHoe || path.contains("hoe")) {
            return "hoe";
        }
        if (item instanceof ItemShield || item == Items.SHIELD || path.contains("shield")) {
            return "shield";
        }
        if (item instanceof ItemBow || item == Items.BOW || path.contains("bow")) {
            return path.contains("crossbow") ? "crossbow" : "bow";
        }
        if (item instanceof ItemFishingRod || item == Items.FISHING_ROD || path.contains("fishing_rod")) {
            return "fishing_rod";
        }
        if (path.contains("trident") || path.contains("spear")) {
            return "spear";
        }
        EnumAction action = stack.getItemUseAction();
        if (action == EnumAction.BOW) {
            return "bow";
        }
        if (action == EnumAction.BLOCK) {
            return "shield";
        }
        return "";
    }

    private interface HandPredicate {
        boolean test(EntityLivingBase entity, EnumHand hand);
    }

    private static class HandFunction extends LivingEntityFunction {
        private final HandPredicate predicate;

        private HandFunction(HandPredicate predicate) {
            this.predicate = predicate;
        }

        static HandFunction createAlways() {
            return new HandFunction((entity, hand) -> true);
        }

        static HandFunction createWhenSwinging() {
            return new HandFunction((entity, hand) -> entity.isSwingInProgress && entity.swingingHand == hand && !entity.isPlayerSleeping());
        }

        static HandFunction createWhenUsing() {
            return new HandFunction((entity, hand) -> entity.isHandActive() && entity.getActiveHand() == hand && !entity.isPlayerSleeping());
        }

        @Override
        protected Object eval(ExecutionContext<IContext<EntityLivingBase>> context, ArgumentCollection arguments) {
            EntityEquipmentSlot slotType = parseHandSlot(context, arguments);
            if (slotType == null || arguments.size() < 2) {
                return 0;
            }
            EntityLivingBase entity = context.entity().entity();
            EnumHand hand = slotType == EntityEquipmentSlot.OFFHAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            if (!this.predicate.test(entity, hand)) {
                return 0;
            }
            ItemStack stack = entity.getItemStackFromSlot(slotType);
            return matchesItem(stack, arguments.getAsString(context, 1)) ? 1 : 0;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 2 || size == 3;
        }
    }

    private static class ArmorFunction extends LivingEntityFunction {
        @Override
        protected Object eval(ExecutionContext<IContext<EntityLivingBase>> context, ArgumentCollection arguments) {
            EntityEquipmentSlot slotType = MolangUtils.parseSlotType(context.entity(), arguments.getAsString(context, 0));
            if (slotType == null || slotType.getSlotType() != EntityEquipmentSlot.Type.ARMOR || arguments.size() < 2) {
                return 0;
            }
            return matchesItem(context.entity().entity().getItemStackFromSlot(slotType), arguments.getAsString(context, 1)) ? 1 : 0;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 2 || size == 3;
        }
    }

    private static class RideFunction extends LivingEntityFunction {
        @Override
        protected Object eval(ExecutionContext<IContext<EntityLivingBase>> context, ArgumentCollection arguments) {
            Entity riding = context.entity().entity().getRidingEntity();
            if (riding == null || arguments.size() < 1) {
                return 0;
            }
            ResourceLocation id = EntityList.getKey(riding);
            return id != null && arguments.getAsString(context, 0).equals(id.toString()) ? 1 : 0;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size >= 1;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class SetAnimationFunction extends ContextFunction<Object> {
        @Override
        protected Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
            IAnimationController controller = context.entity().animationEvent().getController();
            if (controller == null) {
                return null;
            }
            String animationName = arguments.getAsString(context, 0);
            if (StringUtils.isEmpty(animationName)) {
                return null;
            }
            ILoopType loopType;
            if (arguments.size() == 1) {
                loopType = null;
            } else {
                int loopInt = arguments.getAsInt(context, 1);
                switch (loopInt) {
                    case 10:
                        loopType = ILoopType.EDefaultLoopTypes.LOOP;
                        break;
                    case 11:
                        loopType = ILoopType.EDefaultLoopTypes.PLAY_ONCE;
                        break;
                    case 12:
                        loopType = ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
                        break;
                    default:
                        loopType = null;
                        break;
                }
            }
            if (loopType != null) {
                controller.setAnimation(animationName, loopType);
            } else {
                controller.setAnimation(animationName);
            }
            return null;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 1 || size == 2;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class SetTransitionSpeedFunction extends ContextFunction<Object> {
        @Override
        protected Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
            IAnimationController controller = context.entity().animationEvent().getController();
            if (controller == null) {
                return null;
            }
            float ticks = arguments.getAsFloat(context, 0);
            if (ticks < 0.0f) {
                return null;
            }
            controller.setTransitionLengthTicks(ticks);
            return null;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 1;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class ResetFunction extends ContextFunction<Object> {
        @Override
        protected Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
            IAnimationController controller = context.entity().animationEvent().getController();
            if (controller == null) {
                return null;
            }
            controller.clearAnimation();
            return null;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 0;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class IndicateReloadFunction extends ContextFunction<Object> {
        @Override
        protected Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
            IAnimationController controller = context.entity().animationEvent().getController();
            if (controller == null) {
                return null;
            }
            controller.stopTransition();
            return null;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 0;
        }
    }
}
