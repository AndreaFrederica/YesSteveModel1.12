package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.ConfigCheckBox;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigScreen extends Screen {
    private final PlayerModelScreen parent;

    public ConfigScreen(PlayerModelScreen parent) {
        super(new StringTextComponent("YSM Config GUI"));
        this.parent = parent;
    }

    @Override
    @Keep
    protected void init() {
        int x = (width - 420) / 2;
        int y = (height - 235) / 2;

        addButton(new FlatColorButton(x + 5, y, 80, 18, new TranslationTextComponent("gui.yes_steve_model.model.return"), (b) -> this.getMinecraft().setScreen(parent)));

        addButton(new ConfigCheckBox(x + 5, y + 25, "disable_self_model", GeneralConfig.DISABLE_SELF_MODEL));
        addButton(new ConfigCheckBox(x + 5, y + 47, "disable_other_model", GeneralConfig.DISABLE_OTHER_MODEL));
        addButton(new ConfigCheckBox(x + 5, y + 69, "print_animation_roulette_msg", GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG));
    }

    @Override
    @Keep
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
