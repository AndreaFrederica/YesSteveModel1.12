package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class FlatIconButton extends FlatColorButton {
    private final static ResourceLocation ICON = new ResourceLocation(YesSteveModel.MOD_ID, "texture/icon.png");
    private final int textureX;
    private final int textureY;

    public FlatIconButton(int x, int y, int width, int height, int textureX, int textureY, Button.IPressable onPress) {
        super(x, y, width, height, StringTextComponent.EMPTY, onPress);
        this.textureX = textureX;
        this.textureY = textureY;
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
        blit(poseStack, this.x + startX, this.y + startY, 16, 16, textureX, textureY, 16, 16, 256, 256);
    }
}
