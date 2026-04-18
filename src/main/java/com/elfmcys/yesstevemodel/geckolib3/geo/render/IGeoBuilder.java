package com.elfmcys.yesstevemodel.geckolib3.geo.render;

import com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree.RawGeometryTree;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;

public interface IGeoBuilder {
    GeoModel constructGeoModel(RawGeometryTree geometryTree);
}
