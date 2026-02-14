package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientProxy;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.AnimatableCacheUtil;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ExecutionException;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public class ReplacePlayerHandRenderEvent {
    private static final String LEFT_ARM = "LeftArm";
    private static final String RIGHT_ARM = "RightArm";

    @SubscribeEvent
    public static void onRenderHand(RenderArmEvent event) {
        if (GeneralConfig.DISABLE_SELF_MODEL) {
            return;
        }
        if (GeneralConfig.DISABLE_SELF_HANDS) {
            return;
        }
        event.setCanceled(true);
        final Minecraft mc = Minecraft.getMinecraft();
        AbstractClientPlayer player = mc.player;
        CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
            ResourceLocation modelId = cap.getModelId();
            GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(ModelIdUtil.getArmId(cap.getModelId()));
            if (geoModel == null || !hasArmBone(event.getArm(), geoModel)) {
                return;
            }
            CustomPlayerRenderer instance = ClientProxy.getInstance();
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            IAnimatable animatable;
            try {
                animatable = AnimatableCacheUtil.ANIMATABLE_CACHE.get(modelId, () -> {
                    CustomPlayerEntity entity = new CustomPlayerEntity();
                    entity.setTexture(cap.getSelectTexture());
                    return entity;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (animatable instanceof CustomPlayerEntity customPlayer) {
                customPlayer.setTexture(cap.getSelectTexture());
                if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(player, customPlayer, modelId))) {
                    return;
                }
                mc.getTextureManager().bindTexture(customPlayer.getTexture());
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                if (instance != null) {
                    if (event.getArm() == EnumHandSide.LEFT) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(0.25, 1.8, 0);
                        GlStateManager.scale(-1, -1, 1);
                        geoModel.getTopLevelBone(LEFT_ARM).ifPresent(bone -> instance.renderRecursively(buffer, bone, 1.0F, 1.0F, 1.0F, 1.0F));
                        tess.draw();
                        GlStateManager.popMatrix();
                    }
                    if (event.getArm() == EnumHandSide.RIGHT) {
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(-0.25, 1.8, 0);
                        GlStateManager.scale(-1, -1, 1);
                        geoModel.getTopLevelBone(RIGHT_ARM).ifPresent(bone -> instance.renderRecursively(buffer, bone, 1.0F, 1.0F, 1.0F, 1.0F));
                        tess.draw();
                        GlStateManager.popMatrix();
                    }
                }
            }
        });
    }

    private static boolean hasArmBone(EnumHandSide arm, GeoModel model) {
        if (arm == EnumHandSide.LEFT) {
            return model.hasTopLevelBone(LEFT_ARM);
        } else {
            return model.hasTopLevelBone(RIGHT_ARM);
        }
    }
}
