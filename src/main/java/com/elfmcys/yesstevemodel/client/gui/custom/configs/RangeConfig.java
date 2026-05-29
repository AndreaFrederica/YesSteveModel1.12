package com.elfmcys.yesstevemodel.client.gui.custom.configs;

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;

public class RangeConfig extends AbstractConfig {
    public static final String TYPE = "range";

    private final double step;
    private final double min;
    private final double max;

    public RangeConfig(String title, String description, String value, double step, double min, double max) {
        super(TYPE, title, description, value);
        this.step = step;
        this.min = min;
        this.max = max;
    }

    public double getStep() {
        return this.step;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }
}