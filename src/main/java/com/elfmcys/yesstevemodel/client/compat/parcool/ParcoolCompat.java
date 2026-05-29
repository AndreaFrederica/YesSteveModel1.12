// TODO: [Phase 7] Mod 兼容 — Parcool 跑酷 mod。
//  1.12 版 Parcool 不存在（或 API 完全不同），当前为最小 stub。
package com.elfmcys.yesstevemodel.client.compat.parcool;

import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;

public class ParcoolCompat {

    @Nullable
    public static String getActionName(EntityPlayer player) {
        return null;
    }
}
