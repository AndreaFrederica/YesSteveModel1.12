package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nonnull;

public interface IGeoLayerRenderer<T extends EntityLivingBase, E extends AnimatableEntity<T>> {
    /**
     * 核心渲染方法，会在主模型渲染后调用。<br>
     * {@link net.minecraft.client.renderer.entity.layers.LayerRenderer#doRenderLayer(EntityLivingBase, float, float, float, float, float, float, float)}
     */
    void render(
            @Nonnull T entity, @Nonnull E animatable,
            float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch, Color renderColor
    );

    /**
     * 由外部调用，可影响是否要加上额外效果，如受击红光。<br>
     * {@link net.minecraft.client.renderer.entity.layers.LayerRenderer#shouldCombineTextures()}
     */
    default boolean shouldCombineTextures() {
        return false;
    }
}
