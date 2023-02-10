package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.client.event.RegisterEntityRenderersEvent;

public final class ControllerUtils {
    public static final String MAIN_CONTROLLER = "main_controller";
    public static final String USE_CONTROLLER = "use_controller";
    public static final String CAP_CONTROLLER = "cap_controller";

    /**
     * 重载动画用的 <br>
     * 部分动画是单次播放的，由于 GeckoLib 的机制，那么再次触发后将不会播放 <br>
     * 这个工具就是用来重载的
     *
     * @param name 动画控制器名
     */
    public static void markNeedsReload(String name) {
        RegisterEntityRenderersEvent.getInstance().getCustomPlayerEntity().getFactory()
                .getOrCreateAnimationData(0).getAnimationControllers().get(name).markNeedsReload();
    }

    public static void markCapControllerReload() {
        markNeedsReload(CAP_CONTROLLER);
    }
}
