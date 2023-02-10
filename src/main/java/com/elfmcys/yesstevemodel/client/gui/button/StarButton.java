package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetStarModel;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class StarButton extends FlatColorButton {
    private final static ResourceLocation ICON = new ResourceLocation(YesSteveModel.MOD_ID, "texture/icon.png");

    public StarButton(int x, int y) {
        super(x, y, 20, 20, StringTextComponent.EMPTY, (b) -> {
        });
    }

    @Override
    @Keep
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        super.renderButton(poseStack, mouseX, mouseY, pPartialTick);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(ICON);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int startX = (this.width - 16) / 2;
        int startY = (this.height - 16) / 2;
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(starModelsCap -> {
                ResourceLocation modelId = modelInfoCap.getModelId();
                if (starModelsCap.containModel(modelId)) {
                    blit(poseStack, this.x + startX, this.y + startY, 16, 16, 16, 0, 16, 16, 256, 256);
                } else {
                    blit(poseStack, this.x + startX, this.y + startY, 16, 16, 0, 0, 16, 16, 256, 256);
                }
            }));
        }
    }

    @Override
    @Keep
    public void onPress() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(starModelsCap -> {
                ResourceLocation modelId = modelInfoCap.getModelId();
                if (starModelsCap.containModel(modelId)) {
                    starModelsCap.removeModel(modelId);
                    NetworkHandler.CHANNEL.sendToServer(SetStarModel.remove(modelId));
                } else {
                    starModelsCap.addModel(modelId);
                    NetworkHandler.CHANNEL.sendToServer(SetStarModel.add(modelId));
                }
            }));
        }
    }
}
