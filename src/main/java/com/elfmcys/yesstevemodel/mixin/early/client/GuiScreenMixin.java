package com.elfmcys.yesstevemodel.mixin.early.client;

import com.elfmcys.yesstevemodel.util.ComponentUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiScreen.class)
public class GuiScreenMixin {
    @Inject(
            method = "handleComponentClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/text/event/ClickEvent;getAction()Lnet/minecraft/util/text/event/ClickEvent$Action;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void handleCopyEvent(ITextComponent component, CallbackInfoReturnable<Boolean> cir) {
        ClickEvent clickEvent = component.getStyle().getClickEvent();
        if (clickEvent != null && clickEvent.getAction() == ComponentUtils.COPY_TO_CLIPBOARD) {
            GuiScreen.setClipboardString(clickEvent.getValue());
            cir.cancel();
        }
    }
}
