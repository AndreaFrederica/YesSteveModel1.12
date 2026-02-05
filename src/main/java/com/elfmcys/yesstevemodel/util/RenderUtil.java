package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.client.ClientProxy;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/*
1.16.5 - 1.12.2
player.yBodyRot - player.renderYawOffset	身体转向角度
player.yRot - player.rotationYaw	视口偏航角
player.xRot	- player.rotationPitch	视口俯仰角
player.yHeadRot	- player.rotationYawHead	头部偏航角
player.yHeadRotO - player.prevRotationYawHead	上一刻头部偏航角
 */
public final class RenderUtil {
    public static void renderTextureScreenEntity(float pPosX, float pPosY, float pScale, float pitch, float yaw, EntityPlayer player, ResourceLocation modelId, ResourceLocation textureId, boolean showGround, Consumer<CustomPlayerEntity> consumer) {
        if (player == null) {
            return;
        }
        try {
            CustomPlayerRenderer renderer = ClientProxy.getInstance();
            IAnimatable animatable = AnimatableCacheUtil.TEXTURE_GUI_CACHE.get(modelId, CustomPlayerEntity::new);
            if (animatable instanceof CustomPlayerEntity entity) {
                consumer.accept(entity);

                entity.setMainModel(ModelIdUtil.getMainId(modelId));
                entity.setTexture(textureId);

                GlStateManager.pushMatrix();
                GlStateManager.translate(pPosX, pPosY, 1050.0F);
                GlStateManager.scale(1.0F, 1.0F, -1.0F);

                /*
                动画位移矩阵开始
                 */
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0D, 0.0D, 1000.0D);
                GlStateManager.scale(pScale, pScale, pScale);
                GlStateManager.translate(0, 0.8, 0);
                GlStateManager.rotate(180.0F, 0, 0, 1);
                rotateAndEnableLighting();
                float xp = -10 + pitch;
                GlStateManager.rotate(xp, 1, 0, 0);

                float yBodyRot = player.renderYawOffset;
                float yRot = player.rotationYaw;
                float xRot = player.rotationPitch;
                float yHeadRotO = player.prevRotationYawHead;
                float yHeadRot = player.rotationYawHead;
                //Pose pose = player.getPose();

                player.renderYawOffset = -yaw;
                player.rotationYaw = 180;
                player.rotationPitch = 0;
                player.rotationYawHead = player.rotationYaw;
                player.prevRotationYawHead = player.rotationYaw;

                RenderManager dispatcher = Minecraft.getMinecraft().getRenderManager();
                xp = 180.0F - xp;
                dispatcher.setPlayerViewY(xp);
                dispatcher.setRenderShadow(false);
                if (entity.hasPreviewAnimation("sleep")) {
                    GlStateManager.rotate(yaw - 90, 0, 1, 0);
                    GlStateManager.translate(0.5, 0.5625, 0);
                    //player.setPose(Pose.SLEEPING);
                }
                if (entity.hasPreviewAnimation("swim") || entity.hasPreviewAnimation("swim_stand")) {
                    //player.setPose(Pose.SWIMMING);
                }
                if (entity.hasPreviewAnimation("sneak") || entity.hasPreviewAnimation("sneaking")) {
                    //player.setPose(Pose.CROUCHING);
                }
                if (entity.hasPreviewAnimation("sit")) {
                    GlStateManager.translate(0, -0.5, 0);
                }
                if (entity.hasPreviewAnimation("ride")) {
                    GlStateManager.translate(0, 0.85, 0);
                }
                if (entity.hasPreviewAnimation("ride_pig")) {
                    GlStateManager.translate(0, 0.3125, 0);
                }
                if (entity.hasPreviewAnimation("boat")) {
                    GlStateManager.translate(0, -0.45, 0);
                }
                renderer.doRender(player, entity, 0, 0, 0, 0.0F, 1.0F);
                // 清理实体渲染
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableColorMaterial();
                try {
                    renderExtraEntity(yaw, player, entity, dispatcher);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                GlStateManager.popMatrix();
                /*
                动画位移矩阵结束
                 */
                if (showGround) {
                    if (entity.hasPreviewAnimation("sleep")) {
                        renderBed(pScale, pitch, yaw);
                    }
                    renderGround(pScale, pitch, yaw);
                }
                dispatcher.setRenderShadow(true);

                player.renderYawOffset = yBodyRot;
                player.rotationYaw = yRot;
                player.rotationPitch = xRot;
                player.prevRotationYawHead = yHeadRotO;
                player.rotationYawHead = yHeadRot;
                //player.setPose(pose);

                GlStateManager.popMatrix();

                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.disableTexture2D();
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void renderBed(float scale, float pitch, float yaw) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.0D, 1000.0D);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, 0.8, 0);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        GlStateManager.rotate(-10 + pitch, 1, 0, 0);

        GlStateManager.rotate(yaw + 180, 0, 1, 0);
        GlStateManager.translate(-0.5, 0, 0.5);
        renderSingleBlock(Blocks.BED.getDefaultState());
        GlStateManager.popMatrix();
    }

