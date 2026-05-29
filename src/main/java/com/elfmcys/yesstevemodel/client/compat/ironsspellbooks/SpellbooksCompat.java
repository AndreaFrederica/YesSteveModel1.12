// TODO: [Phase 7] Mod 兼容 — Iron's Spellbooks 魔法 mod。
//  1.12 版 Iron's Spellbooks 不存在（或 API 完全不同），当前为最小 stub。
package com.elfmcys.yesstevemodel.client.compat.ironsspellbooks;

import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;

public class SpellbooksCompat {

    @Nullable
    public static PlayState resolvePlayState(AnimationEvent<?> event, EntityLivingBase livingEntity) {
        return null;
    }
}
