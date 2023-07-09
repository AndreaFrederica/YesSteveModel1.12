package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.compat.TacGunRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fml.ModList;

public class CustomPlayerItemInHandLayer<T extends LivingEntity & IAnimatable> extends GeoLayerRenderer<T> {
    private final static String TAC_ID = "tac";

    public CustomPlayerItemInHandLayer(IGeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    @Keep
    public void render(MatrixStack poseStack, IRenderTypeBuffer bufferIn, int packedLightIn, T entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityRenderer.getGeoModel() == null) {
            return;
        }
        ItemStack offhandItem = entityLivingBaseIn.getOffhandItem();
        ItemStack mainHandItem = entityLivingBaseIn.getMainHandItem();
        GeoModel geoModel = entityRenderer.getGeoModel();
        if (!offhandItem.isEmpty() || !mainHandItem.isEmpty()) {
            if (!geoModel.rightHandBones.isEmpty()) {
                poseStack.pushPose();
                this.renderArmWithItem(entityLivingBaseIn, mainHandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, poseStack, bufferIn, packedLightIn, partialTicks);
                poseStack.popPose();
            }
            if (!geoModel.leftHandBones.isEmpty()) {
                poseStack.pushPose();
                this.renderArmWithItem(entityLivingBaseIn, offhandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, poseStack, bufferIn, packedLightIn, partialTicks);
                poseStack.popPose();
            }
            if (ModList.get().isLoaded(TAC_ID) && TacGunRenderer.isGun(offhandItem)) {
                poseStack.pushPose();
                TacGunRenderer.renderOffhandGun(offhandItem, geoModel, entityLivingBaseIn, poseStack, packedLightIn, partialTicks);
                poseStack.popPose();
            }
        }
    }

    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, HandSide arm, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int light, float partialTicks) {
        if (!itemStack.isEmpty()) {
            boolean isLeftHand = arm == HandSide.LEFT;
            translateToHand(arm, poseStack, this.entityRenderer.getGeoModel());
            if (ModList.get().isLoaded(TAC_ID) && TacGunRenderer.isGun(itemStack)) {
                if (!isLeftHand) {
                    TacGunRenderer.renderMainhandGun(itemStack, livingEntity, poseStack, light, partialTicks);
                }
            } else {
                poseStack.translate(0, -0.0625, -0.1);
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
                Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, transformType, isLeftHand, poseStack, bufferSource, light);
            }
        }
    }

    protected void translateToHand(HandSide arm, MatrixStack poseStack, GeoModel geoModel) {
        if (arm == HandSide.LEFT) {
            int size = geoModel.leftHandBones.size();
            for (int i = 0; i < size - 1; i++) {
                RenderUtils.prepMatrixForBone(poseStack, geoModel.leftHandBones.get(i));
            }
            GeoBone lastBone = geoModel.leftHandBones.get(size - 1);
            RenderUtils.translateMatrixToBone(poseStack, lastBone);
            RenderUtils.translateToPivotPoint(poseStack, lastBone);
            RenderUtils.rotateMatrixAroundBone(poseStack, lastBone);
            RenderUtils.scaleMatrixForBone(poseStack, lastBone);
        } else {
            int size = geoModel.rightHandBones.size();
            for (int i = 0; i < size - 1; i++) {
                RenderUtils.prepMatrixForBone(poseStack, geoModel.rightHandBones.get(i));
            }
            GeoBone lastBone = geoModel.rightHandBones.get(size - 1);
            RenderUtils.translateMatrixToBone(poseStack, lastBone);
            RenderUtils.translateToPivotPoint(poseStack, lastBone);
            RenderUtils.rotateMatrixAroundBone(poseStack, lastBone);
            RenderUtils.scaleMatrixForBone(poseStack, lastBone);
        }
    }
}
