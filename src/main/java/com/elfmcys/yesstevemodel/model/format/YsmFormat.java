package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Converter;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.util.Md5Utils;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.elfmcys.yesstevemodel.util.YesModelUtils;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import static com.elfmcys.yesstevemodel.model.ServerModelManager.*;

public final class YsmFormat {
    public static void cacheAllModels(Path rootPath) {
        Collection<File> ysmFiles = FileUtils.listFiles(rootPath.toFile(), new String[]{"ysm"}, false);
        for (File ysmFile : ysmFiles) {
            String modelId = removeExtension(ysmFile.getName());
            if (!ResourceUtil.isValidResourceLocation(modelId)) {
                continue;
            }
            try {
                Map<String, byte[]> data = YesModelUtils.input(ysmFile);
                if (data.isEmpty()) {
                    continue;
                }
                if (!data.containsKey(MAIN_MODEL_FILE_NAME)) {
                    continue;
                }
                if (!data.containsKey(ARM_MODEL_FILE_NAME)) {
                    continue;
                }
                if (data.keySet().stream().noneMatch(fileName -> fileName.endsWith(".png"))) {
                    continue;
                }

                boolean isAuth = rootPath.equals(AUTH);
                ServerModelInfo info = cacheModel(data, modelId, isAuth);
                if (info != null) {
                    CACHE_NAME_INFO.put(modelId, info);
                    if (isAuth) AUTH_MODELS.add(modelId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static ServerModelInfo cacheModel(Map<String, byte[]> input, String modelId, boolean isAuth) {
        try {
            ModelData data = getModelData(input, modelId, isAuth);
            byte[] dataBytes = EncryptTools.assembleEncryptModels(data);
            data.setMd5(Md5Utils.md5Hex(dataBytes).toUpperCase(Locale.US));
            FileUtils.writeByteArrayToFile(CACHE_SERVER.resolve(data.getInfo().getMd5()).toFile(), dataBytes);
            return data.getInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nonnull
    private static ModelData getModelData(Map<String, byte[]> data, String modelId, boolean isAuth) throws IOException {
        Map<String, byte[]> model = Maps.newHashMap();
        if (data.containsKey(INFO_FILE_NAME)) {
            model.put("info", getBytes(data, INFO_FILE_NAME));
        }
        model.put("main", getBytes(data, MAIN_MODEL_FILE_NAME));
        model.put("arm", getBytes(data, ARM_MODEL_FILE_NAME));
        if (data.containsKey(ARROW_MODEL_FILE_NAME)) {
            model.put("arrow", getBytes(data, ARROW_MODEL_FILE_NAME));
        }

        Map<String, byte[]> texture = Maps.newHashMap();
        data.forEach((name, textureData) -> {
            if (name.endsWith(".png")) {
                texture.put(name, textureData);
            }
        });

        Map<String, byte[]> animation = Maps.newHashMap();
        animation.put("main", getBytes(data, MAIN_ANIMATION_FILE_NAME));
        animation.put("arm", getBytes(data, ARM_ANIMATION_FILE_NAME));
        animation.put("extra", getBytes(data, EXTRA_ANIMATION_FILE_NAME));
        animation.put("tac", getBytes(data, TAC_ANIMATION_FILE_NAME));
        animation.put("carryon", getBytes(data, CARRY_ON_ANIMATION_FILE_NAME));
        animation.put("arrow", getBytes(data, ARROW_ANIMATION_FILE_NAME));

        return new ModelData(modelId, isAuth, Type.YSM, model, texture, animation);
    }

    private static byte[] getBytes(Map<String, byte[]> data, String fileName) throws IOException {
        if (MAIN_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(MAIN_ANIMATION_FILE_NAME)) {
            Path filePath = BUILTIN.resolve("default/main.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }
        if (ARM_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(ARM_ANIMATION_FILE_NAME)) {
            Path filePath = BUILTIN.resolve("default/arm.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }
        if (EXTRA_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(EXTRA_ANIMATION_FILE_NAME)) {
            Path filePath = BUILTIN.resolve("default/extra.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }
        if (TAC_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(TAC_ANIMATION_FILE_NAME)) {
            Path filePath = BUILTIN.resolve("default/tac.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }
        if (CARRY_ON_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(CARRY_ON_ANIMATION_FILE_NAME)) {
            Path filePath = BUILTIN.resolve("default/carryon.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }
        if (ARROW_ANIMATION_FILE_NAME.equals(fileName) && !data.containsKey(ARROW_ANIMATION_FILE_NAME)) {
            Path filePath = BUILTIN.resolve("default/arrow.animation.json");
            return FileUtils.readFileToByteArray(filePath.toFile());
        }

        if (INFO_FILE_NAME.equals(fileName)) {
            String infoJson = new String(data.get(fileName), StandardCharsets.UTF_8);
            ExtraInfo info = YesSteveModel.GSON.fromJson(infoJson, ExtraInfo.class);
            return ObjectStreamUtil.toByteArray(info);
        }
        if (MAIN_MODEL_FILE_NAME.equals(fileName) || ARM_MODEL_FILE_NAME.equals(fileName) || ARROW_MODEL_FILE_NAME.equals(fileName)) {
            String modelJson = new String(data.get(fileName), StandardCharsets.UTF_8);
            RawGeoModel rawModel = Converter.fromJsonString(modelJson);
            return ObjectStreamUtil.toByteArray(rawModel);
        }

        return data.get(fileName);
    }
}
