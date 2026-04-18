package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Set;

public final class FormatManager {
    // name 不带后缀名，file name 带后缀名
    // HashSet 无序
    public static final Set<String> MODEL_NAMES = Sets.newHashSet(
            "main", "arm", "arrow"
    );
    public static final Set<String> ANIMATION_NAMES = Sets.newHashSet(
            "main", "arm", "extra", "tac", "carryon", "arrow"
    );

    public static final String INFO_NAME = "info";
    public static final String INFO_FILE_NAME = "info.json";
    public static final String MAIN_MODEL_NAME = "main";
    public static final String MAIN_MODEL_FILE_NAME = "main.json";
    public static final String ARM_MODEL_FILE_NAME = "arm.json";
    public static final String ARROW_TEXTURE_FILE_NAME = "arrow.png";

    public static boolean isModelNameNecessary(String modelName) {
        return modelName.equals("main") || modelName.equals("arm");
    }

    public static String getModelFileName(String modelName) {
        return modelName + ".json";
    }

    public static String getAnimFileName(String animName) {
        return animName + ".animation.json";
    }

    public static String getDefaultAnimFileName(String animName) {
        return "default/" + getAnimFileName(animName);
    }

    public static File getDefaultAnimFile(String animName) {
        return ServerModelManager.BUILTIN.resolve(FormatManager.getDefaultAnimFileName(animName)).toFile();
    }
}
