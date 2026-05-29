// TODO: [Phase 7] Mod 兼容 — TACZ (Timeless and Classics Zero) 枪械 mod。
//  1.12 版 TACZ 不存在（或 API 完全不同），当前为最小 stub。
package com.elfmcys.yesstevemodel.client.compat.gun.tacz;

import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class TacCompat {
    public static boolean isLoaded() {
        return false;
    }

    @Nullable
    public static PlayState handleGunHoldAnimState(ItemStack stack, AnimationEvent<?> event) {
        return null;
    }

    @Nullable
    public static PlayState handleGunActionAnimState(ItemStack stack, AnimationEvent<?> event) {
        return null;
    }

    @Nullable
    public static PlayState handleTaczAnimState(EntityLivingBase entity, AnimationEvent<?> event, String animName, ILoopType loopType) {
        return null;
    }
}
