package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetStarModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class StarButton extends FlatColorButton {
    private final static ResourceLocation ICON = new ResourceLocation(YesSteveModel.MOD_ID, "texture/icon.png");

    public StarButton(int x, int y) {
        super(x, y, 20, 20, "", (b) -> {
        });
    }

    @Override
    public void renderWidget(@Nonnull Minecraft mc, int mouseX, int mouseY, float pPartialTick) {
        super.renderWidget(mc, mouseX, mouseY, pPartialTick);
        mc.getTextureManager().bindTexture(ICON);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableDepth();
        int startX = (this.width - 16) / 2;
        int startY = (this.height - 16) / 2;
        EntityPlayerSP player = mc.player;
        CapabilityEvent.getCapability(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> CapabilityEvent.getCapability(player, StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(starModelsCap -> {
            ResourceLocation modelId = modelInfoCap.getModelId();
            if (starModelsCap.containModel(modelId)) {
                this.drawTexturedModalRect(this.x + startX, this.y + startY, 16, 0, 16, 16);
            } else {
                this.drawTexturedModalRect(this.x + startX, this.y + startY, 0, 0, 16, 16);
            }
        }));
    }

    @Override
    public void onPress() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        CapabilityEvent.getCapability(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> CapabilityEvent.getCapability(player, StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(starModelsCap -> {
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
