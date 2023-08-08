package com.elfmcys.yesstevemodel.mixin.plugin;

import com.elfmcys.yesstevemodel.client.compat.FirstPersonCompat;
import com.elfmcys.yesstevemodel.util.Keep;
import com.google.common.collect.Lists;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class MixinTweaker implements IMixinConfigPlugin {
    public MixinTweaker() {
        FirstPersonCompat.init();
    }

    @Keep
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Keep
    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Keep
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Keep
    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Keep
    @Override
    public List<String> getMixins() {
        if (FirstPersonCompat.isInstalled()) {
            return Lists.newArrayList("FirstPersonForgeWrapperMixin");
        } else {
            return null;
        }
    }

    @Keep
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Keep
    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
