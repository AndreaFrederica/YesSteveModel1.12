package com.elfmcys.yesstevemodel.client.gui.custom;

public class ExtraAnimationButtons {
    private final String id;
    private final String name;
    private final String description;
    private final AbstractConfig[] configForms;

    public ExtraAnimationButtons(String id, String name, String description, AbstractConfig[] configForms) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.configForms = configForms;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public AbstractConfig[] getConfigForms() {
        return this.configForms;
    }
}