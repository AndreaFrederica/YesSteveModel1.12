package com.elfmcys.yesstevemodel.config;

import com.elfmcys.yesstevemodel.config.util.ConfigBuilder;

import javax.annotation.Nonnull;

public class GeneralConfig {
    public static boolean DISCLAIMER_SHOW = true;
    public static boolean PRINT_ANIMATION_ROULETTE_MSG = true;
    public static boolean DISABLE_SELF_MODEL = false;
    public static boolean DISABLE_OTHER_MODEL = false;
    public static boolean DISABLE_SELF_HANDS = false;
    public static String DEFAULT_MODEL_ID = "default";
    public static String DEFAULT_MODEL_TEXTURE = "default.png";

    static void build(@Nonnull ConfigBuilder builder) {
        builder.pushCategory("general");

        DISCLAIMER_SHOW = builder.get(
                "DisclaimerShow",
                DISCLAIMER_SHOW,
                "Whether to display disclaimer GUI"
        );

        PRINT_ANIMATION_ROULETTE_MSG = builder.get(
                "PrintAnimationRouletteMsg",
                PRINT_ANIMATION_ROULETTE_MSG,
                "Whether to print animation roulette play message"
        );

        DISABLE_SELF_MODEL = builder.get(
                "DisableSelfModel",
                DISABLE_SELF_MODEL,
                "Prevents rendering of self player's model"
        );

        DISABLE_OTHER_MODEL = builder.get(
                "DisableOtherModel",
                DISABLE_OTHER_MODEL,
                "Prevents rendering of other player's model"
        );

        DISABLE_SELF_HANDS = builder.get(
                "DisableSelfHands",
                DISABLE_SELF_HANDS,
                "Prevents rendering of self player's hand"
        );

        DEFAULT_MODEL_ID = builder.get(
                "DefaultModelId",
                DEFAULT_MODEL_ID,
                "The default model ID when a player first enters the game"
        );

        DEFAULT_MODEL_TEXTURE = builder.get(
                "DefaultModelTexture",
                DEFAULT_MODEL_TEXTURE,
                "The default model texture when a player first enters the game"
        );

        builder.popCategory();
    }
}
