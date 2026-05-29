package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.CommonProxy;
import com.elfmcys.yesstevemodel.command.ClientCacheCommand;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.capability.EmptyStorage;
import com.elfmcys.yesstevemodel.client.compat.Mods;
import com.elfmcys.yesstevemodel.client.entity.CustomArrowEntity;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.input.*;
import com.elfmcys.yesstevemodel.client.renderer.CustomArrowRenderer;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    private static CustomPlayerRenderer CUSTOM_PLAYER_RENDERER;
    private static CustomArrowRenderer CUSTOM_ARROW_RENDERER;

    public static CustomPlayerRenderer getInstance() {
        return CUSTOM_PLAYER_RENDERER;
    }

    public static CustomArrowRenderer getArrowInstance() {
        return CUSTOM_ARROW_RENDERER;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        registerCapability();

        Mods.init();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientCommandHandler.instance.registerCommand(new ClientCacheCommand());
        ClientRegistry.registerKeyBinding(PlayerModelScreenKey.PLAYER_MODEL_KEY);
        ClientRegistry.registerKeyBinding(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        ClientRegistry.registerKeyBinding(DebugAnimationKey.DEBUG_ANIMATION_KEY);
        ClientRegistry.registerKeyBinding(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY);
        ExtraAnimationKey.registerKeyBinding();

        AnimationRegister.registerAnimationState();

        RenderManager context = Minecraft.getMinecraft().getRenderManager();
        CUSTOM_PLAYER_RENDERER = new CustomPlayerRenderer(context);
        CUSTOM_ARROW_RENDERER = new CustomArrowRenderer(context);
    }

    private static void registerCapability() {
        CapabilityManager.INSTANCE.register(CustomPlayerEntity.class, new EmptyStorage<>(), () -> null);
        CapabilityManager.INSTANCE.register(CustomArrowEntity.class, new EmptyStorage<>(), () -> null);
    }
}
