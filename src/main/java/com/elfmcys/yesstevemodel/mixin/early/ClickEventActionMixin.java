package com.elfmcys.yesstevemodel.mixin.early;

import com.elfmcys.yesstevemodel.util.ComponentUtils;
import net.minecraft.util.text.event.ClickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ClickEvent.Action.class)
public class ClickEventActionMixin {
    @Shadow
    @Final
    private static Map<String, ClickEvent.Action> NAME_MAPPING;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onStaticInit(CallbackInfo ci) {
        if (ComponentUtils.COPY_TO_CLIPBOARD != null) {
            NAME_MAPPING.putIfAbsent(ComponentUtils.COPY_TO_CLIPBOARD.getCanonicalName(), ComponentUtils.COPY_TO_CLIPBOARD);
        }
    }
}
