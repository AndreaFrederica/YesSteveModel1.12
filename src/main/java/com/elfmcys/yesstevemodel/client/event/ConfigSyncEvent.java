package com.elfmcys.yesstevemodel.client.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.config.Config;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

//很奇怪，用注解注册不上。
//@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public class ConfigSyncEvent {
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(YesSteveModel.MOD_ID)) {
            Config.readFromProp();
        }
    }
}
