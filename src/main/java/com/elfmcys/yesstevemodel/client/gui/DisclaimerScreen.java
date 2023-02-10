package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class DisclaimerScreen extends Screen {
    private CheckboxButton readCheckbox;
    private int x;
    private int y;

    public DisclaimerScreen() {
        super(new StringTextComponent("Disclaimer GUI"));
    }

    @Override
    @Keep
    protected void init() {
        this.buttons.clear();
        this.children.clear();

        TranslationTextComponent mainText = new TranslationTextComponent("gui.yes_steve_model.disclaimer.text");
        List<IReorderingProcessor> splitMainText = font.split(mainText, 400);
        int totalHeight = splitMainText.size() * font.lineHeight + 20 + 20 + 10 + 20;
        this.x = (width - 400) / 2;
        this.y = (height - totalHeight) / 2;

        TranslationTextComponent readCheckboxText = new TranslationTextComponent("gui.yes_steve_model.disclaimer.read");
        int readTextWidth = font.width(readCheckboxText);
        readCheckbox = new CheckboxButton((width - readTextWidth) / 2, y + totalHeight - 50, readTextWidth, 20, readCheckboxText, !GeneralConfig.DISCLAIMER_SHOW.get());
        addButton(readCheckbox);
        addButton(new Button((width - 300) / 2, y + totalHeight - 20, 300, 20, new TranslationTextComponent("gui.yes_steve_model.disclaimer.close"), b -> {
            if (readCheckbox.selected()) {
                GeneralConfig.DISCLAIMER_SHOW.set(false);
                Minecraft.getInstance().setScreen(new PlayerModelScreen());
            } else {
                Minecraft.getInstance().setScreen(null);
            }
        }));
    }

    @Override
    @Keep
    public void render(MatrixStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(poseStack);
        font.drawWordWrap(new TranslationTextComponent("gui.yes_steve_model.disclaimer.text"), x, y, 400, 0xffffffff);
        super.render(poseStack, pMouseX, pMouseY, pPartialTick);
    }
}
