package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.model.format.Type;
import com.elfmcys.yesstevemodel.network.message.RequestServerModelInfo;
import com.elfmcys.yesstevemodel.util.FileSizeUtils;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ModelInfoButton extends Button {
    private final RequestServerModelInfo.Info info;
    private boolean isSelect = false;

    public ModelInfoButton(int pX, int pY, int pHeight, RequestServerModelInfo.Info info, IPressable pOnPress) {
        super(pX, pY, 250, pHeight, StringTextComponent.EMPTY, pOnPress);
        this.info = info;
    }

    @Override
    @Keep
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer font = minecraft.font;
        if (isSelect) {
            fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xff_1E90FF, 0xff_1E90FF);
        } else {
            fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0xff_434242, 0xff_434242);
        }
        if (this.isHovered()) {
            fillGradient(poseStack, this.x, this.y + 1, this.x + 1, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x + this.width - 1, this.y + 1, this.x + this.width, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
        }
        drawString(poseStack, font, info.getFileName(), this.x + 5, this.y + (this.height - 8) / 2, 0xF3EFE0);
        if (info.getType() == Type.FOLDER) {
            drawString(poseStack, font, new TranslationTextComponent("gui.yes_steve_model.model_manage.type.folder"), this.x + 155, this.y + (this.height - 8) / 2, TextFormatting.AQUA.getColor());
        } else if (info.getType() == Type.ZIP) {
            drawString(poseStack, font, new TranslationTextComponent("gui.yes_steve_model.model_manage.type.zip"), this.x + 155, this.y + (this.height - 8) / 2, TextFormatting.GOLD.getColor());
        } else {
            drawString(poseStack, font, new TranslationTextComponent("gui.yes_steve_model.model_manage.type.ysm"), this.x + 155, this.y + (this.height - 8) / 2, TextFormatting.YELLOW.getColor());
        }
        drawString(poseStack, font, FileSizeUtils.size(info.getSize()), this.x + 205, this.y + (this.height - 8) / 2, 0xC0C0C0);
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
