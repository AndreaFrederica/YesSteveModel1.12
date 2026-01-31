package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.model.CustomPlayerModel;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerElytraLayer;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerItemInHandLayer;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<CustomPlayerEntity> {
    private GeoModel geoModel;

    @SuppressWarnings("all")
    public CustomPlayerRenderer(RenderManager ctx) {
        super(ctx, new CustomPlayerModel(), new CustomPlayerEntity());
        this.addLayer(new CustomPlayerItemInHandLayer<>(this));
        this.addLayer(new CustomPlayerElytraLayer<>(this));
    }

    @Override
    public void doRender(@Nonnull EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (this.animatable != null && entity instanceof EntityPlayer player) {
            CapabilityEvent.getCapability(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                this.animatable.setPlayer(player);
                this.animatable.setMainModel(ModelIdUtil.getMainId(cap.getModelId()));
                this.animatable.setTexture(cap.getSelectTexture());
            });
            if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(player, this.animatable, ModelIdUtil.getModelIdFromMainId(this.animatable.getMainModel())))) {
                return;
            }
        }
        ResourceLocation location = this.modelProvider.getModelLocation(animatable);
        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(location);
        if (geoModel != null) {
            this.geoModel = geoModel;
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
    }

    @Override
    public float getWidthScale(Object animatable) {
        if (this.animatable != null) {
            return this.animatable.getWidthScale();
        }
        return super.getWidthScale(animatable);
    }

    @Override
    public float getHeightScale(Object animatable) {
        if (this.animatable != null) {
            return this.animatable.getHeightScale();
        }
        return super.getHeightScale(animatable);
    }

    public CustomPlayerEntity getCustomPlayerEntity() {
        return this.animatable;
    }

    @Nullable
    public GeoModel getGeoModel() {
        return geoModel;
    }
}
