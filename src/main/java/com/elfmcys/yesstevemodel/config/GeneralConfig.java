package com.elfmcys.yesstevemodel.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GeneralConfig {
    public static ForgeConfigSpec.BooleanValue DISCLAIMER_SHOW;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        init(builder);
        return builder.build();
    }

    public static void init(ForgeConfigSpec.Builder builder) {
        builder.push("general");

        builder.comment("Whether to display disclaimer GUI");
        DISCLAIMER_SHOW = builder.define("DisclaimerShow", true);

        builder.pop();
    }
}
