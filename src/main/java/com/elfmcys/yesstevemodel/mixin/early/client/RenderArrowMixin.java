package com.elfmcys.yesstevemodel.mixin.early.client;

import com.elfmcys.yesstevemodel.capability.ArrowModelCapability;
import com.elfmcys.yesstevemodel.client.ClientProxy;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(RenderArrow.class)
public class RenderArrowMixin {
    @Inject(method = "doRender(Lnet/minecraft/entity/projectile/EntityArrow;DDDFF)V", at = @At("HEAD"), cancellable = true)
    public void render(EntityArrow pEntity, double x, double y, double z, float pEntityYaw, float pPartialTicks, CallbackInfo callback) {
        if (GeneralConfig.DISABLE_ARROWS_MODEL) return;
        Optional<ArrowModelCapability> optional = CapabilityEvent.getArrowModelCap(pEntity);
        if (optional.isPresent() && !optional.get().getModelId().equals(ArrowModelCapability.EMPTY)) {
            ResourceLocation arrowModelId = ModelIdUtil.getArrowId(new ResourceLocation(optional.get().getModelId()));
            GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(arrowModelId);
            if (geoModel != null) {
                ClientProxy.getArrowInstance().doRender(pEntity, x, y, z, pEntityYaw, pPartialTicks);
                callback.cancel();
            }
        }
    }
}