    private static void renderGround(float scale, float pitch, float yaw) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.0D, 1000.0D);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, 0.8, 0);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        GlStateManager.rotate(-10 + pitch, 1, 0, 0);

        GlStateManager.rotate(yaw, 0, 1, 0);
        GlStateManager.translate(-1.5, -1, -1.5);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                GlStateManager.translate(0, 0, 1);
                renderSingleBlock(Blocks.GRASS.getDefaultState());
            }
            GlStateManager.translate(1, 0, -3);
        }
        GlStateManager.translate(-1, 1, 1);
        renderSingleBlock(Blocks.TALLGRASS.getStateFromMeta(1));
        GlStateManager.translate(0, 0, 1);
        renderSingleBlock(Blocks.RED_FLOWER.getStateFromMeta(4));
        GlStateManager.popMatrix();
    }

    private static void renderSingleBlock(IBlockState state) {
        GlStateManager.pushMatrix(); // 一定要 push，原版方块渲染不干净
        final Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getBlockRendererDispatcher().renderBlockBrightness(state, 1.0F);
        GlStateManager.popMatrix();
    }

    @SuppressWarnings({"DataFlowIssue", "UnnecessaryReturnStatement"})
    private static void renderExtraEntity(float yaw, EntityPlayer player, CustomPlayerEntity playerEntity, RenderManager dispatcher) throws ExecutionException {
        if (playerEntity.hasPreviewAnimation("ride")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityList.getKey(EntityHorse.class), () -> new EntityHorse(player.world));
            renderExtraEntity(yaw, player, dispatcher, entity);
            return;
        }
        if (playerEntity.hasPreviewAnimation("ride_pig")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityList.getKey(EntityPig.class), () -> new EntityPig(player.world));
            renderExtraEntity(yaw, player, dispatcher, entity);
            return;
        }
        if (playerEntity.hasPreviewAnimation("boat")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityList.getKey(EntityBoat.class), () -> new EntityBoat(player.world));
            renderExtraEntity(yaw, player, dispatcher, entity);
            return;
        }
    }

    private static void renderExtraEntity(float yaw, EntityPlayer player, RenderManager dispatcher, Entity entity) {
        GlStateManager.rotate(yaw, 0, 1, 0);
        dispatcher.renderEntity(entity, 0, -entity.getMountedYOffset() - player.getYOffset(), 0, 0.0F, 1.0F, false);
        // 清理实体渲染
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
    }

    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, EntityPlayer player, ResourceLocation modelId, ResourceLocation textureId, Consumer<CustomPlayerEntity> consumer) {
        if (player == null) {
            return;
        }
        try {
            CustomPlayerRenderer renderer = ClientProxy.getInstance();
            IAnimatable animatable = AnimatableCacheUtil.ANIMATABLE_CACHE.get(modelId, CustomPlayerEntity::new);
            if (animatable instanceof CustomPlayerEntity entity) {
                consumer.accept(entity);
                renderModel(pPosX, pPosY, (float) pScale, player, modelId, textureId, renderer, entity);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, EntityPlayer player, ResourceLocation modelId, ResourceLocation textureId) {
        renderEntityInInventory(pPosX, pPosY, pScale, player, modelId, textureId, entity -> {
            if (entity.hasPreviewAnimation()) {
                entity.clearPreviewAnimation();
            }
        });
    }

    private static void renderModel(
            double pPosX, double pPosY, float pScale, EntityPlayer player,
            ResourceLocation modelId, ResourceLocation textureId,
            GeoReplacedEntityRenderer<CustomPlayerEntity> renderer, CustomPlayerEntity entity
    ) {
        entity.setMainModel(ModelIdUtil.getMainId(modelId));
        entity.setTexture(textureId);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) pPosX, (float) pPosY, 1050.0F);
        GlStateManager.scale(1.0F, 1.0F, -1.0F);

        GlStateManager.translate(0.0D, 0.0D, 1000.0D);
        GlStateManager.scale(pScale, pScale, pScale);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        rotateAndEnableLighting();
        float xp = -10;
        GlStateManager.rotate(xp, 1, 0, 0);

        float yBodyRot = player.renderYawOffset;
        float yRot = player.rotationYaw;
        float xRot = player.rotationPitch;
        float yHeadRotO = player.prevRotationYawHead;
        float yHeadRot = player.rotationYawHead;

        ItemStack[] itemStacks = new ItemStack[EntityEquipmentSlot.values().length];
        int i = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            itemStacks[i] = player.getItemStackFromSlot(slot);
            if (slot == EntityEquipmentSlot.MAINHAND) {
                player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
            } else if (slot == EntityEquipmentSlot.OFFHAND) {
                player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
            } else {
                player.inventory.armorInventory.set(slot.getIndex(), ItemStack.EMPTY);
            }
            i++;
        }

        player.renderYawOffset = 200;
        player.rotationYaw = 180;
        player.rotationPitch = 0;
        player.rotationYawHead = player.rotationYaw;
        player.prevRotationYawHead = player.rotationYaw;

        RenderManager dispatcher = Minecraft.getMinecraft().getRenderManager();
        xp = 180.0F - xp;
        dispatcher.setPlayerViewY(xp);
        dispatcher.setRenderShadow(false);
        renderer.doRender(player, entity, 0, 0, 0, 0.0F, 1.0F);
        dispatcher.setRenderShadow(true);

        player.renderYawOffset = yBodyRot;
        player.rotationYaw = yRot;
        player.rotationPitch = xRot;
        player.prevRotationYawHead = yHeadRotO;
        player.rotationYawHead = yHeadRot;

        i = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack itemStack = itemStacks[i];
            if (slot == EntityEquipmentSlot.MAINHAND) {
                player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
            } else if (slot == EntityEquipmentSlot.OFFHAND) {
                player.inventory.offHandInventory.set(0, itemStack);
            } else {
                player.inventory.armorInventory.set(slot.getIndex(), itemStack);
            }
            i++;
        }

        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void renderPlayerEntity(EntityPlayer player, double posX, double posY, float scale, float yawOffset, int z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX + scale * 0.5f, (float) posY + scale * 2, z);
        GlStateManager.scale(1, 1, -1);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        rotateAndEnableLighting();
        float yRot = player.renderYawOffset + yawOffset - 180;
        GlStateManager.rotate(yRot, 0, 1, 0);
        RenderManager renderDispatcher = Minecraft.getMinecraft().getRenderManager();
        yRot = 180.0F - yRot;
        renderDispatcher.setPlayerViewY(yRot);
        renderDispatcher.setRenderShadow(false);
        renderDispatcher.renderEntity(player, 0, 0, 0, 0.0F, 1.0F, false); // 最后这个参数是隐藏碰撞箱
        renderDispatcher.setRenderShadow(true);
        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private static void rotateAndEnableLighting() {
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
    }

    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        final Minecraft mc = Minecraft.getMinecraft();
        int scale = new ScaledResolution(mc).getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                screenX * scale,
                mc.displayHeight - (screenY * scale + boxHeight * scale),
                Math.max(0, boxWidth * scale),
                Math.max(0, boxHeight * scale)
        );
    }
}
