package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.ConfigCheckBox;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.config.Config;
import com.elfmcys.yesstevemodel.config.ExtraPlayerScreenConfig;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;

public class ConfigScreen extends Screen {
    private final PlayerModelScreen parent;

    public ConfigScreen(PlayerModelScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int x = (this.width - 420) / 2;
        int y = (this.height - 235) / 2;

        this.addButton(new FlatColorButton(x + 5, y, 80, 18, I18n.format("gui.yes_steve_model.model.return"), (b) -> this.mc.displayGuiScreen(parent)));

        this.addButton(new ConfigCheckBox(x + 5, y + 25, "disable_self_model", this.fontRenderer,
                GeneralConfig.DISABLE_SELF_MODEL, (value) -> GeneralConfig.DISABLE_SELF_MODEL = value));
        this.addButton(new ConfigCheckBox(x + 5, y + 47, "disable_other_model", this.fontRenderer,
                GeneralConfig.DISABLE_OTHER_MODEL, (value) -> GeneralConfig.DISABLE_OTHER_MODEL = value));
        this.addButton(new ConfigCheckBox(x + 5, y + 69, "print_animation_roulette_msg", this.fontRenderer,
                GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG, (value) -> GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG = value));
        this.addButton(new ConfigCheckBox(x + 5, y + 91, "disable_self_hands", this.fontRenderer,
                GeneralConfig.DISABLE_SELF_HANDS, (value) -> GeneralConfig.DISABLE_SELF_HANDS = value));
        this.addButton(new ConfigCheckBox(x + 5, y + 112, "disable_player_render", this.fontRenderer,
                ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER, (value) -> ExtraPlayerScreenConfig.DISABLE_PLAYER_RENDER = value));
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawDefaultBackground();
        super.drawScreen(pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void onGuiClosed() {
        Config.save();
        super.onGuiClosed();
    }

    @Override
    public void onResize(@Nonnull Minecraft mc, int width, int height) {
        Config.save();
        super.onResize(mc, width, height);
    }
}
