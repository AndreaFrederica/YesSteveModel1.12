package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
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

/**
 * 可参考原版实现 {@link net.minecraft.client.renderer.entity.layers.LayerHeldItem}。
 */
//TODO：GL 状态
public class CustomPlayerItemInHandLayer<T extends EntityLivingBase & IAnimatable> extends GeoLayerRenderer<T> {
    //private final static String TAC_ID = "tac";

    public CustomPlayerItemInHandLayer(IGeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(@Nonnull T entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, Color renderColor) {
        if (this.entityRenderer.getGeoModel() == null) {
            return;
        }
        ItemStack offhandItem = entityLivingBaseIn.getHeldItemOffhand();
        ItemStack mainHandItem = entityLivingBaseIn.getHeldItemMainhand();
        GeoModel geoModel = this.entityRenderer.getGeoModel();
        if (!offhandItem.isEmpty() || !mainHandItem.isEmpty()) {
            if (!geoModel.rightHandBones.isEmpty()) {
                GlStateManager.pushMatrix();
                this.renderArmWithItem(entityLivingBaseIn, mainHandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
                GlStateManager.popMatrix();
            }
            if (!geoModel.leftHandBones.isEmpty()) {
                GlStateManager.pushMatrix();
                this.renderArmWithItem(entityLivingBaseIn, offhandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
                GlStateManager.popMatrix();
            }
//            if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(offhandItem)) {
//                GlStateManager.pushMatrix();
//                TacGunRenderer.renderOffhandGun(offhandItem, geoModel, entityLivingBaseIn, packedLightIn, partialTicks);
//                GlStateManager.popMatrix();
//            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    protected void renderArmWithItem(EntityLivingBase livingEntity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, EnumHandSide arm) {
        if (!itemStack.isEmpty()) {
            boolean isLeftHand = arm == EnumHandSide.LEFT;
            translateToHand(arm, this.entityRenderer.getGeoModel());
//            if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(itemStack)) {
//                if (!isLeftHand) {
//                    TacGunRenderer.renderMainhandGun(itemStack, livingEntity, light, partialTicks);
//                }
//            } else {
            GlStateManager.translate(0, -0.0625, -0.1);
            GlStateManager.rotate(-90.0F, 1, 0, 0);
            Minecraft.getMinecraft().getItemRenderer().renderItemSide(livingEntity, itemStack, transformType, isLeftHand);
//            }
        }
    }

    protected static void translateToHand(EnumHandSide arm, GeoModel geoModel) {
        if (arm == EnumHandSide.LEFT) {
            int size = geoModel.leftHandBones.size();
            for (int i = 0; i < size - 1; i++) {
                RenderUtils.prepMatrixForBone(geoModel.leftHandBones.get(i));
            }
            GeoBone lastBone = geoModel.leftHandBones.get(size - 1);
            RenderUtils.translateMatrixToBone(lastBone);
            RenderUtils.translateToPivotPoint(lastBone);
            RenderUtils.rotateMatrixAroundBone(lastBone);
            RenderUtils.scaleMatrixForBone(lastBone);
        } else {
            int size = geoModel.rightHandBones.size();
            for (int i = 0; i < size - 1; i++) {
                RenderUtils.prepMatrixForBone(geoModel.rightHandBones.get(i));
            }
            GeoBone lastBone = geoModel.rightHandBones.get(size - 1);
            RenderUtils.translateMatrixToBone(lastBone);
            RenderUtils.translateToPivotPoint(lastBone);
            RenderUtils.rotateMatrixAroundBone(lastBone);
            RenderUtils.scaleMatrixForBone(lastBone);
        }
    }
}
