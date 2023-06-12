package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.util.Keep;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigCheckBox extends CheckboxButton {
    private final ForgeConfigSpec.BooleanValue configSpec;

    public ConfigCheckBox(int pX, int pY, String key, ForgeConfigSpec.BooleanValue configSpec) {
        super(pX, pY, 400, 20, new TranslationTextComponent("gui.yes_steve_model.config." + key), configSpec.get());
        this.configSpec = configSpec;
    }

    @Override
    @Keep
    public void onPress() {
        super.onPress();
        this.configSpec.set(!this.configSpec.get());
    }
}
