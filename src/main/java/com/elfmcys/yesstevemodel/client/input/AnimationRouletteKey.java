package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.gui.AnimationRouletteScreen;
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
public class AnimationRouletteKey {
    public static final KeyBinding ANIMATION_ROULETTE_KEY = new KeyBinding("key.yes_steve_model.animation_roulette.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.category.yes_steve_model");

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        if (ANIMATION_ROULETTE_KEY.isDown()) {
            Minecraft.getInstance().setScreen(new AnimationRouletteScreen());
        }
    }
}
