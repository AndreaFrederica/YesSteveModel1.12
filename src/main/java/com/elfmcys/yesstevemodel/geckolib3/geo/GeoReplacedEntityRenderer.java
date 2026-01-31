package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.elfmcys.yesstevemodel.mclib.utils.Interpolations;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class GeoReplacedEntityRenderer<T extends IAnimatable> extends Render<EntityLivingBase> implements IGeoRenderer {
    protected static final Map<Class<? extends IAnimatable>, GeoReplacedEntityRenderer> renderers = new ConcurrentHashMap<>();

    static {
        AnimationController.addModelFetcher((IAnimatable object) -> {
            GeoReplacedEntityRenderer renderer = renderers.get(object.getClass());
            return renderer == null ? null : renderer.getGeoModelProvider();
        });
    }

    protected final AnimatedGeoModel<IAnimatable> modelProvider;
    protected final List<GeoLayerRenderer> layerRenderers = new ObjectArrayList<>();
    protected T animatable;
    protected IAnimatable currentAnimatable;
    protected float widthScale = 1;
    protected float heightScale = 1;
    private IRenderCycle currentModelRenderCycle = EModelRenderCycle.INITIAL;

    public GeoReplacedEntityRenderer(RenderManager renderManager,
                                     AnimatedGeoModel<IAnimatable> modelProvider, T animatable) {
        super(renderManager);
        this.modelProvider = modelProvider;
        this.animatable = animatable;
        renderers.putIfAbsent(animatable.getClass(), this);
    }

    public static void registerReplacedEntity(Class<? extends IAnimatable> itemClass,
                                              GeoReplacedEntityRenderer renderer) {
        renderers.put(itemClass, renderer);
    }

    public static GeoReplacedEntityRenderer getRenderer(Class<? extends IAnimatable> animatableClass) {
        return renderers.get(animatableClass);
    }

    @SuppressWarnings("unused")
    private static float getFacingAngle(EnumFacing facingIn) {
        return switch (facingIn) {
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            default -> 0.0F;
        };
    }

    @Override
    @Nonnull
    public IRenderCycle getCurrentModelRenderCycle() {
        return this.currentModelRenderCycle;
    }

    @Override
    public void setCurrentModelRenderCycle(IRenderCycle currentModelRenderCycle) {
        this.currentModelRenderCycle = currentModelRenderCycle;
    }

    @Override
    public float getWidthScale(Object animatable) {
        return this.widthScale;
    }

    @Override
    public float getHeightScale(Object entity) {
        return this.heightScale;
    }

    @Override
    public void doRender(
            @Nonnull EntityLivingBase entity,
            double x, double y, double z,
            float entityYaw, float partialTicks
    ) {
        this.doRender(entity, this.animatable, x, y, z, entityYaw, partialTicks);
    }

    public void doRender(
            @Nonnull EntityLivingBase entity, T animatable,
            double x, double y, double z,
            float entityYaw, float partialTick
    ) {
        this.currentAnimatable = animatable;
        // TODO: entity.isPassenger() looks redundant here
        boolean shouldSit = /* entity.isPassenger() && */ (entity.getRidingEntity() != null &&
                entity.getRidingEntity().shouldRiderSit());

        this.setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        EntityModelData entityModelData = new EntityModelData();
        entityModelData.isSitting = shouldSit;
        entityModelData.isChild = entity.isChild();

        float lerpBodyRot = Interpolations.lerpYaw(entity.prevRenderYawOffset, entity.renderYawOffset, partialTick);
        float lerpHeadRot = Interpolations.lerpYaw(entity.prevRotationYawHead, entity.rotationYawHead, partialTick);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (shouldSit && entity.getRidingEntity() instanceof EntityLivingBase vehicle) {
            lerpBodyRot = Interpolations.lerpYaw(vehicle.prevRenderYawOffset, vehicle.renderYawOffset, partialTick);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;
            if (clampedHeadYaw * clampedHeadYaw > 2500f) {
                lerpBodyRot += clampedHeadYaw * 0.2f;
            }
            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }

        /*
         * TODO: vanilla mobs can't sleep in beds in 1.12.2 and below if
         * (entity.getPose() == Pose.SLEEPING) { Direction direction =
         * entity.getBedDirection(); if (direction != null) { float f4 =
         * entity.getEyeHeight(Pose.STANDING) - 0.1F; stack.translate((double) ((float)
         * (-direction.getXOffset()) * f4), 0.0D, (double) ((float)
         * (-direction.getZOffset()) * f4)); } }
         */
        float lerpedAge = this.handleRotationFloat(entity, partialTick);
        this.applyRotations(entity, lerpedAge, lerpBodyRot, partialTick);

        float limbSwingAmount = 0.0F;
        float limbSwing = 0.0F;
        if (!shouldSit && entity.isEntityAlive()) {
            limbSwingAmount = Math.min(1, Interpolations.lerp(entity.prevLimbSwingAmount, entity.limbSwingAmount, partialTick));
            limbSwing = entity.limbSwing - entity.limbSwingAmount * (1 - partialTick);
            if (entity.isChild()) {
                limbSwing *= 3.0F;
            }
        }
        float headPitch = Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTick);
        entityModelData.headPitch = -headPitch;
        entityModelData.netHeadYaw = -netHeadYaw;
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(animatable));
        AnimationEvent predicate = new AnimationEvent(animatable, limbSwing, limbSwingAmount, partialTick,
                (limbSwingAmount <= -this.getSwingMotionAniMathHelperreshold() || limbSwingAmount <= this.getSwingMotionAniMathHelperreshold()), Collections.singletonList(entityModelData));

        this.modelProvider.setCustomAnimations(animatable, this.getInstanceId(entity), predicate);
        //GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0.01f, 0);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.getEntityTexture(entity));

        Color renderColor = this.getRenderColor(entity, partialTick);

        if (!entity.isInvisibleToPlayer(Minecraft.getMinecraft().player)) {
            this.render(model, entity, partialTick,
                    (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
                    (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
        }

        if (entity instanceof EntityPlayer player && !player.isSpectator()) {
            for (GeoLayerRenderer layerRenderer : this.layerRenderers) {
                layerRenderer.render(entity, limbSwing, limbSwingAmount, partialTick,
                        lerpedAge, netHeadYaw, headPitch, renderColor);
            }
        }
        if (entity instanceof EntityLiving entityLiving) {
            Entity leashHolder = entityLiving.getLeashHolder();
            if (leashHolder != null) {
                this.renderLeash(entityLiving, x, y, z, entityYaw, partialTick);
            }
        }
        //GlStateManager.popMatrix();
        GlStateManager.popMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTick);
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nullable EntityLivingBase entity) {
        return this.modelProvider.getTextureLocation(this.currentAnimatable);
    }

    @Override
    public AnimatedGeoModel getGeoModelProvider() {
        return this.modelProvider;
    }

    protected void applyRotations(EntityLivingBase entity, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        if (!entity.isPlayerSleeping()) {
            GlStateManager.rotate(180.0F - rotationYaw, 0, 1, 0);
        }

        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            GlStateManager.rotate(f * this.getDeathMaxRotation(entity), 0, 0, 1);
        }
        /*
         * TODO: probably doesn't exist in 1.12.2 as well else if
         * (entity.isSpinAttacking()) {
         * matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90.0F -
         * entity.rotationPitch));
         * matrixStackIn.rotate(Vector3f.YP.rotationDegrees(((float)
         * entity.ticksExisted + partialTicks) * -75.0F)); } else if (pose ==
         * Pose.SLEEPING) { Direction direction = entity.getBedDirection(); float
         * f1 = direction != null ? getFacingAngle(direction) : rotationYaw;
         * matrixStackIn.rotate(Vector3f.YP.rotationDegrees(f1));
         * matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(this.getDeathMaxRotation(
         * entity))); matrixStackIn.rotate(Vector3f.YP.rotationDegrees(270.0F)); }
         */
        else if (entity.hasCustomName() || entity instanceof EntityPlayer) {
            String name = TextFormatting.getTextWithoutFormattingCodes(entity.getName());
            if (entity instanceof EntityPlayer player && player.isWearing(EnumPlayerModelParts.CAPE)) {
                return;
            }
            if ("Dinnerbone".equals(name) || "Grumm".equals(name)) {
                GlStateManager.translate(0.0D, entity.height + 0.1F, 0.0D);
                GlStateManager.rotate(180, 0, 0, 1);
            }
        }
    }

    protected boolean isVisible(EntityLivingBase livingEntityIn) {
        return !livingEntityIn.isInvisible();
    }

    protected float getDeathMaxRotation(EntityLivingBase entityLivingBaseIn) {
        return 90.0F;
    }

    /**
     * Returns where in the swing animation the living entity is (from 0 to 1). Args
     * : entity, partialTickTime
     */
    protected float getSwingProgress(EntityLivingBase livingBase, float partialTickTime) {
        return livingBase.getSwingProgress(partialTickTime);
    }

    protected float getSwingMotionAniMathHelperreshold() {
        return 0.15f;
    }

    @Override
    public ResourceLocation getTextureLocation(Object animatable) {
        return this.modelProvider.getTextureLocation((IAnimatable) animatable);
    }

    public final boolean addLayer(GeoLayerRenderer<? extends EntityLivingBase> layer) {
        return this.layerRenderers.add(layer);
    }

    /**
     * Defines what float the third param in setRotationAngles of ModelBase is
     */
    protected float handleRotationFloat(EntityLivingBase livingBase, float partialTicks) {
        return (float) livingBase.ticksExisted + partialTicks;
    }

    protected void renderLeash(
            EntityLiving entityLivingIn,
            double x, double y, double z,
            float entityYaw, float partialTicks
    ) {
        @Nullable Entity entity = entityLivingIn.getLeashHolder();

        if (entity != null) {
            y = y - (1.6D - (double) entityLivingIn.height) * 0.5D;
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            double d0 = Interpolations.lerp(entity.prevRotationYaw, entity.rotationYaw, partialTicks * 0.5F)
                    * 0.01745329238474369D;
            double d1 = Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTicks * 0.5F)
                    * 0.01745329238474369D;
            double d2 = Math.cos(d0);
            double d3 = Math.sin(d0);
            double d4 = Math.sin(d1);

            if (entity instanceof EntityHanging) {
                d2 = 0.0D;
                d3 = 0.0D;
                d4 = -1.0D;
            }

            double d5 = Math.cos(d1);
            double d6 = Interpolations.lerp(entity.prevPosX, entity.posX, partialTicks) - d2 * 0.7D
                    - d3 * 0.5D * d5;
            double d7 = Interpolations.lerp(entity.prevPosY + (double) entity.getEyeHeight() * 0.7D,
                    entity.posY + (double) entity.getEyeHeight() * 0.7D, partialTicks) - d4 * 0.5D - 0.25D;
            double d8 = Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTicks) - d3 * 0.7D
                    + d2 * 0.5D * d5;
            double d9 = Interpolations.lerp(entityLivingIn.prevRenderYawOffset,
                    entityLivingIn.renderYawOffset, partialTicks) * 0.01745329238474369D
                    + (Math.PI / 2D);
            d2 = Math.cos(d9) * (double) entityLivingIn.width * 0.4D;
            d3 = Math.sin(d9) * (double) entityLivingIn.width * 0.4D;
            double d10 = Interpolations.lerp(entityLivingIn.prevPosX, entityLivingIn.posX, partialTicks)
                    + d2;
            double d11 = Interpolations.lerp(entityLivingIn.prevPosY, entityLivingIn.posY, partialTicks);
            double d12 = Interpolations.lerp(entityLivingIn.prevPosZ, entityLivingIn.posZ, partialTicks)
                    + d3;
            x = x + d2;
            z = z + d3;
            double d13 = (float) (d6 - d10);
            double d14 = (float) (d7 - d11);
            double d15 = (float) (d8 - d12);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

            for (int j = 0; j <= 24; ++j) {
                float f = 0.5F;
                float f1 = 0.4F;
                float f2 = 0.3F;

                if (j % 2 == 0) {
                    f *= 0.7F;
                    f1 *= 0.7F;
                    f2 *= 0.7F;
                }

                float f3 = (float) j / 24.0F;
                bufferbuilder
                        .pos(x + d13 * (double) f3 + 0.0D,
                                y + d14 * (double) (f3 * f3 + f3) * 0.5D
                                        + (double) ((24.0F - (float) j) / 18.0F + 0.125F),
                                z + d15 * (double) f3)
                        .color(f, f1, f2, 1.0F).endVertex();
                bufferbuilder
                        .pos(x + d13 * (double) f3 + 0.025D,
                                y + d14 * (double) (f3 * f3 + f3) * 0.5D
                                        + (double) ((24.0F - (float) j) / 18.0F + 0.125F) + 0.025D,
                                z + d15 * (double) f3)
                        .color(f, f1, f2, 1.0F).endVertex();
            }

            tessellator.draw();
            bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

            for (int k = 0; k <= 24; ++k) {
                float f4 = 0.5F;
                float f5 = 0.4F;
                float f6 = 0.3F;

                if (k % 2 == 0) {
                    f4 *= 0.7F;
                    f5 *= 0.7F;
                    f6 *= 0.7F;
                }

                float f7 = (float) k / 24.0F;
                bufferbuilder
                        .pos(x + d13 * (double) f7 + 0.0D,
                                y + d14 * (double) (f7 * f7 + f7) * 0.5D
                                        + (double) ((24.0F - (float) k) / 18.0F + 0.125F) + 0.025D,
                                z + d15 * (double) f7)
                        .color(f4, f5, f6, 1.0F).endVertex();
                bufferbuilder.pos(x + d13 * (double) f7 + 0.025D,
                        y + d14 * (double) (f7 * f7 + f7) * 0.5D + (double) ((24.0F - (float) k) / 18.0F + 0.125F),
                        z + d15 * (double) f7 + 0.025D).color(f4, f5, f6, 1.0F).endVertex();
            }

            tessellator.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableCull();
        }
    }
}
