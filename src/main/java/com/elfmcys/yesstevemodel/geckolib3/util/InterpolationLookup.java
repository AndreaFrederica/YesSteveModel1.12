package com.elfmcys.yesstevemodel.geckolib3.util;

import java.util.List;

public class InterpolationLookup<T> {
    private final List<T> keyframes;
    private final float startTime;
    private final float endTime;
    private final FrameTimeProvider<T> timeProvider;
    private int currentIndex = 0;
    private float rangeStart;
    private float rangeEnd;

    @FunctionalInterface
    public interface FrameTimeProvider<T> {
        float apply(T value);
    }

    public InterpolationLookup(List<T> keyframes, float startTime, FrameTimeProvider<T> timeProvider) {
        this.keyframes = keyframes;
        this.startTime = startTime;
        this.endTime = timeProvider.apply(keyframes.get(keyframes.size() - 1));
        this.timeProvider = timeProvider;
        this.rangeStart = startTime;
        this.rangeEnd = this.timeProvider.apply(keyframes.get(0));
        if (startTime > this.rangeEnd || startTime > this.endTime || this.rangeEnd > this.endTime) {
            throw new IllegalArgumentException();
        }
    }

    public T getAtTime(float time) {
        if (this.keyframes.size() == 1) {
            return this.keyframes.get(0);
        }
        if (time < this.rangeStart) {
            T first = this.keyframes.get(0);
            if (this.currentIndex == 0) {
                return first;
            }
            this.currentIndex = 0;
            this.rangeStart = this.startTime;
            this.rangeEnd = this.timeProvider.apply(first);
            return time >= this.rangeEnd ? getAtTime(time) : first;
        }
        if (time == this.rangeStart || time < this.rangeEnd || this.rangeEnd == this.endTime) {
            return this.keyframes.get(this.currentIndex);
        }
        if (time >= this.endTime) {
            T last = this.keyframes.get(this.keyframes.size() - 1);
            float previousTime = this.timeProvider.apply(this.keyframes.get(this.keyframes.size() - 2));
            float lastTime = this.timeProvider.apply(last);
            if (previousTime > lastTime) {
                throw new IllegalArgumentException();
            }
            this.currentIndex = this.keyframes.size() - 1;
            this.rangeStart = previousTime;
            this.rangeEnd = lastTime;
            return last;
        }
        float previousEnd = this.rangeEnd;
        int nextIndex = this.currentIndex + 1;
        while (true) {
            T next = this.keyframes.get(nextIndex);
            float nextTime = this.timeProvider.apply(next);
            if (previousEnd > nextTime) {
                throw new IllegalArgumentException();
            }
            if (time < nextTime) {
                this.currentIndex = nextIndex;
                this.rangeStart = previousEnd;
                this.rangeEnd = nextTime;
                return next;
            }
            previousEnd = nextTime;
            nextIndex++;
        }
    }

    public float getStartTime() {
        return this.startTime;
    }

    public float getEndTime() {
        return this.endTime;
    }
}