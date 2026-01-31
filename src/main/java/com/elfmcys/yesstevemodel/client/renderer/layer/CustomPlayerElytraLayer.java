package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * 可参考原版实现 {@link net.minecraft.client.renderer.entity.layers.LayerElytra}。
 */
//TODO：GL 状态
public class CustomPlayerElytraLayer<T extends EntityLivingBase & IAnimatable> extends GeoLayerRenderer<T> {
    private static final ResourceLocation WINGS_LOCATION = new ResourceLocation("textures/entity/elytra.png");
    private final ModelElytra elytraModel = new ModelElytra();

    public CustomPlayerElytraLayer(IGeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(T livingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, Color renderColor) {
        ItemStack stack = livingEntity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (stack.getItem() == Items.ELYTRA && this.entityRenderer.getGeoModel() != null) {
            GeoModel geoModel = this.entityRenderer.getGeoModel();
            if (!geoModel.elytraBones.isEmpty()) {
                ResourceLocation texture;
                if (livingEntity instanceof EntityPlayerSP player) {
                    if (player.isPlayerInfoSet() && player.getLocationElytra() != null) {
                        texture = player.getLocationElytra();
                    } else if (player.hasPlayerInfo() && player.getLocationCape() != null && player.isWearing(EnumPlayerModelParts.CAPE)) {
                        texture = player.getLocationCape();
                    } else {
                        texture = WINGS_LOCATION;
                    }
                } else {
                    texture = WINGS_LOCATION;
                }
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                GlStateManager.pushMatrix();
                translateToElytra(geoModel);
                //GlStateManager.translate(0, 1.5, 0);
                GlStateManager.rotate(180, 0, 0, 1);
                //GlStateManager.scale(2.0f, 2.0f, 2.0f);
                Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
                this.elytraModel.setRotationAngles(pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, 0.0625F, livingEntity);
                this.elytraModel.render(livingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, 0.0625F);
                // TODO：附魔光效
//                if (stack.isItemEnchanted()) {
//                    LayerArmorBase.renderEnchantedGlint(this, livingEntity, this.elytraModel, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch, 1.0F);
//                }

                GlStateManager.popMatrix();

                GlStateManager.disableBlend();
            }
        }
    }

    protected static void translateToElytra(GeoModel geoModel) {
        int size = geoModel.elytraBones.size();
        for (int i = 0; i < size - 1; i++) {
            RenderUtils.prepMatrixForBone(geoModel.elytraBones.get(i));
        }
        GeoBone lastBone = geoModel.elytraBones.get(size - 1);
        RenderUtils.translateMatrixToBone(lastBone);
        RenderUtils.translateToPivotPoint(lastBone);
        RenderUtils.rotateMatrixAroundBone(lastBone);
        RenderUtils.scaleMatrixForBone(lastBone);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
