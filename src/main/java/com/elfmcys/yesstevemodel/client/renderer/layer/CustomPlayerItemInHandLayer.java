package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.model.CustomPlayerModel;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;

public class CustomPlayerItemInHandLayer<T extends LivingEntity & IAnimatable> extends GeoLayerRenderer<T> {
    public CustomPlayerItemInHandLayer(IGeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    @Keep
    public void render(MatrixStack poseStack, IRenderTypeBuffer bufferIn, int packedLightIn, T entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack offhandItem = entityLivingBaseIn.getOffhandItem();
        ItemStack mainHandItem = entityLivingBaseIn.getMainHandItem();
        if (!offhandItem.isEmpty() || !mainHandItem.isEmpty()) {
            poseStack.pushPose();
            this.renderArmWithItem(entityLivingBaseIn, mainHandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HandSide.RIGHT, poseStack, bufferIn, packedLightIn);
            this.renderArmWithItem(entityLivingBaseIn, offhandItem, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HandSide.LEFT, poseStack, bufferIn, packedLightIn);
            poseStack.popPose();
        }
    }

    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemCameraTransforms.TransformType transformType, HandSide arm, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int light) {
        if (!itemStack.isEmpty() && this.getEntityModel() instanceof CustomPlayerModel) {
            CustomPlayerModel customPlayerModel = (CustomPlayerModel) this.getEntityModel();
            poseStack.pushPose();
            customPlayerModel.translateToHand(arm, poseStack);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
            boolean isLeftHand = arm == HandSide.LEFT;
            poseStack.translate(0, 0.15, 0);
            Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, transformType, isLeftHand, poseStack, bufferSource, light);
            poseStack.popPose();
        }
    }
}
