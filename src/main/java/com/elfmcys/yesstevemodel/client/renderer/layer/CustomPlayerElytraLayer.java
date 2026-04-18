package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.client.compat.ElytraCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.ILocationModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * 可参考原版实现 {@link net.minecraft.client.renderer.entity.layers.LayerElytra}。
 */
public class CustomPlayerElytraLayer<T extends EntityLivingBase, E extends AnimatableEntity<T>> implements IGeoLayerRenderer<T, E> {
    private final ModelElytra elytraModel = new ModelElytra();

    public CustomPlayerElytraLayer() {
    }

    @Override
    public void render(
            @Nonnull T entity, @Nonnull E animatable,
            float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch, Color renderColor
    ) {
        if (!ElytraCompat.isWearingElytra(entity)) return;
        ILocationModel geoModel = animatable.getCurrentModel();
        if (geoModel == null || geoModel.elytraBones().isEmpty()) return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.pushMatrix();
        boolean scaleResult = RenderUtils.prepMatrixForLocator(geoModel.elytraBones());
        // 缩放不为 0 才会渲染
        if (!scaleResult) {
            GlStateManager.rotate(180, 0, 0, 1);
            ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            Minecraft mc = Minecraft.getMinecraft();
            mc.getTextureManager().bindTexture(getElytraTexture(entity, stack.getItem()));
            final float scale = 1 / 16F;
            this.elytraModel.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
            this.elytraModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            if (stack.hasEffect()) {
                RenderPlayer renderer = mc.getRenderManager().getSkinMap().get("default");
                LayerArmorBase.renderEnchantedGlint(renderer, entity, this.elytraModel, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            }
        }
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected static ResourceLocation getElytraTexture(EntityLivingBase livingEntity, Item elytra) {
        if (livingEntity instanceof AbstractClientPlayer player) {
            if (player.isPlayerInfoSet() && player.getLocationElytra() != null) {
                return player.getLocationElytra();
            } else if (player.hasPlayerInfo() && player.getLocationCape() != null && player.isWearing(EnumPlayerModelParts.CAPE)) {
                return player.getLocationCape();
            }
        }
        return ElytraCompat.getDefaultTexture(elytra);
    }
}
