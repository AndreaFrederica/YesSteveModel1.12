package com.elfmcys.yesstevemodel.config;

import com.elfmcys.yesstevemodel.Tags;
import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

/**
 * Forge 的 Config Gui，更直观一点。
 */
public class ConfigGui extends GuiConfig {
    public ConfigGui(final GuiScreen parent) {
        super(parent, Config.getRootConfigElements(), YesSteveModel.MOD_ID, false, false, Tags.MOD_NAME);
    }
}
