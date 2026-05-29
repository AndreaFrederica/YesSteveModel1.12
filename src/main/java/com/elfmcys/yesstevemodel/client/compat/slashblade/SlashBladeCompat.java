// TODO: [Phase 7] Mod 兼容 — SlashBlade 拔刀 mod。
//  1.12 版 SlashBlade 的物品注册、连击系统与高版本不同，不能简单移植。
//  当前所有方法返回 false/null，待 1.12 版 SlashBlade API 确定后重新实现。
package com.elfmcys.yesstevemodel.client.compat.slashblade;

import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class SlashBladeCompat {

    public static boolean isSlashBladeItem(ItemStack stack) {
        return false;
    }

    @Nullable
    public static String getComboAnimName(AnimationEvent<?> event) {
        return null;
    }

    @Nullable
    public static PlayState handleSlashBladeAnim(EntityLivingBase entity, AnimationEvent<?> event, String animName, ILoopType loopType) {
        return null;
    }
}
