package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.AnimatableCacheUtil;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ExecutionException;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class RenderFirstPlayerBackground {
    private static final String NAME = "Background";
    /**
     * 因为 RenderHandEvent 可有几率会渲染多次，所以为了避免多次渲染，这样设计
     */
    private static boolean ALREADY_RENDERED = false;

    @SubscribeEvent
    public static void onRenderLevelLase(RenderWorldLastEvent event) {
        ALREADY_RENDERED = false;
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player == null || ALREADY_RENDERED) {
            return;
        }
        ALREADY_RENDERED = true;
        player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
            ResourceLocation modelId = cap.getModelId();
            GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(ModelIdUtil.getArmId(cap.getModelId()));
            if (geoModel == null || !geoModel.hasTopLevelBone(NAME)) {
                return;
            }
            CustomPlayerRenderer instance = RegisterEntityRenderersEvent.getInstance();
            MatrixStack poseStack = event.getMatrixStack();
            IRenderTypeBuffer multiBufferSource = event.getBuffers();
            IVertexBuilder buffer;
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

            if (animatable instanceof CustomPlayerEntity) {
                CustomPlayerEntity customPlayer = (CustomPlayerEntity) animatable;
                customPlayer.setTexture(cap.getSelectTexture());
                if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(player, customPlayer, modelId))) {
                    return;
                }
                buffer = multiBufferSource.getBuffer(RenderType.entityTranslucent(customPlayer.getTexture()));
                int packedLight = event.getLight();
                if (instance != null) {
                    poseStack.pushPose();
                    poseStack.translate(0, -1.5, 0);
                    geoModel.getTopLevelBone(NAME).ifPresent(bone -> instance.renderRecursively(bone, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1));
                    poseStack.popPose();
                }
            }
        });
    }
}
