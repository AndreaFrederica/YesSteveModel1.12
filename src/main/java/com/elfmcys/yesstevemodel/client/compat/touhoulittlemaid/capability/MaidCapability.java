// TODO: [Phase 7] Mod 兼容 — TouhouLittleMaid 女仆实体 capability。
//  1.12 版 TLM 的实体类型和 capability 系统与高版本完全不同，当前为最小 stub。
package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability;

import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;

public class MaidCapability extends GeoEntity<EntityLivingBase> {

    public MaidCapability(@Nullable EntityLivingBase entity) {
        super(entity, 60);
    }

    @Override
    public net.minecraft.util.ResourceLocation getModelLocation() {
        return null;
    }

    @Override
    public net.minecraft.util.ResourceLocation getTextureLocation() {
        return null;
    }

    @Override
    public net.minecraft.util.ResourceLocation getAnimationFileLocation() {
        return null;
    }
}
