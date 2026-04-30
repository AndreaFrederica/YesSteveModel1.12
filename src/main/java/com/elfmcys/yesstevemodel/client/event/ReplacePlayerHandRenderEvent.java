package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.client.ClientProxy;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.client.util.AnimatableCacheUtil;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ExecutionException;

public class ReplacePlayerHandRenderEvent {
    public static void renderArm(EnumHandSide arm) {
        final Minecraft mc = Minecraft.getMinecraft();
        AbstractClientPlayer player = mc.player;
        CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
            ResourceLocation modelId = cap.getModelId();
            CustomPlayerRenderer instance = ClientProxy.getInstance();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            AnimatableEntity<?> animatable;
            try {
                animatable = AnimatableCacheUtil.ANIMATABLE_CACHE.get(modelId, () -> {
                    CustomPlayerEntity entity = new CustomPlayerEntity(null);
                    entity.setTextureLocation(cap.getSelectTexture());
                    return entity;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (animatable instanceof CustomPlayerEntity customPlayer) {
                customPlayer.setModelLocation(ModelIdUtil.getArmId(modelId));
                customPlayer.setTextureLocation(cap.getSelectTexture());
                if (!customPlayer.updateModel()) {
                    return;
                }
                AnimatedGeoModel geoModel = customPlayer.getCurrentModel();
                if (geoModel == null) {
                    return;
                }
                AnimatedGeoBone bone = getArmBone(arm, geoModel);
                if (bone == null) {
                    return;
                }
                if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(player, customPlayer, modelId))) {
                    return;
                }
                mc.getTextureManager().bindTexture(customPlayer.getTextureLocation());
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                if (instance != null) {
                    if (arm == EnumHandSide.LEFT) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0.25, 1.8, 0);
                        GlStateManager.scale(-1, -1, 1);
                        instance.renderRecursively(bone, tess, 1.0F, 1.0F, 1.0F, 1.0F);
                        tess.draw();
                        GlStateManager.popMatrix();
                    } else {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(-0.25, 1.8, 0);
                        GlStateManager.scale(-1, -1, 1);
                        instance.renderRecursively(bone, tess, 1.0F, 1.0F, 1.0F, 1.0F);
                        tess.draw();
                        GlStateManager.popMatrix();
                    }
                }
            }
        });
    }

    private static AnimatedGeoBone getArmBone(EnumHandSide arm, AnimatedGeoModel model) {
        return arm == EnumHandSide.LEFT ? model.leftArm() : model.rightArm();
    }

    public static boolean shouldRenderArm() {
        return !GeneralConfig.DISABLE_SELF_MODEL && !GeneralConfig.DISABLE_SELF_HANDS;
    }
}
