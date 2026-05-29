package com.elfmcys.yesstevemodel.geckolib3.core;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class EntityFrameStateTracker<T extends Entity> {
    protected final @Nullable T entity;

    private int currentTick;
    private Vec3d lastPosition;
    private Vec3d positionDelta = Vec3d.ZERO;
    private String cachedModelId;
    private float currentTime;
    private float timeDelta;
    private final IntOpenHashSet processedStates = new IntOpenHashSet();

    public EntityFrameStateTracker(@Nullable T entity) {
        this.entity = entity;
    }

    public void reset() {
        this.processedStates.clear();
        this.currentTick = 0;
        this.lastPosition = null;
        this.positionDelta = Vec3d.ZERO;
        this.cachedModelId = null;
        this.currentTime = 0.0f;
        this.timeDelta = 0.0f;
    }

    public void updateState(int tickCount, float seekTime, float partialTick) {
        if (this.currentTick < tickCount) {
            this.processedStates.clear();
            this.currentTick = tickCount;
        }
        if (this.currentTime < seekTime) {
            this.timeDelta = seekTime - this.currentTime;
            this.currentTime = seekTime;
            this.cachedModelId = null;
            this.updatePosition(partialTick);
        }
    }

    private void updatePosition(float partialTick) {
        if (this.entity == null) {
            this.positionDelta = Vec3d.ZERO;
            return;
        }
        double x = this.entity.prevPosX + (this.entity.posX - this.entity.prevPosX) * partialTick;
        double y = this.entity.prevPosY + (this.entity.posY - this.entity.prevPosY) * partialTick;
        double z = this.entity.prevPosZ + (this.entity.posZ - this.entity.prevPosZ) * partialTick;
        Vec3d position = new Vec3d(x, y, z);
        if (this.lastPosition != null) {
            this.positionDelta = position.subtract(this.lastPosition);
        }
        this.lastPosition = position;
    }

    public boolean markProcessed(int state) {
        return this.processedStates.add(state);
    }

    public boolean isProcessed(int state) {
        return this.processedStates.contains(state);
    }

    public Vec3d getPositionDelta() {
        return this.positionDelta;
    }

    @Nullable
    public String getCachedModelId() {
        return this.cachedModelId;
    }

    public void setCachedModelId(String cachedModelId) {
        this.cachedModelId = cachedModelId;
    }

    public float getTimeDelta() {
        return this.timeDelta;
    }
}
