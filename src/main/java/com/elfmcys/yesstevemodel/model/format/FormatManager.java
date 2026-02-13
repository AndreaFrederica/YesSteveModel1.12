package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.model.ServerModelManager;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public final class FormatManager {
    // name 不带后缀名，file name 带后缀名
    // HashSet 无序
    public static final Set<String> MODEL_NAMES = new HashSet<>();
    public static final Set<String> ANIMATION_NAMES = new HashSet<>();

    static {
        MODEL_NAMES.add("main");
        MODEL_NAMES.add("arm");
        MODEL_NAMES.add("arrow");

        ANIMATION_NAMES.add("main");
        ANIMATION_NAMES.add("arm");
        ANIMATION_NAMES.add("extra");
        ANIMATION_NAMES.add("tac");
        ANIMATION_NAMES.add("carryon");
        ANIMATION_NAMES.add("arrow");
    }

    public static final String INFO_NAME = "info";
    public static final String INFO_FILE_NAME = "info.json";
    public static final String MAIN_MODEL_FILE_NAME = "main.json";
    public static final String ARM_MODEL_FILE_NAME = "arm.json";

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

    public static String removeExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex != -1) {
            fileName = fileName.substring(0, lastIndex);
        }
        return fileName;
    }
}
