package com.elfmcys.yesstevemodel.model;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Converter;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.model.format.Type;
import com.elfmcys.yesstevemodel.model.format.access.IModelAccess;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.RequestSyncModel;
import com.elfmcys.yesstevemodel.util.GetJarResources;
import com.elfmcys.yesstevemodel.util.Md5Utils;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.elfmcys.yesstevemodel.model.format.FormatManager.*;

public final class ServerModelManager {
    /**
     * 配置相关文件夹
     */
    public static final Path FOLDER = Paths.get("config", YesSteveModel.MOD_ID);

    /**
     * 内置模型输出的文件夹
     */
    public static final Path BUILTIN = FOLDER.resolve("builtin");
    /**
     * 自定义模型所放置的文件夹
     */
    public static final Path CUSTOM = FOLDER.resolve("custom");
    public static final Path AUTH = FOLDER.resolve("auth");
    public static final Path EXPORT = FOLDER.resolve("export");

    /**
     * 生成缓存文件的文件夹
     */
    public static final Path CACHE = FOLDER.resolve("cache");
    public static final Path CACHE_SERVER = CACHE.resolve("server");
    /**
     * 存储密码的文件
     */
    public static final Path PASSWORD_FILE = CACHE_SERVER.resolve("PASSWORD");
    public static final Path CACHE_CLIENT = CACHE.resolve("client");
    /**
     * 模型名称 -> 模型额外信息缓存
     * 可以方便的通过此缓存，来判断客户端发来的 MD5 在不在服务端
     * 从而将服务器文件发送给玩家
     * 还可以获取其他服务端模型信息
     */
    public static final Map<String, ServerModelInfo> CACHE_NAME_INFO = Maps.newHashMap();

    /**
     * 放置授权模型名称
     */
    public static final Set<String> AUTH_MODELS = Sets.newHashSet();

    public static void sendRequestSyncModelMessage(PlayerList playerList) {
        for (EntityPlayerMP player : playerList.getPlayers()) {
            NetworkHandler.sendToClientPlayer(new RequestSyncModel(), player);
        }
    }

    public static void sendRequestSyncModelMessage() {
        ClientModelManager.sendSyncModelMessage();
    }

    public static void sendRequestSyncModelMessage(EntityPlayer player) {
        NetworkHandler.sendToClientPlayer(new RequestSyncModel(), player);
    }

    public static void reloadPacks() {
        CACHE_NAME_INFO.clear();
        AUTH_MODELS.clear();

        createFolder(FOLDER);
        createFolder(CUSTOM);
        createFolder(AUTH);
        createFolder(EXPORT);

        createFolder(CACHE);
        createFolder(CACHE_SERVER);
        createFolder(CACHE_CLIENT);

        deleteFolder(BUILTIN);
        GetJarResources.copyFolder(getCustomFiles("builtin"), BUILTIN.getParent(), "builtin");
        initPassword();
        cacheAllModels(BUILTIN);
        cacheAllModels(CUSTOM);
        cacheAllModels(AUTH);
    }

    private static void initPassword() {
        try {
            EncryptTools.createRandomPassword();
            File passwordFile = PASSWORD_FILE.toFile();
            if (passwordFile.isFile()) {
                EncryptTools.readPassword(FileUtils.readFileToByteArray(passwordFile));
            } else {
                FileUtils.writeByteArrayToFile(passwordFile, EncryptTools.writePassword());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCustomFiles(String path) {
        return String.format("/assets/%s/%s", YesSteveModel.MOD_ID, path);
    }

    private static void createFolder(Path path) {
        File folder = path.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteFolder(Path path) {
        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    模型加载
     */

    public static void cacheAllModels(Path rootPath) {
        File[] files = rootPath.toFile().listFiles();
        if (files == null) return;
        for (File file : files) {
            Type type = Type.getType(file);
            if (type == Type.UNKNOWN) continue;
            String modelId = type.getFileName(file);
            if (!ResourceUtil.isValidResourceLocation(modelId)) continue;
            loadLegacyModel(rootPath, file, modelId, type);
        }
    }

    private static void loadLegacyModel(Path rootPath, File file, String modelId, Type type) {
        try (IModelAccess access = type.createAccess(file)) {
            if (access == null) return;
            boolean isAuth = rootPath.equals(AUTH);
            ModelData modelData = getModelData(access, modelId, isAuth, type);
            if (modelData == null) return;
            ServerModelInfo info = cacheModel(modelData);
            CACHE_NAME_INFO.put(modelId, info);
            if (isAuth) AUTH_MODELS.add(modelId);
        } catch (Exception e) {
            YesSteveModel.LOGGER.warn("Failed to load {} model: {}", type.getName(), file.getName(), e);
        }
    }

    /**
     * @return 若为 null 则代表模型无效。
     */
    @Nullable
    public static ModelData getModelData(IModelAccess access, String modelId, boolean isAuth, Type type) throws IOException {
        Map<String, byte[]> model = Maps.newHashMap();
        // info.json
        byte[] infoBytes = access.readFile(INFO_FILE_NAME);
        if (infoBytes != null && infoBytes.length > 0) {
            String infoJson = new String(infoBytes, StandardCharsets.UTF_8);
            ExtraInfo info = YesSteveModel.GSON.fromJson(infoJson, ExtraInfo.class);
            model.put(INFO_NAME, ObjectStreamUtil.toByteArray(info));
        }
        for (String modelName : MODEL_NAMES) {
            byte[] data = access.readFile(getModelFileName(modelName));
            if (data == null || data.length == 0) {
                if (isModelNameNecessary(modelName)) return null;
                continue;
            }
            String json = new String(data, StandardCharsets.UTF_8);
            RawGeoModel rawModel = Converter.fromJsonString(json);
            model.put(modelName, ObjectStreamUtil.toByteArray(rawModel));
        }

        Map<String, byte[]> texture = Maps.newHashMap();
        for (String pngPath : access.listFiles(".png")) {
            byte[] data = access.readFile(pngPath);
            if (data != null) {
                texture.put(pngPath, data);
            }
        }

        Map<String, byte[]> animation = Maps.newHashMap();
        for (String animName : ANIMATION_NAMES) {
            byte[] animData = access.readFile(getAnimFileName(animName));
            if (animData == null || animData.length == 0) {
                java.io.File defaultFile = getDefaultAnimFile(animName);
                if (defaultFile.isFile()) {
                    animData = FileUtils.readFileToByteArray(defaultFile);
                }
            }
            if (animData != null) {
                animation.put(animName, animData);
            }
        }

        return new ModelData(modelId, isAuth, type, model, texture, animation);
    }

    /**
     * 使用 {@link EncryptTools#assembleEncryptModels(ModelData)} 加密，并以 MD5 值命名存入服务端缓存目录。
     *
     * @param modelData 封装好的序列化模型文件二进制流
     */
    @Nonnull
    private static ServerModelInfo cacheModel(ModelData modelData) throws IOException {
        byte[] dataBytes = EncryptTools.assembleEncryptModels(modelData);
        modelData.setMd5(Md5Utils.md5Hex(dataBytes).toUpperCase(Locale.US));
        FileUtils.writeByteArrayToFile(CACHE_SERVER.resolve(modelData.getInfo().getMd5()).toFile(), dataBytes);
        return modelData.getInfo();
    }
}
