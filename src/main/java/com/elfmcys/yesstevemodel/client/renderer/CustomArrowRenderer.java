package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.ArrowModelCapability;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.entity.CustomArrowEntity;
import com.elfmcys.yesstevemodel.client.entity.GeckoProjectileEntity;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoProjectilesRenderer;
import com.elfmcys.yesstevemodel.model.format.FormatManager;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class CustomArrowRenderer extends GeoProjectilesRenderer<EntityArrow, AnimatableEntity<EntityArrow>> {
    private final Map<EntityArrow, GeckoProjectileEntity<EntityArrow>> modernArrows = new WeakHashMap<>();

    public CustomArrowRenderer(RenderManager ctx) {
        super(ctx);
    }

    @Override
    public void doRender(@Nonnull EntityArrow entity, double x, double y, double z, float entityYaw, float partialTick) {
        CustomArrowEntity animatable = this.getLegacyAnimatableEntity(entity);
        Optional<ArrowModelCapability> optional = CapabilityEvent.getArrowModelCap(entity);
        if (!optional.isPresent()) {
            return;
        }
        ResourceLocation modelId = new ResourceLocation(optional.get().getModelId());
        ResourceLocation projectileId = EntityList.getKey(entity);
        if (projectileId != null && ClientModelManager.hasModernProjectileModel(modelId, projectileId)) {
            GeckoProjectileEntity<EntityArrow> modern = this.modernArrows.computeIfAbsent(entity, arrow -> new GeckoProjectileEntity<>(arrow, 60));
            modern.setProjectileModel(modelId, projectileId);
            modern.installControllers();
            super.render(entity, modern, x, y, z, entityYaw, partialTick);
            return;
        }
        animatable.setModelLocation(ModelIdUtil.getArrowId(modelId));
        animatable.setTextureLocation(ModelIdUtil.getSubModelId(modelId, FormatManager.ARROW_TEXTURE_FILE_NAME));
        super.render(entity, animatable, x, y, z, entityYaw, partialTick);
    }

    @Nonnull
    @Override
    public AnimatableEntity<EntityArrow> getAnimatableEntity(EntityArrow entity) {
        return getLegacyAnimatableEntity(entity);
    }

    private CustomArrowEntity getLegacyAnimatableEntity(EntityArrow entity) {
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
