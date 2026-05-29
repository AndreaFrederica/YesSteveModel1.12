package com.elfmcys.yesstevemodel.geckolib3.util;

import java.util.ArrayList;
import java.util.List;

public class LinearKeyframeInterpolator implements IInterpolable {
    private final List<Segment> segments;
    private final InterpolationLookup<Segment> lookup;

    public LinearKeyframeInterpolator(float[] keys, float[] values) {
        List<Segment> segmentList = new ArrayList<>(Math.max(0, keys.length - 1));
        for (int index = 0; index < keys.length - 1; index++) {
            float startTimeTick = keys[index] * 20.0f;
            float endTimeTick = keys[index + 1] * 20.0f;
            float startValue = 1.0f - values[index];
            float endValue = 1.0f - values[index + 1];
            segmentList.add(new Segment(startTimeTick, endTimeTick, startValue, endValue));
        }
        this.segments = segmentList;
        this.lookup = new InterpolationLookup<>(this.segments, 0.0f, segment -> segment.endTime);
    }

    private LinearKeyframeInterpolator(List<Segment> segments) {
        this.segments = segments;
        this.lookup = new InterpolationLookup<>(this.segments, 0.0f, segment -> segment.endTime);
    }

    @Override
    public float interpolate(float time) {
        Segment segment = this.lookup.getAtTime(time);
        if (time <= segment.startTime) {
            return segment.startValue;
        }
        if (time >= segment.endTime) {
            return segment.startValue + segment.valueDelta;
        }
        return segment.startValue + (segment.valueDelta * ((time - segment.startTime) / segment.duration));
    }

    @Override
    public float getProgress() {
        return this.lookup.getEndTime();
    }

    @Override
    public LinearKeyframeInterpolator asInterpolator() {
        return new LinearKeyframeInterpolator(this.segments);
    }

    private static final class Segment {
        private final float startTime;
        private final float duration;
        private final float endTime;
        private final float startValue;
        private final float valueDelta;

        private Segment(float startTime, float endTime, float startValue, float endValue) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = endTime - startTime;
            this.startValue = startValue;
            this.valueDelta = endValue - startValue;
        }
    }
}