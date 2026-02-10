package com.elfmcys.yesstevemodel.client.gui.button;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class FlatColorButton extends Button {
    private boolean isSelect = false;
    private @Nullable List<String> tooltips;

    public FlatColorButton(int pX, int pY, int pWidth, int pHeight, String pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress);
    }

    public FlatColorButton setTooltips(String key) {
        this.tooltips = Collections.singletonList(I18n.format(key));
        return this;
    }

    public FlatColorButton setTooltips(@Nullable List<String> tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    public void renderToolTip(GuiScreen screen, int pMouseX, int pMouseY) {
        if (this.hovered && this.tooltips != null) {
            screen.drawHoveringText(this.tooltips, pMouseX, pMouseY);
        }
    }

    @Override
    public void renderWidget(@Nonnull Minecraft mc, int mouseX, int mouseY, float pPartialTick) {
        FontRenderer font = mc.fontRenderer;
        if (this.isSelect) {
            this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xff_1E90FF, 0xff_1E90FF);
        } else {
            this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xff_434242, 0xff_434242);
        }
        if (this.isMouseOver()) {
            this.drawGradientRect(this.x, this.y + 1, this.x + 1, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + 1, 0xff_F3EFE0, 0xff_F3EFE0);
            this.drawGradientRect(this.x + this.width - 1, this.y + 1, this.x + this.width, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            this.drawGradientRect(this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
        }
        this.renderString(font, 0xF3EFE0);
    }

    public void setSelect(boolean select) {
        this.isSelect = select;
    }
}
