package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.TimeUnit;

@SideOnly(Side.CLIENT)
public final class AnimatableCacheUtil {
    /// 用于背景、手臂、模型按钮渲染的动画实体
    public static final Cache<ResourceLocation, IAnimatable> ANIMATABLE_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    /// 仅用于纹理选择界面的动画实体
    public static final Cache<ResourceLocation, IAnimatable> TEXTURE_GUI_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    /// 纹理选择界面实体缓存
    public static final Cache<ResourceLocation, Entity> ENTITIES_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
}
