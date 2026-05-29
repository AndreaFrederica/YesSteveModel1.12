package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;

public class VehicleModelBundle {
    private final GeoModel model;
    private final Object2ReferenceMap<String, Animation> animations;
    private final Object2ReferenceMap<String, AnimationController> animationControllers;
    private final OuterFileTexture texture;

    public VehicleModelBundle(GeoModel model, Object2ReferenceMap<String, Animation> animations, Object2ReferenceMap<String, AnimationController> animationControllers, OuterFileTexture texture) {
        this.model = model;
        this.animations = animations;
        this.animationControllers = animationControllers;
        this.texture = texture;
    }

    public GeoModel getModel() {
        return this.model;
    }

    public Object2ReferenceMap<String, Animation> getAnimations() {
        return this.animations;
    }

    public Object2ReferenceMap<String, AnimationController> getAnimationControllers() {
        return this.animationControllers;
    }

    public OuterFileTexture getTexture() {
        return this.texture;
    }
}
