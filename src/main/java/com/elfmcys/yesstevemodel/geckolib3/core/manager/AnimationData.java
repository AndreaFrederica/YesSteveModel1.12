/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.manager;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.List;

@SuppressWarnings("rawtypes")
public class AnimationData {
    private final List<IAnimationController> animationControllers = new ReferenceArrayList<>(32);
    private final Object2ReferenceOpenHashMap<String, IAnimationController> animationControllerMap = new Object2ReferenceOpenHashMap<>(16);
    public double tick;
    public boolean isFirstTick = true;
    public double startTick = -1;
    public boolean shouldPlayWhilePaused = false;
    private double resetTickLength = 1;

    public AnimationData() {
    }

    public IAnimationController addAnimationController(IAnimationController value) {
        this.animationControllers.add(value);
        return value;
    }

    public double getResetSpeed() {
        return this.resetTickLength;
    }

    /**
     * 这是任何没有动画的骨骼恢复到其初始位置所需的时间
     *
     * @param resetTickLength 重置时所需的 tick。不能为负数
     */
    public void setResetSpeedInTicks(double resetTickLength) {
        this.resetTickLength = resetTickLength < 0 ? 0 : resetTickLength;
    }

    public List<IAnimationController> getAnimationControllers() {
        return this.animationControllers;
    }

    public IAnimationController getAnimationControllerByName(String name) {
        if (this.animationControllerMap.isEmpty() && !this.animationControllers.isEmpty()) {
            for (IAnimationController controller : this.animationControllers) {
                this.animationControllerMap.put(controller.getName(), controller);
            }
        }
        return this.animationControllerMap.get(name);
    }

    public void clear() {
        this.tick = 0;
        this.isFirstTick = true;
        this.startTick = -1;
        for (IAnimationController controller : this.animationControllers) {
            controller.reset();
        }
        this.animationControllers.clear();
        this.animationControllerMap.clear();
    }
}
