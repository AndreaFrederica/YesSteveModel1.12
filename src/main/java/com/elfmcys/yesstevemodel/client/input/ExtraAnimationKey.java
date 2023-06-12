package com.elfmcys.yesstevemodel.client.input;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetPlayAnimation;
import com.google.common.collect.Lists;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = YesSteveModel.MOD_ID)
public class ExtraAnimationKey {
    public static final List<KeyBinding> EXTRA_ANIMATION_KEYS = Lists.newArrayList();

    public static void registerKeyBinding() {
        for (int i = 0; i <= 7; i++) {
            String name = String.format("key.yes_steve_model.extra_animation.%d.desc", i);
            KeyBinding keyMapping = new KeyBinding(name,
                    KeyConflictContext.IN_GAME,
                    KeyModifier.NONE,
                    InputMappings.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    "key.category.yes_steve_model");
            ClientRegistry.registerKeyBinding(keyMapping);
            EXTRA_ANIMATION_KEYS.add(keyMapping);
        }
    }

    @SubscribeEvent
    public static void onKeyboardInput(InputEvent.KeyInputEvent event) {
        for (KeyBinding key : EXTRA_ANIMATION_KEYS) {
            if (key.isDown()) {
                NetworkHandler.CHANNEL.sendToServer(new SetPlayAnimation(EXTRA_ANIMATION_KEYS.indexOf(key)));
                return;
            }
        }
    }
}
