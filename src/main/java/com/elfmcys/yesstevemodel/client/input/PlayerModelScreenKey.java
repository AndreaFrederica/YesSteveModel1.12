package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.DisclaimerScreen;
import com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class PlayerModelScreenKey {
    public static final KeyBinding PLAYER_MODEL_KEY = new KeyBinding("key.yes_steve_model.player_model.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.ALT,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (PLAYER_MODEL_KEY.isDown()) {
            if (GeneralConfig.DISCLAIMER_SHOW.get()) {
                Minecraft.getInstance().setScreen(new DisclaimerScreen());
            } else {
                Minecraft.getInstance().setScreen(new PlayerModelScreen());
            }
        }
    }
}
