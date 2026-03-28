package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
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
public class CustomPlayerItemInHandLayer<T extends EntityLivingBase, R extends IGeoRenderer<T>> extends GeoLayerRenderer<T, R> {
    //private final static String TAC_ID = "tac";

    public CustomPlayerItemInHandLayer(R entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(@Nonnull T entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, Color renderColor) {
        GeoModel geoModel = this.entityRenderer.getGeoModel();
        if (geoModel == null) return;
        renderArmWithItem(entityLivingBaseIn, geoModel.rightHandBones, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
        renderArmWithItem(entityLivingBaseIn, geoModel.leftHandBones, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
//        if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(offhandItem)) {
//            GlStateManager.pushMatrix();
//            TacGunRenderer.renderOffhandGun(offhandItem, geoModel, entityLivingBaseIn, packedLightIn, partialTicks);
//            GlStateManager.popMatrix();
//        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected static void renderArmWithItem(EntityLivingBase livingEntity, List<GeoBone> bones, ItemCameraTransforms.TransformType transformType, EnumHandSide arm) {
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
