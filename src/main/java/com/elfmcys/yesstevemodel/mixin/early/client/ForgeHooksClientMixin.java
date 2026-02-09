package com.elfmcys.yesstevemodel.mixin.early.client;

import com.elfmcys.yesstevemodel.config.GeneralConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class ForgeHooksClientMixin {
    @Inject(method = "renderSpecificFirstPersonHand", at = @At("HEAD"), cancellable = true)
    private static void cancelRenderHandEvent(
            EnumHand hand, float partialTicks, float interpPitch,
            float swingProgress, float equipProgress, ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (GeneralConfig.DISABLE_SELF_MODEL || GeneralConfig.DISABLE_SELF_HANDS) return;
        cir.setReturnValue(false);
    }
}
