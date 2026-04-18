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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;

public abstract class GeoProjectilesRenderer<T extends Entity, E extends AnimatableEntity<T>> extends Render<T> implements IGeoRenderer<T> {
    protected E currentAnimatable;
    private IRenderCycle currentModelRenderCycle = EModelRenderCycle.INITIAL;

    protected GeoProjectilesRenderer(RenderManager renderManager) {
        super(renderManager);
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

        this.setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(Interpolations.lerp(entity.prevRotationYaw, entity.rotationYaw, partialTick) - 90, 0, 1, 0);
        GlStateManager.rotate(Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTick), 0, 0, 1);

        /// {@link net.minecraft.client.renderer.entity.RenderArrow#doRender(EntityArrow, double, double, double, float, float)}
        // 箭矢不摇晃似乎是预期行为，先注释掉
        /*
        if (entity instanceof EntityArrow arrow) {
            float deltaShake = arrow.arrowShake - partialTick;
            if (deltaShake > 0.0F) {
                GlStateManager.rotate(-MathHelper.sin(deltaShake * 3.0F) * deltaShake, 0.0F, 0.0F, 1.0F);
            }
        }
        */

        EntityModelData entityModelData = new EntityModelData();
        AnimationEvent<E> predicate = new AnimationEvent<>(animatable, 0, 0, partialTick, false, Collections.singletonList(entityModelData));
        AnimationContext<T> ctx = new AnimationContext<>(entity, animatable, predicate, entityModelData);
        animatable.setCustomAnimations(ctx, predicate);
        AnimatedGeoModel model = animatable.getCurrentModel();

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.translate(0, 0.01f, 0);
        boolean scoreTeamColor = false;
        if (this.renderOutlines) {
            scoreTeamColor = this.setScoreTeamColor(entity);
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        Color renderColor = this.getRenderColor(entity, partialTick);
        if (model != null) {
            boolean isVisible = this.isVisible(entity);
            boolean isGhost = !isVisible && !entity.isInvisibleToPlayer(mc.player);
            if ((isVisible || isGhost) && animatable.getTextureLocation() != null) {
                this.bindTexture(animatable.getTextureLocation());
                if (isGhost) GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                this.render(model, entity, partialTick,
                        (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
                        (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
                if (isGhost) GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
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
        super.doRender(entity, x, y, z, entityYaw, partialTick);
    }

    protected boolean isVisible(T entity) {
        return !entity.isInvisible() || this.renderOutlines;
    }

    /**
     * {@link net.minecraft.client.renderer.entity.RenderLivingBase#setScoreTeamColor(EntityLivingBase)}
     */
    @SuppressWarnings("JavadocReference")
    protected boolean setScoreTeamColor(T entityIn) {
        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    /**
     * {@link net.minecraft.client.renderer.entity.RenderLivingBase#unsetScoreTeamColor()}
     */
    @SuppressWarnings("JavadocReference")
    protected void unsetScoreTeamColor() {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
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
