package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.client.animation.molang.functions.physics.IPhysics;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import javax.annotation.Nullable;

public class PhysicsManager {
    private final Object2ReferenceOpenHashMap<String, IPhysics> physicsValues = new Object2ReferenceOpenHashMap<>(16);
    private float lastRenderTicks = 0;

    public void update(float renderTicks) {
        if (this.lastRenderTicks > 0) {
            if (renderTicks > this.lastRenderTicks) {
                float interval = (renderTicks - this.lastRenderTicks) / 20f;
                this.lastRenderTicks = renderTicks;
                this.physicsValues.forEach((key, value) -> value.update(interval));
            }
        } else {
            this.lastRenderTicks = renderTicks;
        }
    }

    public void put(String key, IPhysics physics) {
        this.physicsValues.put(key, physics);
    }

    @Nullable
    public IPhysics get(String key) {
        return this.physicsValues.get(key);
    }

    public void reset() {
        this.lastRenderTicks = 0;
        this.physicsValues.clear();
    }
}
