package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class FlatColorButton extends Button {
    public FlatColorButton(int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, Button.IPressable pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }

    @Override
    @Keep
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer font = minecraft.font;
        fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xff_434242, 0xff_434242);
        if (this.isHovered()) {
            fillGradient(poseStack, this.x, this.y + 1, this.x + 1, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x + this.width - 1, this.y + 1, this.x + this.width, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
        }
        drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xF3EFE0);
    }
}
