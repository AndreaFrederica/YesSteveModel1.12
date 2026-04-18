package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.ILocationBone;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.ILocationModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 可参考原版实现 {@link net.minecraft.client.renderer.entity.layers.LayerHeldItem}。
 */
public class CustomPlayerItemInHandLayer<T extends EntityLivingBase, E extends AnimatableEntity<T>> implements IGeoLayerRenderer<T, E> {
    //private final static String TAC_ID = "tac";

    public CustomPlayerItemInHandLayer() {
    }

    @Override
    public void render(
            @Nonnull T entity, @Nonnull E animatable,
            float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch, Color renderColor
    ) {
        ILocationModel geoModel = animatable.getCurrentModel();
        if (geoModel == null) return;
        renderArmWithItem(entity, geoModel.rightHandBones(), ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
        renderArmWithItem(entity, geoModel.leftHandBones(), ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
//        if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(offhandItem)) {
//            GlStateManager.pushMatrix();
//            TacGunRenderer.renderOffhandGun(offhandItem, geoModel, entityLivingBaseIn, packedLightIn, partialTicks);
//            GlStateManager.popMatrix();
//        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected static void renderArmWithItem(EntityLivingBase livingEntity, List<? extends ILocationBone> bones, ItemCameraTransforms.TransformType transformType, EnumHandSide arm) {
        if (bones.isEmpty()) return;
        boolean isLeftHand = arm == EnumHandSide.LEFT;
        // 先这样处理，YSM 定位组直接就叫主副手，不好改
        //ItemStack itemStack = livingEntity.getPrimaryHand() == arm ? livingEntity.getHeldItemMainhand() : livingEntity.getHeldItemOffhand();
        ItemStack itemStack = isLeftHand ? livingEntity.getHeldItemOffhand() : livingEntity.getHeldItemMainhand();
        if (itemStack.isEmpty()) return;

        GlStateManager.pushMatrix();
        // 缩放不为 0 才会渲染
        boolean scaleResult = RenderUtils.prepMatrixForLocator(bones);
        if (!scaleResult) {
            GlStateManager.translate(0, -0.0625, -0.1);
            GlStateManager.rotate(-90.0F, 1, 0, 0);
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(livingEntity, itemStack, transformType, isLeftHand);
        }
        GlStateManager.popMatrix();
    }
}
