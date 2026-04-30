package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.client.animation.molang.functions.*;
import com.elfmcys.yesstevemodel.client.animation.molang.variable.LadderFacingVariable;
import com.elfmcys.yesstevemodel.client.animation.molang.variable.MoveInputVariable;
import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.compat.ElytraCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query.EmptyFunction;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.mixininterface.EntityArrowAccessor;
import com.elfmcys.yesstevemodel.util.ComponentUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class YSMBinding extends ContextBinding {
    public static final YSMBinding INSTANCE = new YSMBinding();

    // TODO：补全
    private YSMBinding() {
        this.function("dump_equipped_item", new DumpEquippedItem());
        this.function("dump_relative_block", new DumpRelativeBlock());
        this.var("dump_mods", YSMBinding::dumpMods);
        this.entityVar("dump_effects", YSMBinding::dumpEffects);
        this.entityVar("dump_biome", YSMBinding::dumpBiome);

        this.function("mod_version", new ModVersion());
        this.function("equipped_enchantment_level", new EquippedEnchantmentLevel());
        this.function("effect_level", new EffectLevel());
        this.function("relative_block_name", new RelativeBlockName());
        this.function("relative_block_name_any", new RelativeBlockNameAny());

        this.function("bone_rot", new BoneRotation());
        this.function("bone_pos", new BonePosition());
        this.function("bone_scale", new BoneScale());
        this.function("bone_pivot_abs", new EmptyFunction());

        this.var("head_yaw", ctx -> ctx.data().netHeadYaw);
        this.var("head_pitch", ctx -> ctx.data().headPitch);
        this.var("weather", ctx -> getWeather(ctx.level()));
        this.var("dimension_name", ctx -> ctx.level().provider.getDimensionType().getName());
        this.var("fps", ctx -> Minecraft.getDebugFPS());
        this.var("time_delta", ctx -> 0.0);

        this.entityVar("ground_speed2", ctx -> 0.0);
        this.entityVar("input_vertical", MoveInputVariable::getVertical);
        this.entityVar("input_horizontal", MoveInputVariable::getHorizontal);
        this.entityVar("person_view", ctx -> 0);
        this.entityVar("rendering_in_paperdoll", ctx -> false);
        this.entityVar("rendering_in_inventory", ctx -> false);
        this.entityVar("block_light", ctx -> 0);
        this.entityVar("sky_light", ctx -> 0);

        this.entityVar("is_passenger", ctx -> ctx.entity().isRiding());
        this.entityVar("is_sneak", ctx -> ctx.entity().onGround && ctx.entity().isSneaking());
        this.entityVar("biome_category", ctx -> 0);
        this.entityVar("is_open_air", ctx -> isOpenAir(ctx.entity()));
        this.entityVar("eye_in_water", ctx -> ctx.entity().isInWater());
        this.entityVar("frozen_ticks", ctx -> 0);
        this.entityVar("air_supply", ctx -> ctx.entity().getAir());
        this.entityVar("delta_movement_length", ctx -> 0.0);

        this.livingEntityVar("has_helmet", ctx -> getSlotValue(ctx.entity(), EntityEquipmentSlot.HEAD));
        this.livingEntityVar("has_chest_plate", ctx -> getSlotValue(ctx.entity(), EntityEquipmentSlot.CHEST));
        this.livingEntityVar("has_leggings", ctx -> getSlotValue(ctx.entity(), EntityEquipmentSlot.LEGS));
        this.livingEntityVar("has_boots", ctx -> getSlotValue(ctx.entity(), EntityEquipmentSlot.FEET));
        this.livingEntityVar("has_mainhand", ctx -> getSlotValue(ctx.entity(), EntityEquipmentSlot.MAINHAND));
        this.livingEntityVar("has_offhand", ctx -> getSlotValue(ctx.entity(), EntityEquipmentSlot.OFFHAND));
        this.livingEntityVar("has_elytra", ctx -> ElytraCompat.isWearingElytra(ctx.entity()));
        this.livingEntityVar("is_riptide", ctx -> TridentCompat.isAutoSpinAttack(ctx.entity()));
        this.livingEntityVar("is_sleep", ctx -> ctx.entity().isPlayerSleeping());
        this.livingEntityVar("armor_value", ctx -> ctx.entity().getTotalArmorValue());
        this.livingEntityVar("hurt_time", ctx -> ctx.entity().hurtTime);
        this.livingEntityVar("is_close_eyes", ctx -> getEyeCloseState(ctx.animationEvent(), ctx.entity()));
        this.livingEntityVar("on_ladder", ctx -> ctx.entity().isOnLadder());
        this.livingEntityVar("ladder_facing", new LadderFacingVariable());
        this.livingEntityVar("arrow_count", ctx -> ctx.entity().getArrowCountInEntity());
        this.livingEntityVar("stinger_count", ctx -> 0);

        this.livingEntityVar("entity_type", ctx -> getEntityType(ctx.entity()));
        this.livingEntityVar("is_player", ctx -> "player".equals(getEntityType(ctx.entity())));
        this.livingEntityVar("is_maid", ctx -> "maid".equals(getEntityType(ctx.entity())));
        this.livingEntityVar("food_level", ctx -> getFoodLevel(ctx.entity()));
        this.livingEntityVar("xxa", ctx -> 0.0);
        this.livingEntityVar("yya", ctx -> 0.0);
        this.livingEntityVar("zza", ctx -> 0.0);
        this.livingEntityVar("mainhand_charged_crossbow", ctx -> CrossbowCompat.isCharged(ctx.entity().getHeldItemMainhand()));
        this.livingEntityVar("offhand_charged_crossbow", ctx -> CrossbowCompat.isCharged(ctx.entity().getHeldItemOffhand()));
        this.livingEntityVar("swinging", ctx -> ctx.entity().isSwingInProgress);
        this.livingEntityVar("swing_time", ctx -> ctx.entity().swingProgressInt);
        this.livingEntityVar("swinging_arm", ctx -> ctx.entity().swingingHand == EnumHand.MAIN_HAND ? 0 : 1);
        this.livingEntityVar("attack_time", ctx -> ctx.entity().getSwingProgress(ctx.animationEvent().getPartialTick()));

        this.playerEntityVar("texture_name", ctx -> StringUtils.EMPTY);
        this.playerEntityVar("first_person_mod_hide", ctx -> false);
        this.playerEntityVar("has_left_shoulder_parrot", ctx -> !ctx.entity().getLeftShoulderEntity().isEmpty());
        this.playerEntityVar("has_right_shoulder_parrot", ctx -> !ctx.entity().getRightShoulderEntity().isEmpty());
        this.playerEntityVar("left_shoulder_parrot_variant", ctx -> getParrotVariant(ctx.entity().getLeftShoulderEntity()));
        this.playerEntityVar("right_shoulder_parrot_variant", ctx -> getParrotVariant(ctx.entity().getRightShoulderEntity()));

        this.playerEntityVar("attack_damage", ctx -> ctx.entity().getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        this.playerEntityVar("attack_speed", ctx -> ctx.entity().getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue());
        this.playerEntityVar("attack_knockback", ctx -> 0.0);
        this.playerEntityVar("movement_speed", ctx -> ctx.entity().getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
        this.playerEntityVar("knockback_resistance", ctx -> ctx.entity().getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue());
        this.playerEntityVar("luck", ctx -> ctx.entity().getEntityAttribute(SharedMonsterAttributes.LUCK).getAttributeValue());

        this.playerEntityVar("block_reach", ctx -> ctx.entity().getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue());
        this.playerEntityVar("entity_reach", ctx -> 0.0);
        this.playerEntityVar("swim_speed", ctx -> ctx.entity().getEntityAttribute(EntityLivingBase.SWIM_SPEED).getAttributeValue());
        this.playerEntityVar("entity_gravity", ctx -> 0.0);
        this.playerEntityVar("step_height_addition", ctx -> 0.0);
        this.playerEntityVar("nametag_distance", ctx -> 0.0);
        this.playerEntityVar("is_fishing", ctx -> ctx.entity().fishEntity != null);
        this.playerEntityVar("in_shield_block_cooldown", ctx -> false);

        this.abstractClientPlayerVar("elytra_rot_x", ctx -> Math.toDegrees(ctx.entity().rotateElytraX));
        this.abstractClientPlayerVar("elytra_rot_y", ctx -> Math.toDegrees(ctx.entity().rotateElytraY));
        this.abstractClientPlayerVar("elytra_rot_z", ctx -> Math.toDegrees(ctx.entity().rotateElytraZ));

        this.localPlayerEntityVar("hit_target_id", YSMBinding::getHitTargetId);
        this.localPlayerEntityVar("hit_target_type", YSMBinding::getHitTargetType);

        this.function("first_order", new FirstOrderFunction());
        this.function("second_order", new SecondOrderFunction());
        this.function("particle", new EmptyFunction());
        this.function("abs_particle", new EmptyFunction());
        this.function("perlin_noise", new EmptyFunction());
        this.function("play_sound", new EmptyFunction());
        this.function("stop_sound", new EmptyFunction());
        this.function("stop_all_sounds", new EmptyFunction());
        this.function("keyboard", new EmptyFunction());
        this.function("mouse", new EmptyFunction());
        this.function("sync", new EmptyFunction());

        this.entityVar("projectile_owner", ctx -> ctx.createChild(getOwner(ctx.entity())));
        this.throwableEntityVar("throwable_item", ctx -> YSMBinding.getItem(ctx.entity()));
        this.fishingHookEntityVar("hooked_in", ctx -> getHookedIn(ctx.entity()));
        this.fishingHookEntityVar("is_biting", ctx -> ctx.entity().ticksCatchable > 0);
        this.arrowEntityVar("on_ground_time", ctx -> ctx.entity().timeInGround);
        this.arrowEntityVar("in_ground", ctx -> ctx.entity().inGround);
        this.arrowEntityVar("is_spectral_arrow", ctx -> ctx.entity() instanceof EntitySpectralArrow);
        this.entityVar("shoot_item_id", ctx -> ((EntityArrowAccessor) ctx.entity()).ysm$getShootItemId());

        // TODO：饰品栏
        this.function("has_any_curios", new EmptyFunction());
        this.function("has_any_curios_with_all_tags", new EmptyFunction());
        this.function("has_any_curios_with_any_tag", new EmptyFunction());
        this.livingEntityVar("dump_curios", ctx -> {
            ctx.debugOutput("Baubles not installed.");
            return null;
        });
    }

    private static int getFoodLevel(EntityLivingBase entity) {
        return entity instanceof EntityPlayer player ? player.getFoodStats().getFoodLevel() : 20;
    }

    private static boolean getEyeCloseState(AnimationEvent<?> animationEvent, EntityLivingBase player) {
        double remainder = (animationEvent.getAnimationTick() + Math.abs(player.getUniqueID().getLeastSignificantBits()) % 10) % 90;
        boolean isBlinkTime = 85 < remainder && remainder < 90;
        return player.isPlayerSleeping() || isBlinkTime;
    }

    private static boolean getSlotValue(EntityLivingBase entity, EntityEquipmentSlot slot) {
        return !entity.getItemStackFromSlot(slot).isEmpty();
    }

    private static int getWeather(WorldClient world) {
        if (world.isThundering()) {
            return 2;
        } else if (world.isRaining()) {
            return 1;
        }
        return 0;
    }

    private static boolean isOpenAir(Entity entity) {
        BlockPos blockpos = entity.getPosition();
        if (!entity.world.canSeeSky(blockpos)) {
            return false;
        }
        return entity.world.getPrecipitationHeight(blockpos).getY() <= blockpos.getY();
    }

    private static Object dumpMods(IContext<?> ctx) {
        if (!ctx.hasDebugOutput()) {
            return null;
        }

        Loader.instance().getModList().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ModContainer::getName))
                .forEach(info ->
                        ctx.debugOutput(new TextComponentString("Mod: display ").appendSibling(ComponentUtils.copyOnClickText(info.getName()))
                                .appendText("  id ").appendSibling(ComponentUtils.copyOnClickText(info.getModId()))));
        return null;
    }

    public static Object dumpEffects(IContext<Entity> ctx) {
        if (!ctx.hasDebugOutput()) {
            return null;
        }

        Entity entity = ctx.entity();
        Collection<PotionEffect> effects = null;
        if (entity instanceof EntityTippedArrow tippedArrow) {
            effects = tippedArrow.customPotionEffects;
        } else if (entity instanceof EntityLivingBase living) {
            effects = living.getActivePotionEffects();
        }

        if (effects != null) {
            for (PotionEffect effect : effects) {
                Potion potion = effect.getPotion();
                ResourceLocation potionId = potion.getRegistryName();
                if (potionId == null) {
                    continue;
                }
                ctx.debugOutput(new TextComponentString("Effect: display ").appendSibling(ComponentUtils.copyOnClickText(I18n.format(potion.getName())))
                        .appendText("  name ").appendSibling(ComponentUtils.copyOnClickText(potionId.toString()))
                        .appendText("  lv=").appendText(String.valueOf(effect.getAmplifier() + 1)));
            }
        }
        return null;
    }

    public static Object dumpBiome(IContext<Entity> ctx) {
        if (!ctx.hasDebugOutput()) {
            return null;
        }

        Entity entity = ctx.entity();
        var biome = entity.world.getBiome(entity.getPosition());
        ResourceLocation biomeId = biome.getRegistryName();
        if (biomeId == null) {
            return null;
        }
        ctx.debugOutput(new TextComponentString("Name ").appendSibling(ComponentUtils.copyOnClickText(biomeId.toString())));
        for (BiomeDictionary.Type type : BiomeDictionary.getTypes(biome)) {
            ctx.debugOutput(new TextComponentString("Type ").appendSibling(ComponentUtils.copyOnClickText(type.getName().toLowerCase(Locale.ENGLISH))));
        }
        return null;
    }

    private static String getHitTargetId(IContext<EntityPlayerSP> ctx) {
        Minecraft mc = Minecraft.getMinecraft();
        RayTraceResult hitResult = mc.objectMouseOver;
        if (hitResult == null) return StringUtils.EMPTY;
        return switch (hitResult.typeOfHit) {
            case BLOCK -> {
                //noinspection ConstantValue
                if (mc.world == null || hitResult.getBlockPos() == null) yield StringUtils.EMPTY;
                ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(mc.world.getBlockState(hitResult.getBlockPos()).getBlock());
                yield blockId != null ? blockId.toString() : StringUtils.EMPTY;
            }
            case ENTITY -> {
                if (hitResult.entityHit == null) yield StringUtils.EMPTY;
                ResourceLocation entityId = EntityList.getKey(hitResult.entityHit);
                yield entityId != null ? entityId.toString() : StringUtils.EMPTY;
            }
            default -> StringUtils.EMPTY;
        };
    }

    private static String getHitTargetType(IContext<EntityPlayerSP> ctx) {
        RayTraceResult hitResult = Minecraft.getMinecraft().objectMouseOver;
        if (hitResult == null) return StringUtils.EMPTY;
        return switch (hitResult.typeOfHit) {
            case BLOCK -> "block";
            case ENTITY -> "entity";
            default -> StringUtils.EMPTY;
        };
    }

    private static String getEntityType(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return "player";
        }
        String entityId = EntityList.getEntityString(entity);
        return entityId == null ? StringUtils.EMPTY : entityId;
    }

    @Nullable
    private static Entity getOwner(Entity entity) {
        if (entity instanceof EntityArrow arrow) {
            return arrow.shootingEntity;
        } else if (entity instanceof EntityThrowable throwable) {
            return throwable.getThrower();
        }
        return null;
    }

    @Nonnull
    private static String getItem(EntityThrowable throwable) {
        Item item = null;
        if (throwable instanceof EntityEgg) {
            item = Items.EGG;
        } else if (throwable instanceof EntityEnderPearl) {
            item = Items.ENDER_PEARL;
        } else if (throwable instanceof EntityExpBottle) {
            item = Items.EXPERIENCE_BOTTLE;
        } else if (throwable instanceof EntityPotion) {
            item = Items.POTIONITEM;
        } else if (throwable instanceof EntitySnowball) {
            item = Items.SNOWBALL;
        }
        if (item != null) {
            ResourceLocation itemId = item.getRegistryName();
            if (itemId != null) {
                return itemId.toString();
            }
        }
        return StringUtils.EMPTY;
    }

    private static String getHookedIn(EntityFishHook fishHook) {
        String entityId = EntityList.getEntityString(fishHook.caughtEntity);
        return entityId == null ? StringUtils.EMPTY : entityId;
    }

    private static String getParrotVariant(NBTTagCompound shoulderCompound) {
        /// {@link net.minecraft.client.renderer.entity.layers.LayerEntityOnShoulder#doRenderLayer(EntityPlayer, float, float, float, float, float, float, float)}
        if (EntityList.getClassFromName(shoulderCompound.getString("id")) == EntityParrot.class) {
            return switch (shoulderCompound.getInteger("Variant")) {
                case 0 -> "red_blue";
                case 1 -> "blue";
                case 2 -> "green";
                case 3 -> "yellow_blue";
                case 4 -> "grey";
                default -> "empty";
            };
        }
        return "empty";
    }
}
