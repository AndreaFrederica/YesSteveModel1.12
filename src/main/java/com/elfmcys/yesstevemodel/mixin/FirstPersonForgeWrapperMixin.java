package com.elfmcys.yesstevemodel.mixin;

import com.elfmcys.yesstevemodel.client.compat.FirstPersonCompat;
import com.elfmcys.yesstevemodel.util.Keep;
import dev.tr7zw.firstperson.forge.ForgeWrapper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeWrapper.class)
@SuppressWarnings("all")
public class FirstPersonForgeWrapperMixin {
    @Keep
    @Inject(method = "getOffset()Ljava/lang/Object;", at = @At("RETURN"), remap = false, cancellable = true)
    private void getOffset(CallbackInfoReturnable<Object> cir) {
        Vector3d current = (Vector3d) cir.getReturnValue();
        cir.setReturnValue(FirstPersonCompat.transformPlayerOffset(current));
    }
}
