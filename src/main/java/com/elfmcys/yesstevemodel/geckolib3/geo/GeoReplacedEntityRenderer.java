package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.Interpolations;
import com.elfmcys.yesstevemodel.mixininterface.RenderLivingBaseAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class GeoReplacedEntityRenderer<T extends EntityLivingBase, E extends AnimatableEntity<T>> extends RenderLivingBase<T> implements IGeoRenderer<T> {
    protected final List<IGeoLayerRenderer<T, E>> layerRenderers = new ObjectArrayList<>();
    protected E currentAnimatable;
    private IRenderCycle currentModelRenderCycle = EModelRenderCycle.INITIAL;

    protected GeoReplacedEntityRenderer(RenderManager renderManager) {
        super(renderManager, new ModelPlayer(0.0F, true), 0.5F);
    }

    @Nonnull
    public abstract E getAnimatableEntity(T entity);

    @Override
    public void doRender(
            @Nonnull T entity,
            double x, double y, double z,
            float entityYaw, float partialTick
    ) {
        this.render(entity, this.getAnimatableEntity(entity), x, y, z, entityYaw, partialTick);
    }

    public void render(
            @Nonnull T entity, @Nonnull E animatable,
            double x, double y, double z,
            float entityYaw, float partialTick
    ) {
        this.currentAnimatable = animatable;
        boolean shouldSit = entity.isRiding() && (entity.getRidingEntity() != null && entity.getRidingEntity().shouldRiderSit());

        this.setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);

        if (MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entity, this, partialTick, x, y, z))) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        this.renderLivingAt(entity, x, y, z);

        EntityModelData entityModelData = new EntityModelData();
        entityModelData.isSitting = shouldSit;
        entityModelData.isChild = entity.isChild();

        float lerpBodyRot = Interpolations.lerpYaw(entity.prevRenderYawOffset, entity.renderYawOffset, partialTick);
        float lerpHeadRot = Interpolations.lerpYaw(entity.prevRotationYawHead, entity.rotationYawHead, partialTick);
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        /// {@link RenderLivingBase#doRender(EntityLivingBase, double, double, double, float, float)}
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
        entityModelData.netHeadYaw = -MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw), -85, 85);
        AnimationEvent<E> predicate = new AnimationEvent<>(animatable, limbSwing, limbSwingAmount, partialTick,
                (limbSwingAmount <= -this.getSwingMotionAnimThreshold() || limbSwingAmount <= this.getSwingMotionAnimThreshold()), Collections.singletonList(entityModelData));
        AnimationContext<?> ctx = new AnimationContext<>(entity, animatable, predicate, entityModelData);
        animatable.setCustomAnimations(ctx, predicate);
        AnimatedGeoModel model = animatable.getCurrentModel();

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.translate(0, 0.01f, 0);
        boolean scoreTeamColor = false;
        boolean mainBrightness = false;
        if (this.renderOutlines) {
            scoreTeamColor = this.setScoreTeamColor(entity);
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        } else {
            mainBrightness = this.setDoRenderBrightness(entity, partialTick);
        }

        Color renderColor = this.getRenderColor(entity, partialTick);
        if (model != null) {
            boolean isVisible = this.isVisible(entity);
            boolean isGhost = !isVisible && !entity.isInvisibleToPlayer(mc.player);
            if ((isVisible || isGhost) && this.bindEntityTexture(entity)) {
                if (isGhost) GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                this.render(model, entity, partialTick,
                        (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
                        (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
                if (isGhost) GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
            }
        }

        if (!this.renderOutlines) {
            if (mainBrightness) this.unsetBrightness();
            GlStateManager.depthMask(true);
        }

        if (!(entity instanceof EntityPlayer player) || !player.isSpectator()) {
            for (IGeoLayerRenderer<T, E> layerRenderer : this.layerRenderers) {
                boolean layerBrightness = this.setBrightness(entity, partialTick, layerRenderer.shouldCombineTextures());
                layerRenderer.render(
                        entity, animatable,
                        limbSwing, limbSwingAmount,
                        partialTick, lerpedAge,
                        netHeadYaw, headPitch, renderColor
                );
                if (layerBrightness) this.unsetBrightness();
            }
        }

        if (this.renderOutlines) {
            if (scoreTeamColor) this.unsetScoreTeamColor();
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();

        // 嗯...TLM 这么实现，一定有它的道理吧
        if (this.renderManager.renderViewEntity != null) {
            ((RenderLivingBaseAccessor) this).ysm$renderNameTag(entity, x, y, z, entityYaw, partialTick);
        }

        /// {@link net.minecraft.client.renderer.entity.RenderLiving#doRender(EntityLiving, double, double, double, float, float)}
        if (!this.renderOutlines && entity instanceof EntityLiving mob) {
            Entity leashHolder = mob.getLeashHolder();
            //noinspection ConstantValue
            if (leashHolder != null) {
                this.renderLeash(entity, x, y, z, entityYaw, partialTick, leashHolder);
            }
        }
        MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, this, partialTick, x, y, z));
    }

    protected float getSwingMotionAnimThreshold() {
        return 0.15f;
    }

    public void addLayer(IGeoLayerRenderer<T, E> layer) {
        this.layerRenderers.add(layer);
    }

    public List<IGeoLayerRenderer<T, E>> getLayerRenderers() {
        return this.layerRenderers;
    }

    /**
     * {@link net.minecraft.client.renderer.entity.RenderPlayer#renderLivingAt(AbstractClientPlayer, double, double, double)}
     */
    @SuppressWarnings("JavadocReference")
    @Override
    protected void renderLivingAt(@Nonnull T entity, double x, double y, double z) {
        if (entity instanceof EntityPlayer player && player.isEntityAlive() && player.isPlayerSleeping()) {
            super.renderLivingAt(entity, x + player.renderOffsetX, y + player.renderOffsetY, z + player.renderOffsetZ);
        } else {
            super.renderLivingAt(entity, x, y, z);
        }
    }

    /**
     * {@link net.minecraft.client.renderer.entity.RenderPlayer#applyRotations(AbstractClientPlayer, float, float, float)}
     */
    @SuppressWarnings("JavadocReference")
    @Override
    protected void applyRotations(@Nonnull T entity, float ageInTicks, float rotationYaw, float partialTicks) {
        if (entity instanceof EntityPlayer player && entity.isEntityAlive() && entity.isPlayerSleeping()) {
            GlStateManager.rotate(player.getBedOrientationInDegrees(), 0, 1, 0);
            GlStateManager.rotate(this.getDeathMaxRotation(entity), 0, 0, 1);
            GlStateManager.rotate(270.0F, 0, 1, 0);
            return;
        }
        int deathTime = entity.deathTime;
        entity.deathTime = 0;
        super.applyRotations(entity, ageInTicks, rotationYaw, partialTicks);
        entity.deathTime = deathTime;
    }

    /**
     * {@link net.minecraft.client.renderer.entity.RenderLiving#renderLeash(EntityLiving, double, double, double, float, float)}
     */
    @SuppressWarnings("JavadocReference")
    protected <TEntity extends Entity> void renderLeash(
            T entity, double x, double y, double z,
            float entityYaw, float partialTicks, @Nonnull TEntity leashHolder
    ) {
        y = y - (1.6D - (double) entity.height) * 0.5D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        double holderYawRad = Interpolations.lerp(leashHolder.prevRotationYaw, leashHolder.rotationYaw, partialTicks * 0.5F) * 0.01745329238474369D;
        double holderPitchRad = Interpolations.lerp(leashHolder.prevRotationPitch, leashHolder.rotationPitch, partialTicks * 0.5F) * 0.01745329238474369D;
        double cosHolderYaw = Math.cos(holderYawRad);
        double sinHolderYaw = Math.sin(holderYawRad);
        double sinHolderPitch = Math.sin(holderPitchRad);
        if (leashHolder instanceof EntityHanging) {
            cosHolderYaw = 0.0D;
            sinHolderYaw = 0.0D;
            sinHolderPitch = -1.0D;
        }
        double cosHolderPitch = Math.cos(holderPitchRad);

        double holderX = Interpolations.lerp(leashHolder.prevPosX, leashHolder.posX, partialTicks)
                - cosHolderYaw * 0.7D - sinHolderYaw * 0.5D * cosHolderPitch;
        double holderY = Interpolations.lerp(leashHolder.prevPosY + (double) leashHolder.getEyeHeight() * 0.7D,
                leashHolder.posY + (double) leashHolder.getEyeHeight() * 0.7D, partialTicks)
                - sinHolderPitch * 0.5D - 0.25D;
        double holderZ = Interpolations.lerp(leashHolder.prevPosZ, leashHolder.posZ, partialTicks)
                - sinHolderYaw * 0.7D + cosHolderYaw * 0.5D * cosHolderPitch;

        double entityYawRad = Interpolations.lerp(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks) * 0.01745329238474369D + (Math.PI / 2D);

        double neckOffsetX = Math.cos(entityYawRad) * (double) entity.width * 0.4D;
        double neckOffsetZ = Math.sin(entityYawRad) * (double) entity.width * 0.4D;
        double entityNeckX = Interpolations.lerp(entity.prevPosX, entity.posX, partialTicks) + neckOffsetX;
        double entityNeckY = Interpolations.lerp(entity.prevPosY, entity.posY, partialTicks);
        double entityNeckZ = Interpolations.lerp(entity.prevPosZ, entity.posZ, partialTicks) + neckOffsetZ;
        x += neckOffsetX;
        z += neckOffsetZ;
        double diffX = (float) (holderX - entityNeckX);
        double diffY = (float) (holderY - entityNeckY);
        double diffZ = (float) (holderZ - entityNeckZ);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        // 拴绳的一个面
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int segment = 0; segment <= 24; ++segment) {
            // 初始颜色
            float r = 0.5F;
            float g = 0.4F;
            float b = 0.3F;

            // 偶数段颜色减深
            if (segment % 2 == 0) {
                r *= 0.7F;
                g *= 0.7F;
                b *= 0.7F;
            }

            float progress = (float) segment / 24.0F;
            double hangOffset = progress * progress + progress;
            double yOffset = (24.0F - (float) segment) / 18.0F + 0.125F;

            buffer.pos(x + diffX * (double) progress + 0.0D,
                            y + diffY * hangOffset * 0.5D + yOffset,
                            z + diffZ * (double) progress)
                    .color(r, g, b, 1.0F).endVertex();
            buffer.pos(x + diffX * (double) progress + 0.025D,
                            y + diffY * hangOffset * 0.5D + yOffset + 0.025D,
                            z + diffZ * (double) progress)
                    .color(r, g, b, 1.0F).endVertex();
        }
        tessellator.draw();

        // 拴绳的另一个面
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int segment = 0; segment <= 24; ++segment) {
            float r = 0.5F;
            float g = 0.4F;
            float b = 0.3F;

            if (segment % 2 == 0) {
                r *= 0.7F;
                g *= 0.7F;
                b *= 0.7F;
            }

            float progress = (float) segment / 24.0F;
            double hangOffset = progress * progress + progress;
            double yOffset = (24.0F - (float) segment) / 18.0F + 0.125F;

            buffer.pos(x + diffX * (double) progress + 0.0D,
                            y + diffY * hangOffset * 0.5D + yOffset + 0.025D,
                            z + diffZ * (double) progress)
                    .color(r, g, b, 1.0F).endVertex();
            buffer.pos(x + diffX * (double) progress + 0.025D,
                            y + diffY * hangOffset * 0.5D + yOffset,
                            z + diffZ * (double) progress + 0.025D)
                    .color(r, g, b, 1.0F).endVertex();
        }
        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
    }

    /*
    IGeoRenderer
     */

    @Nonnull
    @Override
    public IRenderCycle getCurrentModelRenderCycle() {
        return this.currentModelRenderCycle;
    }

    @Override
    public void setCurrentModelRenderCycle(IRenderCycle currentModelRenderCycle) {
        this.currentModelRenderCycle = currentModelRenderCycle;
    }

    @Nonnull
    @Override
    public ResourceLocation getEntityTexture(@Nonnull T entity) {
        return this.currentAnimatable.getTextureLocation();
    }
}
