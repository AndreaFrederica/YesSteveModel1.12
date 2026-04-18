package com.elfmcys.yesstevemodel.geckolib3.geo.render.built;

import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ModelProperties;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.util.List;

@Desugar
public record GeoModel(List<GeoBone> topLevelBones, ModelProperties properties) {
    public GeoModel(List<GeoBone> topLevelBones, ModelProperties properties) {
        this.topLevelBones = ObjectLists.unmodifiable(new ObjectArrayList<>(topLevelBones));
        this.properties = properties;
    }
}
