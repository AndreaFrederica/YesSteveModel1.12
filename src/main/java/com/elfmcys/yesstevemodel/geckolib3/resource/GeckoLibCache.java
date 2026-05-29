package com.elfmcys.yesstevemodel.geckolib3.resource;

import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import com.elfmcys.yesstevemodel.client.animation.molang.FnBinding;
import com.elfmcys.yesstevemodel.client.animation.molang.YSMBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class GeckoLibCache {
    private static GeckoLibCache INSTANCE;
    public final MolangParser parser = createMolangParser();
    private final Map<ResourceLocation, AnimationFile> animations = Maps.newHashMap();
    private final Map<ResourceLocation, GeoModel> geoModels = Maps.newHashMap();

    public static GeckoLibCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeckoLibCache();
            return INSTANCE;
        }
        return INSTANCE;
    }

    public Map<ResourceLocation, AnimationFile> getAnimations() {
        return this.animations;
    }

    public Map<ResourceLocation, GeoModel> getGeoModels() {
        return this.geoModels;
    }

    public static MolangParser getMolangParser() {
        return createMolangParser();
    }

    private static MolangParser createMolangParser() {
        HashMap<String, ObjectBinding> map = new HashMap<>();
        map.put("ysm", YSMBinding.INSTANCE);
//        map.put("tlm", TLMBinding.INSTANCE);
        map.put("ctrl", CtrlBinding.INSTANCE);
        map.put("fn", new FnBinding());
        return new MolangParser(map);
    }
}
