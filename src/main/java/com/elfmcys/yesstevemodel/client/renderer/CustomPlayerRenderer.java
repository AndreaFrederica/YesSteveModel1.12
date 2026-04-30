package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerElytraLayer;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerItemInHandLayer;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerParrotOnShoulderLayer;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<EntityPlayer, CustomPlayerEntity> {
    public CustomPlayerRenderer(RenderManager ctx) {
        super(ctx);
        this.addLayer(new CustomPlayerItemInHandLayer<>());
        this.addLayer(new CustomPlayerElytraLayer<>());
        this.addLayer(new CustomPlayerParrotOnShoulderLayer<>(ctx));
    }

    @Override
    public void doRender(@Nonnull EntityPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        CustomPlayerEntity animatable = this.getAnimatableEntity(entity);
        CapabilityEvent.getModelInfoCap(entity).ifPresent(cap -> {
            animatable.setModelLocation(ModelIdUtil.getMainId(cap.getModelId()));
            animatable.setTextureLocation(cap.getSelectTexture());
        });
        if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(entity, animatable, ModelIdUtil.getModelIdFromMainId(animatable.getModelLocation())))) {
            return;
        }
        super.render(entity, animatable, x, y, z, entityYaw, partialTicks);
    }

    @Nonnull
    @Override
    public CustomPlayerEntity getAnimatableEntity(EntityPlayer entity) {
        return CapabilityEvent.getCustomPlayerEntityCap(entity)
                .orElseGet(() -> new CustomPlayerEntity(entity));
    }

    @Override
    public float getWidthScale(EntityPlayer entity) {
        if (this.currentAnimatable != null) {
            return this.currentAnimatable.getWidthScale();
        }
        return super.getWidthScale(entity);
    }

    @Override
    public float getHeightScale(EntityPlayer entity) {
        if (this.currentAnimatable != null) {
            return this.currentAnimatable.getHeightScale();
        }
        return super.getHeightScale(entity);
    }
}
