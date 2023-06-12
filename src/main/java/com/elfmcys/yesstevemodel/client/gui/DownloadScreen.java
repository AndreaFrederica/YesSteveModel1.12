package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class DownloadScreen extends Screen {
    private final PlayerModelScreen parent;
    private int x;
    private int y;

    public DownloadScreen(PlayerModelScreen parent) {
        super(new StringTextComponent("YSM Config GUI"));
        this.parent = parent;
    }

    @Override
    @Keep
    protected void init() {
        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;

        addButton(new FlatColorButton(x + 5, y, 80, 18, new TranslationTextComponent("gui.yes_steve_model.model.return"), (b) -> this.getMinecraft().setScreen(parent)));
    }

    @Override
    @Keep
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        drawCenteredString(pPoseStack, font, "Coming Soooooooooooooooooooooooooon™", width / 2, height / 2 - 5, TextFormatting.DARK_RED.getColor());
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
