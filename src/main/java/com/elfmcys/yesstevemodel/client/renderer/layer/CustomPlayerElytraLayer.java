package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.ElytraModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class CustomPlayerElytraLayer<T extends LivingEntity & IAnimatable> extends GeoLayerRenderer<T> {
    private static final ResourceLocation WINGS_LOCATION = new ResourceLocation("textures/entity/elytra.png");
    private final ElytraModel<T> elytraModel = new ElytraModel();

    public CustomPlayerElytraLayer(IGeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    @Keep
    public void render(MatrixStack poseStack, IRenderTypeBuffer bufferIn, int packedLightIn, T livingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        ItemStack stack = livingEntity.getItemBySlot(EquipmentSlotType.CHEST);
        if (stack.getItem() == Items.ELYTRA && this.entityRenderer.getGeoModel() != null) {
            GeoModel geoModel = this.entityRenderer.getGeoModel();
            if (!geoModel.elytraBones.isEmpty()) {
                ResourceLocation texture;
                if (livingEntity instanceof ClientPlayerEntity) {
                    ClientPlayerEntity player = (ClientPlayerEntity) livingEntity;
                    if (player.isElytraLoaded() && player.getElytraTextureLocation() != null) {
                        texture = player.getElytraTextureLocation();
                    } else if (player.isCapeLoaded() && player.getCloakTextureLocation() != null && player.isModelPartShown(PlayerModelPart.CAPE)) {
                        texture = player.getCloakTextureLocation();
                    } else {
                        texture = WINGS_LOCATION;
                    }
                } else {
                    texture = WINGS_LOCATION;
                }
                poseStack.pushPose();
                translateToElytra(poseStack, geoModel);
                poseStack.translate(0, 1.5, 0);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));
                poseStack.scale(2.0f, 2.0f, 2.0f);
                this.elytraModel.setupAnim(livingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
                IVertexBuilder vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferIn, RenderType.armorCutoutNoCull(texture), false, stack.hasFoil());
                this.elytraModel.renderToBuffer(poseStack, vertexconsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }

    protected void translateToElytra(MatrixStack poseStack, GeoModel geoModel) {
        int size = geoModel.elytraBones.size();
        for (int i = 0; i < size - 1; i++) {
            RenderUtils.prepMatrixForBone(poseStack, geoModel.elytraBones.get(i));
        }
        GeoBone lastBone = geoModel.elytraBones.get(size - 1);
        RenderUtils.translateMatrixToBone(poseStack, lastBone);
        RenderUtils.translateToPivotPoint(poseStack, lastBone);
        RenderUtils.rotateMatrixAroundBone(poseStack, lastBone);
        RenderUtils.scaleMatrixForBone(poseStack, lastBone);
    }
}
