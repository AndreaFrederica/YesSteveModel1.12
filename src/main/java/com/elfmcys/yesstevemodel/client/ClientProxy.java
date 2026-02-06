package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.CommonProxy;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.compat.ElytraCompat;
import com.elfmcys.yesstevemodel.client.compat.SwimmingCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import com.elfmcys.yesstevemodel.client.event.ConfigSyncEvent;
import com.elfmcys.yesstevemodel.client.input.*;
import com.elfmcys.yesstevemodel.client.renderer.CustomArrowRenderer;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.common.MinecraftForge;
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
        MinecraftForge.EVENT_BUS.register(new ConfigSyncEvent());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientRegistry.registerKeyBinding(PlayerModelScreenKey.PLAYER_MODEL_KEY);
        ClientRegistry.registerKeyBinding(AnimationRouletteKey.ANIMATION_ROULETTE_KEY);
        ClientRegistry.registerKeyBinding(DebugAnimationKey.DEBUG_ANIMATION_KEY);
        ClientRegistry.registerKeyBinding(ExtraPlayerConfigKey.EXTRA_PLAYER_RENDER_KEY);
        ExtraAnimationKey.registerKeyBinding();

        CrossbowCompat.init();
        ElytraCompat.init();
        SwimmingCompat.init();
        TridentCompat.init();

        AnimationRegister.registerAnimationState();
        AnimationRegister.registerVariables();

        RenderManager context = Minecraft.getMinecraft().getRenderManager();
        CUSTOM_PLAYER_RENDERER = new CustomPlayerRenderer(context);
        CUSTOM_ARROW_RENDERER = new CustomArrowRenderer(context);
    }
}
