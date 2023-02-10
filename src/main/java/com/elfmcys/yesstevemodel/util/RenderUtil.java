package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.event.RegisterEntityRenderersEvent;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@SuppressWarnings("all")
public final class RenderUtil {
    public static void renderTextureScreenEntity(float pPosX, float pPosY, float pScale, float pitch, float yaw, ClientPlayerEntity player, ResourceLocation modelId, ResourceLocation textureId, boolean showGround, Consumer<CustomPlayerEntity> consumer) {
        if (player == null) {
            return;
        }
        try {
            CustomPlayerRenderer renderer = RegisterEntityRenderersEvent.getInstance();
            IAnimatable animatable = AnimatableCacheUtil.TEXTURE_GUI_CACHE.get(modelId, CustomPlayerEntity::new);
            if (animatable instanceof CustomPlayerEntity) {
                CustomPlayerEntity entity = (CustomPlayerEntity) animatable;
                consumer.accept(entity);

                entity.setMainModel(ModelIdUtil.getMainId(modelId));
                entity.setTexture(textureId);

                RenderSystem.pushMatrix();
                RenderSystem.translatef((float) pPosX, (float) pPosY, 1050.0F);
                RenderSystem.scalef(1.0F, 1.0F, -1.0F);

                MatrixStack poseStack = new MatrixStack();
                poseStack.translate(0.0D, 0.0D, 1000.0D);
                poseStack.scale(pScale, pScale, pScale);
                poseStack.translate(0, 0.8, 0);
                Quaternion zp = Vector3f.ZP.rotationDegrees(180.0F);
                Quaternion xp = Vector3f.XP.rotationDegrees(-10 + pitch);
                zp.mul(xp);
                poseStack.mulPose(zp);

                float yBodyRot = player.yBodyRot;
                float yRot = player.yRot;
                float xRot = player.xRot;
                float yHeadRotO = player.yHeadRotO;
                float yHeadRot = player.yHeadRot;
                Pose pose = player.getPose();

                player.yBodyRot = -yaw;
                player.yRot = 180;
                player.xRot = 0;
                player.yHeadRot = player.yRot;
                player.yHeadRotO = player.yRot;

                EntityRendererManager dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
                xp.conj();
                dispatcher.overrideCameraOrientation(xp);
                dispatcher.setRenderShadow(false);
                IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                RenderSystem.runAsFancy(() -> {
                    if (entity.hasPreviewAnimation("sleep")) {
                        poseStack.mulPose(Vector3f.YP.rotationDegrees(yaw - 90));
                        poseStack.translate(0.5, 0.5625, 0);
                        player.setPose(Pose.SLEEPING);
                    }
                    if (entity.hasPreviewAnimation("swim") || entity.hasPreviewAnimation("swim_stand")) {
                        player.setPose(Pose.SWIMMING);
                    }
                    if (entity.hasPreviewAnimation("sneak") || entity.hasPreviewAnimation("sneaking")) {
                        player.setPose(Pose.CROUCHING);
                    }
                    if (entity.hasPreviewAnimation("sit")) {
                        poseStack.translate(0, -0.5, 0);
                    }
                    if (entity.hasPreviewAnimation("ride")) {
                        poseStack.translate(0, 0.85, 0);
                    }
                    if (entity.hasPreviewAnimation("ride_pig")) {
                        poseStack.translate(0, 0.3125, 0);
                    }
                    if (entity.hasPreviewAnimation("boat")) {
                        poseStack.translate(0, -0.45, 0);
                    }
                    renderer.render(player, animatable, 0, 1.0f, poseStack, bufferSource, 0xf000f0);
                    try {
                        renderExtraEntity(yaw, player, entity, poseStack, dispatcher, bufferSource);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                    if (showGround) {
                        if (entity.hasPreviewAnimation("sleep")) {
                            renderBed(pScale, pitch, yaw, bufferSource);
                        }
                        renderGround(pScale, pitch, yaw, bufferSource);
                    }
                });
                bufferSource.endBatch();
                dispatcher.setRenderShadow(true);

                player.yBodyRot = yBodyRot;
                player.yRot = yRot;
                player.xRot = xRot;
                player.yHeadRotO = yHeadRotO;
                player.yHeadRot = yHeadRot;
                player.setPose(pose);

                RenderSystem.popMatrix();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void renderBed(float scale, float pitch, float yaw, IRenderTypeBuffer.Impl bufferSource) {
        MatrixStack poseStack = new MatrixStack();
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, 0.8, 0);
        Quaternion zp = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion xp = Vector3f.XP.rotationDegrees(-10 + pitch);
        zp.mul(xp);
        poseStack.mulPose(zp);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(yaw + 180));
        poseStack.translate(-0.5, 0, 0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.RED_BED.defaultBlockState(), poseStack, bufferSource, 0xf000f0, OverlayTexture.NO_OVERLAY);
    }

    private static void renderGround(float scale, float pitch, float yaw, IRenderTypeBuffer.Impl bufferSource) {
        MatrixStack poseStack = new MatrixStack();
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, 0.8, 0);
        Quaternion zp = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion xp = Vector3f.XP.rotationDegrees(-10 + pitch);
        zp.mul(xp);
        poseStack.mulPose(zp);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
        poseStack.translate(-1.5, -1, -2.5);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                poseStack.translate(0, 0, 1);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRASS_BLOCK.defaultBlockState(), poseStack, bufferSource, 0xf000f0, OverlayTexture.NO_OVERLAY);
            }
            poseStack.translate(1, 0, -3);
        }
        poseStack.translate(-1, 1, 1);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.GRASS.defaultBlockState(), poseStack, bufferSource, 0xf000f0, OverlayTexture.NO_OVERLAY);
        poseStack.translate(0, 0, 1);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.RED_TULIP.defaultBlockState(), poseStack, bufferSource, 0xf000f0, OverlayTexture.NO_OVERLAY);

    }

    private static void renderExtraEntity(float yaw, ClientPlayerEntity player, CustomPlayerEntity playerEntity, MatrixStack poseStack, EntityRendererManager dispatcher, IRenderTypeBuffer.Impl bufferSource) throws ExecutionException {
        if (playerEntity.hasPreviewAnimation("ride")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.HORSE.getRegistryName(), () -> EntityType.HORSE.create(player.level));
            renderExtraEntity(yaw, player, poseStack, dispatcher, bufferSource, entity);
            return;
        }
        if (playerEntity.hasPreviewAnimation("ride_pig")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.PIG.getRegistryName(), () -> EntityType.PIG.create(player.level));
            renderExtraEntity(yaw, player, poseStack, dispatcher, bufferSource, entity);
            return;
        }
        if (playerEntity.hasPreviewAnimation("boat")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityType.BOAT.getRegistryName(), () -> EntityType.BOAT.create(player.level));
            renderExtraEntity(yaw, player, poseStack, dispatcher, bufferSource, entity);
            return;
        }
    }

    private static void renderExtraEntity(float yaw, ClientPlayerEntity player, MatrixStack poseStack, EntityRendererManager dispatcher, IRenderTypeBuffer.Impl bufferSource, Entity entity) {
        poseStack.mulPose(Vector3f.YP.rotationDegrees(yaw));
        dispatcher.render(entity, 0, -entity.getPassengersRidingOffset() - player.getMyRidingOffset(), 0, 0, 1.0f, poseStack, bufferSource, 0xf000f0);
    }

    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, ClientPlayerEntity player, ResourceLocation modelId, ResourceLocation textureId, Consumer<CustomPlayerEntity> consumer) {
        if (player == null) {
            return;
        }
        try {
            CustomPlayerRenderer renderer = RegisterEntityRenderersEvent.getInstance();
            IAnimatable animatable = AnimatableCacheUtil.ANIMATABLE_CACHE.get(modelId, CustomPlayerEntity::new);
            if (animatable instanceof CustomPlayerEntity) {
                CustomPlayerEntity entity = (CustomPlayerEntity) animatable;
                consumer.accept(entity);
                renderModel((double) pPosX, (double) pPosY, (float) pScale, player, modelId, textureId, renderer, animatable, entity);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, ClientPlayerEntity player, ResourceLocation modelId, ResourceLocation textureId) {
        renderEntityInInventory(pPosX, pPosY, pScale, player, modelId, textureId, entity -> {
            if (entity.hasPreviewAnimation()) {
                entity.clearPreviewAnimation();
            }
        });
    }

    private static void renderModel(double pPosX, double pPosY, float pScale, ClientPlayerEntity player, ResourceLocation modelId, ResourceLocation textureId, GeoReplacedEntityRenderer renderer, IAnimatable animatable, CustomPlayerEntity entity) {
        entity.setMainModel(ModelIdUtil.getMainId(modelId));
        entity.setTexture(textureId);

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) pPosX, (float) pPosY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);

        MatrixStack poseStack = new MatrixStack();
        poseStack.translate(0.0D, 0.0D, 1000.0D);
        poseStack.scale(pScale, pScale, pScale);
        Quaternion zp = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion xp = Vector3f.XP.rotationDegrees(-10);
        zp.mul(xp);
        poseStack.mulPose(zp);

        float yBodyRot = player.yBodyRot;
        float yRot = player.yRot;
        float xRot = player.xRot;
        float yHeadRotO = player.yHeadRotO;
        float yHeadRot = player.yHeadRot;

        player.yBodyRot = 200;
        player.yRot = 180;
        player.xRot = 0;
        player.yHeadRot = player.yRot;
        player.yHeadRotO = player.yRot;

        EntityRendererManager dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        xp.conj();
        dispatcher.overrideCameraOrientation(xp);
        dispatcher.setRenderShadow(false);
        IRenderTypeBuffer.Impl bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            renderer.render(player, animatable, 0, 1.0f, poseStack, bufferSource, 0xf000f0);
        });
        bufferSource.endBatch();
        dispatcher.setRenderShadow(true);

        player.yBodyRot = yBodyRot;
        player.yRot = yRot;
        player.xRot = xRot;
        player.yHeadRotO = yHeadRotO;
        player.yHeadRot = yHeadRot;

        RenderSystem.popMatrix();
    }
}
