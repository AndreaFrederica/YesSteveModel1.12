// TODO: [Phase 7] Mod 兼容 — TouhouLittleMaid 女仆 mod。
//  1.12 版的 TLM API 与高版本完全不同（实体类型、capability、渲染均不同），不能简单移植。
//  当前所有方法返回 false/null，待 1.12 版 TLM API 确定后重新实现。
package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;

public class TouhouLittleMaidCompat {

    public static boolean isLoaded() {
        return false;
    }

    public static boolean isMaidEntity(Entity entity) {
        return false;
    }

    public static boolean isMaidSitting(EntityLivingBase entity) {
        return false;
    }

    @Nullable
    public static PlayState handleMaidInteraction(AnimationEvent<?> event, EntityPlayer player, Entity vehicle) {
        return null;
    }
}
