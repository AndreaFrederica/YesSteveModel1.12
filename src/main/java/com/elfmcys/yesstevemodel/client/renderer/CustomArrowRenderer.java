package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.ArrowModelCapability;
import com.elfmcys.yesstevemodel.client.entity.CustomArrowEntity;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoProjectilesRenderer;
import com.elfmcys.yesstevemodel.model.format.FormatManager;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CustomArrowRenderer extends GeoProjectilesRenderer<EntityArrow, CustomArrowEntity> {
    public CustomArrowRenderer(RenderManager ctx) {
        super(ctx);
    }

    @Override
    public void doRender(@Nonnull EntityArrow entity, double x, double y, double z, float entityYaw, float partialTick) {
        CustomArrowEntity animatable = this.getAnimatableEntity(entity);
        Optional<ArrowModelCapability> optional = CapabilityEvent.getArrowModelCap(entity);
        if (!optional.isPresent()) {
            return;
        }
        ResourceLocation modelId = new ResourceLocation(optional.get().getModelId());
        animatable.setModelLocation(ModelIdUtil.getArrowId(modelId));
        animatable.setTextureLocation(ModelIdUtil.getSubModelId(modelId, FormatManager.ARROW_TEXTURE_FILE_NAME));
        super.render(entity, animatable, x, y, z, entityYaw, partialTick);
    }

    @Nonnull
    @Override
    public CustomArrowEntity getAnimatableEntity(EntityArrow entity) {
        return CapabilityEvent.getCustomArrowEntityCap(entity)
                .orElseGet(() -> new CustomArrowEntity(entity));
    }

    @Override
    public float getWidthScale(EntityArrow entity) {
        return 0.7f;
    }

    @Override
    public float getHeightScale(EntityArrow entity) {
        return 0.7f;
    }
}
