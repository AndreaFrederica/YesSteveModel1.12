package rip.ysm.compat.immersivemelodies;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;

public final class ImmersiveMelodiesCompat {
    private ImmersiveMelodiesCompat() {
    }

    public static void registerBindings(CtrlBinding binding) {
        binding.livingEntityVar("im_pitch", ctx -> 0.0f);
        binding.livingEntityVar("im_volume", ctx -> 0.0f);
        binding.livingEntityVar("im_current", ctx -> 0.0f);
        binding.livingEntityVar("im_delta", ctx -> 0L);
        binding.livingEntityVar("im_time", ctx -> 0L);
    }
}