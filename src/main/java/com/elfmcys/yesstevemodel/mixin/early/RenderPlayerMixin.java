package com.elfmcys.yesstevemodel.mixin.early;

import com.elfmcys.yesstevemodel.client.event.RenderArmEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Forge 方案，看 Cleanroom 合不合 PR
@Mixin(RenderPlayer.class)
public class RenderPlayerMixin {
    @Inject(method = "renderLeftArm", at = @At("HEAD"), cancellable = true)
    private void addRenderLeftArmEvent(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new RenderArmEvent(clientPlayer, EnumHandSide.LEFT))) {
            ci.cancel();
        }
    }

    @Inject(method = "renderRightArm", at = @At("HEAD"), cancellable = true)
    private void addRenderRightArmEvent(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new RenderArmEvent(clientPlayer, EnumHandSide.RIGHT))) {
            ci.cancel();
        }
    }
}
