package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class OpenModelFolderScreen extends Screen {
    private final PlayerModelScreen screen;

    protected OpenModelFolderScreen(PlayerModelScreen screen) {
        super(new StringTextComponent("Open Model Folder"));
        this.screen = screen;
    }

    @Override
    @Keep
    protected void init() {
        int x = (width - 310) / 2;
        int y = height / 2 + 60;
        this.buttons.clear();
        this.children.clear();
        this.addButton(new Button(x, y, 150, 20, new TranslationTextComponent("gui.yes_steve_model.open_model_folder.open"), b -> {
            Util.getPlatform().openFile(ServerModelManager.CUSTOM.toFile());
        }));
        this.addButton(new Button(x + 160, y, 150, 20, new TranslationTextComponent("gui.yes_steve_model.model.return"), b -> {
            getMinecraft().setScreen(this.screen);
        }));
    }

    @Override
    @Keep
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        font.drawWordWrap(new TranslationTextComponent("gui.yes_steve_model.open_model_folder.tips"),
                (width - 400) / 2, height / 2 - 80, 400, 0XFFFFFF);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
