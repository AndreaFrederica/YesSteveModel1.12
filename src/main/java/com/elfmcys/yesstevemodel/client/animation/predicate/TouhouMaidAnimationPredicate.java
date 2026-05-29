// TODO: [Phase 7] Mod 兼容 — TouhouLittleMaid 女仆动画 predicate。
//  1.12 版的 TouhouLittleMaid mod API 与高版本完全不同，不能简单移植。
//  当前为死代码（未在 PlayerAnimationController 中注册），待 1.12 版 TLM API 确定后重新实现。
package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;

@SuppressWarnings("rawtypes")
public class TouhouMaidAnimationPredicate implements IAnimationPredicate {

    @Override
    public PlayState predicate(AnimationEvent event, ExpressionEvaluator evaluator) {
        // TODO: [Phase 7] 需要 1.12 版 TouhouLittleMaid API 后重新实现
        return PlayState.STOP;
    }
}
