package com.elfmcys.yesstevemodel.geckolib3.file;

import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnimationControllerFile {
    private final Map<String, AnimationController> animationControllers;

    public AnimationControllerFile(Map<String, AnimationController> animationControllers) {
        this.animationControllers = Collections.unmodifiableMap(new LinkedHashMap<>(animationControllers));
    }

    public Map<String, AnimationController> getAnimationControllers() {
        return this.animationControllers;
    }
}