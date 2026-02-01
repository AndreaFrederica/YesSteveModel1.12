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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GeoProjectilesRenderer<T extends IAnimatable> extends Render<Entity> implements IGeoRenderer {
    protected static final Map<Class<? extends IAnimatable>, GeoProjectilesRenderer> renderers = new ConcurrentHashMap<>();

    static {
        AnimationController.addModelFetcher((IAnimatable object) -> {
            GeoProjectilesRenderer renderer = renderers.get(object.getClass());
            return renderer == null ? null : renderer.getGeoModelProvider();
        });
    }

    private final AnimatedGeoModel modelProvider;
    protected T animatable;
    private IRenderCycle currentModelRenderCycle = EModelRenderCycle.INITIAL;

    public GeoProjectilesRenderer(RenderManager renderManager, AnimatedGeoModel<T> modelProvider, T animatable) {
        super(renderManager);
        this.modelProvider = modelProvider;
        this.animatable = animatable;
        renderers.putIfAbsent(animatable.getClass(), this);
    }

    @Override
    public void doRender(@Nonnull Entity entity, double x, double y, double z, float yaw, float partialTick) {
        GeoModel model = this.modelProvider.getModel(this.modelProvider.getModelLocation(this.animatable));
        this.setCurrentModelRenderCycle(EModelRenderCycle.INITIAL);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(Interpolations.lerp(entity.prevRotationYaw, entity.rotationYaw, partialTick) - 90, 0, 1, 0);
        GlStateManager.rotate(Interpolations.lerp(entity.prevRotationPitch, entity.rotationPitch, partialTick), 0, 0, 1);

        /*
        参考 RenderArrow
         */
        // 箭矢不摇晃似乎是预期行为，先注释掉
        /*
        if (entity instanceof EntityArrow arrow) {
            float deltaShake = arrow.arrowShake - partialTick;
            if (deltaShake > 0.0F) {
                GlStateManager.rotate(-MathHelper.sin(deltaShake * 3.0F) * deltaShake, 0.0F, 0.0F, 1.0F);
            }
        }
         */

        AnimationEvent<T> predicate = new AnimationEvent<>(this.animatable, 0, 0, partialTick, false, Collections.singletonList(new EntityModelData()));
        this.modelProvider.setCustomAnimations(this.animatable, this.getInstanceId(entity), predicate);
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(this.getTextureLocation(entity));
        Color renderColor = this.getRenderColor(entity, partialTick);
        if (mc.player != null && !entity.isInvisibleToPlayer(mc.player)) {
            this.render(
                    model, entity, partialTick,
                    (float) renderColor.getRed() / 255f, (float) renderColor.getBlue() / 255f,
                    (float) renderColor.getGreen() / 255f, (float) renderColor.getAlpha() / 255
            );
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, yaw, partialTick);
    }

    @Override
    public AnimatedGeoModel getGeoModelProvider() {
        return this.modelProvider;
    }

    @Override
    public ResourceLocation getTextureLocation(Object instance) {
        return this.modelProvider.getTextureLocation(instance);
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
    public ResourceLocation getEntityTexture(@Nullable Entity instance) {
        return this.modelProvider.getTextureLocation(this.animatable);
    }
}
