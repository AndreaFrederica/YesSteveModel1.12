package com.elfmcys.yesstevemodel.client.renderer.layer;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.ILocationBone;
import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.IGeoLayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.ILocationModel;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelParrot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderParrot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

/**
 * 可参考原版实现 {@link net.minecraft.client.renderer.entity.layers.LayerEntityOnShoulder}。
 */
public class CustomPlayerParrotOnShoulderLayer<T extends EntityPlayer, E extends AnimatableEntity<T>> implements IGeoLayerRenderer<T, E> {
    private final RenderManager renderManager;

    private RenderLivingBase<? extends EntityLivingBase> leftRenderer;
    private ModelBase leftModel;
    private ResourceLocation leftTexture;
    private UUID leftId;
    private Class<?> leftEntityClass;

    private RenderLivingBase<? extends EntityLivingBase> rightRenderer;
    private ModelBase rightModel;
    private ResourceLocation rightTexture;
    private UUID rightId;
    private Class<?> rightEntityClass;

    public CustomPlayerParrotOnShoulderLayer(RenderManager ctx) {
        this.renderManager = ctx;
    }

    @Override
    public void render(@Nonnull T player, @Nonnull E animatable, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, Color renderColor) {
        ILocationModel geoModel = animatable.getCurrentModel();
        if (geoModel == null) return;
        NBTTagCompound leftShoulderNBT = player.getLeftShoulderEntity();
        NBTTagCompound rightShoulderNBT = player.getRightShoulderEntity();

        if (!leftShoulderNBT.isEmpty() || !rightShoulderNBT.isEmpty()) {
            GlStateManager.enableRescaleNormal();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (!leftShoulderNBT.isEmpty()) {
                this.updateCache(leftShoulderNBT, true);
                this.renderEntityOnShoulder(player, geoModel, true, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }

            if (!rightShoulderNBT.isEmpty()) {
                this.updateCache(rightShoulderNBT, false);
                this.renderEntityOnShoulder(player, geoModel, false, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }

            GlStateManager.disableRescaleNormal();
        }
    }

    private void renderEntityOnShoulder(
            T player, ILocationModel geoModel, boolean isLeft,
            float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch
    ) {
        List<? extends ILocationBone> bones = isLeft ? geoModel.leftShoulderBones() : geoModel.rightShoulderBones();
        RenderLivingBase<? extends EntityLivingBase> renderer = isLeft ? this.leftRenderer : this.rightRenderer;
        ModelBase model = isLeft ? this.leftModel : this.rightModel;
        ResourceLocation texture = isLeft ? this.leftTexture : this.rightTexture;
        Class<?> entityClass = isLeft ? this.leftEntityClass : this.rightEntityClass;

        if (entityClass == EntityParrot.class) ageInTicks = 0.0F;
        if (!bones.isEmpty() && renderer != null && model != null && texture != null) {
            GlStateManager.pushMatrix();
            // 缩放不为 0 才会渲染
            boolean scaleResult = RenderUtils.prepMatrixForLocator(bones);
            if (!scaleResult) {
                GlStateManager.translate(0.0, 1.5, 0.0);
                GlStateManager.rotate(180.0F, 0, 0, 1);
                renderer.bindTexture(texture);
                final float scale = 1 / 16F;
                model.setLivingAnimations(player, limbSwing, limbSwingAmount, partialTicks);
                model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, player);
                model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            }
            GlStateManager.popMatrix();
        }
    }

    private void updateCache(NBTTagCompound compound, boolean isLeft) {
        UUID entityId = compound.getUniqueId("UUID");
        UUID currentId = isLeft ? this.leftId : this.rightId;

        if (currentId == null || !currentId.equals(entityId)) {
            Class<?> entityClass = EntityList.getClassFromName(compound.getString("id"));
            RenderLivingBase<? extends EntityLivingBase> renderer = null;
            ModelBase model = null;
            ResourceLocation texture = null;

            if (entityClass == EntityParrot.class) {
                renderer = new RenderParrot(this.renderManager);
                model = new ModelParrot();
                texture = RenderParrot.PARROT_TEXTURES[compound.getInteger("Variant")];
            }

            if (isLeft) {
                this.leftId = entityId;
                this.leftEntityClass = entityClass;
                this.leftRenderer = renderer;
                this.leftModel = model;
                this.leftTexture = texture;
            } else {
                this.rightId = entityId;
                this.rightEntityClass = entityClass;
                this.rightRenderer = renderer;
                this.rightModel = model;
                this.rightTexture = texture;
            }
        }
    }
}
