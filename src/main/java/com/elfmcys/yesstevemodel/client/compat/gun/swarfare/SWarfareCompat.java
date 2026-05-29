// TODO: [Phase 7] Mod 兼容 — Superb Warfare 枪械 mod。
//  1.12 版 Superb Warfare 不存在（或 API 完全不同），当前为最小 stub。
package com.elfmcys.yesstevemodel.client.compat.gun.swarfare;

import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class SWarfareCompat {
    public static boolean isLoaded() {
        return false;
    }

    @Nullable
    public static PlayState handleGunHoldAnim(ItemStack stack, AnimationEvent<?> event) {
        return null;
    }

    @Nullable
    public static PlayState handleGunActionAnim(ItemStack stack, AnimationEvent<?> event) {
        return null;
    }
}
