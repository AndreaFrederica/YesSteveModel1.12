package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetModelAndTexture;
import com.elfmcys.yesstevemodel.util.Keep;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class TextureButton extends Button {
    private final ResourceLocation modelId;
    private final ResourceLocation textureId;
    private final String name;

    public TextureButton(int pX, int pY, ResourceLocation modelId, ResourceLocation textureId) {
        super(pX, pY, 54, 102, StringTextComponent.EMPTY, (b) -> {
        });
        this.modelId = modelId;
        this.textureId = textureId;
        this.name = ModelIdUtil.getSubNameFromId(textureId);
    }

    @Override
    @Keep
    public void onPress() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap ->
                    cap.setModelAndTexture(modelId, textureId));
        }
        NetworkHandler.CHANNEL.sendToServer(new SetModelAndTexture(modelId, textureId));
    }

    @Override
    @Keep
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer font = minecraft.font;

        fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xFF_434242, 0xFF_434242);
        MainWindow window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int scissorX = (int) (this.x * scale);
        int scissorY = (int) (window.getHeight() - ((this.y + this.height - 20) * scale));
        int scissorW = (int) (this.width * scale);
        int scissorH = (int) ((this.height - 20) * scale);
        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
        RenderUtil.renderEntityInInventory(this.x + this.width / 2, this.y + this.height / 2 + 24, 35, minecraft.player, modelId, textureId);
        RenderSystem.disableScissor();

        StringTextComponent message = new StringTextComponent(name);
        List<IReorderingProcessor> split = font.split(message, 50);
        if (split.size() > 1) {
            drawCenteredString(poseStack, font, split.get(0), this.x + this.width / 2, this.y + this.height - 19, 0xF3EFE0);
            drawCenteredString(poseStack, font, split.get(1), this.x + this.width / 2, this.y + this.height - 10, 0xF3EFE0);
        } else {
            drawCenteredString(poseStack, font, message, this.x + this.width / 2, this.y + this.height - 15, 0xF3EFE0);
        }
        if (this.isHovered()) {
            fillGradient(poseStack, this.x, this.y + 1, this.x + 1, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x + this.width - 1, this.y + 1, this.x + this.width, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
        }
    }

    private static void drawCenteredString(MatrixStack poseStack, FontRenderer pFont, IReorderingProcessor processor, int pX, int pY, int color) {
        pFont.drawShadow(poseStack, processor, (float) (pX - pFont.width(processor) / 2), (float) pY, color);
    }
}
