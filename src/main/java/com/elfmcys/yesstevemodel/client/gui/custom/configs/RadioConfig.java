package com.elfmcys.yesstevemodel.client.gui.custom.configs;

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;

public class RadioConfig extends AbstractConfig {
    public static final String TYPE = "radio";

    private final OrderedStringMap<String, String> labels;

    public RadioConfig(String title, String description, String value, OrderedStringMap<String, String> labels) {
        super(TYPE, title, description, value);
        this.labels = labels;
    }

    public OrderedStringMap<String, String> getLabels() {
        return this.labels;
    }
}