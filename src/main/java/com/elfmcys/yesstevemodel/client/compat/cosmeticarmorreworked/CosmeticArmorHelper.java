// TODO: [Phase 7] Mod 兼容 — Cosmetic Armor Reworked。
//  当前为直通实现（直接调用 entity.getItemStackFromSlot），待 1.12 版 API 确定后可能需要适配
//  Cosmetic Armor Reworked 的外观覆盖逻辑。
package com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class CosmeticArmorHelper {
    public static ItemStack getArmorItem(EntityLivingBase entity, EntityEquipmentSlot slot) {
        return entity.getItemStackFromSlot(slot);
    }
}
