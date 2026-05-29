package com.elfmcys.yesstevemodel.model;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.CheckboxConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RadioConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RangeConfig;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Converter;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.MinecraftGeometry;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ModelProperties;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.model.format.Type;
import com.elfmcys.yesstevemodel.model.format.access.IModelAccess;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.RequestSyncModel;
import com.elfmcys.yesstevemodel.resource.YSMBinaryDeserializer;
import com.elfmcys.yesstevemodel.resource.YSMBinarySerializer;
import com.elfmcys.yesstevemodel.resource.YSMFolderDeserializer;
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo;
import com.elfmcys.yesstevemodel.resource.models.MainModelInfo;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import com.elfmcys.yesstevemodel.util.GetJarResources;
import com.elfmcys.yesstevemodel.util.Md5Utils;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.elfmcys.yesstevemodel.util.data.StringMapPair;
import com.elfmcys.yesstevemodel.util.data.StringPair;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.apache.commons.io.FileUtils;
import rip.ysm.security.YSMByteBuf;
import rip.ysm.security.YsmCrypt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
            loadLegacyModel(rootPath, file, modelId, type);
        }
    }

    private static void loadLegacyModel(Path rootPath, File file, String modelId, Type type) {
        boolean isAuth = rootPath.equals(AUTH);
        try {
            RawYsmModel rawModel = tryLoadModernModel(file, null, type);
            if (rawModel != null) {
                if (isAuth && rawModel.properties != null) {
                    rawModel.properties.isFree = false;
                }
                modelId = resolveModernModelId(modelId, rawModel);
                ServerModelInfo info = cacheModernModel(rawModel, isAuth);
                CACHE_NAME_INFO.put(modelId, info);
                if (isAuth) AUTH_MODELS.add(modelId);
                YesSteveModel.LOGGER.info("Loaded modern {} model {} from {}", type.getName(), modelId, file.getName());
                return;
            }

            try (IModelAccess access = type.createAccess(file)) {
                if (access == null) return;
                rawModel = tryLoadModernModel(file, access, type);
                if (rawModel != null) {
                    if (isAuth && rawModel.properties != null) {
                        rawModel.properties.isFree = false;
                    }
                    modelId = resolveModernModelId(modelId, rawModel);
                    ServerModelInfo info = cacheModernModel(rawModel, isAuth);
                    CACHE_NAME_INFO.put(modelId, info);
                    if (isAuth) AUTH_MODELS.add(modelId);
                    YesSteveModel.LOGGER.info("Loaded modern {} model {} from {}", type.getName(), modelId, file.getName());
                    return;
                }
                rawModel = tryLoadFolderModel(access, type);
                if (rawModel != null) {
                    if (isAuth && rawModel.properties != null) {
                        rawModel.properties.isFree = false;
                    }
                    modelId = resolveModernModelId(modelId, rawModel);
                    ServerModelInfo info = cacheModernModel(rawModel, isAuth);
                    CACHE_NAME_INFO.put(modelId, info);
                    if (isAuth) AUTH_MODELS.add(modelId);
                    YesSteveModel.LOGGER.info("Loaded legacy {} model {} through OpenYSM parser from {}", type.getName(), modelId, file.getName());
                    return;
                }
                if (!ResourceUtil.isValidResourceLocation(modelId)) return;
                ModelData modelData = getModelData(access, modelId, isAuth, type);
                if (modelData == null) return;
                ServerModelInfo info = cacheModel(modelData);
                CACHE_NAME_INFO.put(modelId, info);
                if (isAuth) AUTH_MODELS.add(modelId);
            }
        } catch (Exception e) {
            YesSteveModel.LOGGER.warn("Failed to load {} model: {}", type.getName(), file.getName(), e);
        }
    }

    private static String resolveModernModelId(String fallbackModelId, RawYsmModel rawModel) {
        if (ResourceUtil.isValidResourceLocation(fallbackModelId)) {
            rawModel.modelId = fallbackModelId;
            return fallbackModelId;
        }
        if (!isBlank(rawModel.modelId) && ResourceUtil.isValidResourceLocation(rawModel.modelId)) {
            return rawModel.modelId;
        }
        String hash = Md5Utils.md5Hex(fallbackModelId.getBytes(StandardCharsets.UTF_8)).toLowerCase(Locale.US);
        String modelId = "ysm_" + hash.substring(0, 12);
        rawModel.modelId = modelId;
        return modelId;
    }

    @Nullable
    private static RawYsmModel tryLoadModernModel(File file, @Nullable IModelAccess access, Type type) throws IOException {
        if (access != null && access.exists("ysm.json")) {
            if (type == Type.YSM) {
                try (YSMFolderDeserializer deserializer = new YSMFolderDeserializer(snapshotAccess(access))) {
                    return deserializer.deserialize();
                }
            }
            try (YSMFolderDeserializer deserializer = new YSMFolderDeserializer(file.toPath())) {
                return deserializer.deserialize();
            }
        }

        if (type != Type.YSM) {
            return null;
        }

        byte[] rawFile = FileUtils.readFileToByteArray(file);
        try {
            byte[] decrypted = YsmCrypt.decryptYsmFile(rawFile);
            try (YSMBinaryDeserializer deserializer = new YSMBinaryDeserializer(decrypted)) {
                RawYsmModel rawModel = deserializer.deserializeKeepOpen();
                deserializer.parseYSMFooter(rawModel);
                return rawModel;
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    private static RawYsmModel tryLoadFolderModel(IModelAccess access, Type type) {
        if (type == Type.YSM) {
            return null;
        }
        try (YSMFolderDeserializer deserializer = new YSMFolderDeserializer(snapshotAccess(access))) {
            return deserializer.deserialize();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static Map<String, byte[]> snapshotAccess(IModelAccess access) throws IOException {
        Map<String, byte[]> files = Maps.newHashMap();
        for (String path : access.listFiles("")) {
            byte[] data = access.readFile(path);
            if (data != null) {
                files.put(path, data);
            }
        }
        return files;
    }

    /**
     * @return 若为 null 则代表模型无效。
     */
    @Nullable
    public static ModelData getModelData(IModelAccess access, String modelId, boolean isAuth, Type type) throws IOException {
        Map<String, byte[]> model = Maps.newHashMap();
        OpenYsmDescriptor descriptor = readOpenYsmDescriptor(access);
        ExtraInfo legacyInfo = readLegacyInfo(access);
        for (String modelName : MODEL_NAMES) {
            byte[] data = access.readFile(getModelFileName(modelName));
            if (data == null || data.length == 0) {
                if (isModelNameNecessary(modelName)) return null;
                continue;
            }
            String json = new String(data, StandardCharsets.UTF_8);
            RawGeoModel rawModel = Converter.fromJsonString(json);
            if (MAIN_MODEL_NAME.equals(modelName)) {
                applyOpenYsmDescriptor(rawModel, descriptor, legacyInfo);
            }
            model.put(modelName, ObjectStreamUtil.toByteArray(rawModel));
        }

        ExtraInfo mergedInfo = mergeExtraInfo(descriptor, legacyInfo, null);
        if (mergedInfo != null) {
            model.put(INFO_NAME, ObjectStreamUtil.toByteArray(mergedInfo));
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
                File defaultFile = getDefaultAnimFile(animName);
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

    @Nullable
    private static ExtraInfo readLegacyInfo(IModelAccess access) throws IOException {
        byte[] infoData = access.readFile(INFO_FILE_NAME);
        if (infoData == null || infoData.length == 0) {
            return null;
        }

        String infoJson = new String(infoData, StandardCharsets.UTF_8);
        return YesSteveModel.GSON.fromJson(infoJson, ExtraInfo.class);
    }

    @Nullable
    private static OpenYsmDescriptor readOpenYsmDescriptor(IModelAccess access) throws IOException {
        byte[] ysmJsonData = access.readFile("ysm.json");
        if (ysmJsonData == null || ysmJsonData.length == 0) {
            return null;
        }

        JsonObject root = new JsonParser().parse(new String(ysmJsonData, StandardCharsets.UTF_8)).getAsJsonObject();
        OpenYsmDescriptor descriptor = new OpenYsmDescriptor();

        if (root.has("metadata") && root.get("metadata").isJsonObject()) {
            JsonObject metadata = root.getAsJsonObject("metadata");
            descriptor.name = getJsonString(metadata, "name");
            descriptor.tips = getJsonString(metadata, "tips");

            if (metadata.has("license")) {
                JsonElement license = metadata.get("license");
                if (license.isJsonObject()) {
                    JsonObject licenseObject = license.getAsJsonObject();
                    String type = getJsonString(licenseObject, "type");
                    String description = getJsonString(licenseObject, "desc");
                    descriptor.license = firstNonBlank(description, type);
                } else if (license.isJsonPrimitive()) {
                    descriptor.license = license.getAsString();
                }
            }

            if (metadata.has("authors") && metadata.get("authors").isJsonArray()) {
                List<String> authors = new ArrayList<>();
                for (JsonElement element : metadata.getAsJsonArray("authors")) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject author = element.getAsJsonObject();
                    String authorName = getJsonString(author, "name");
                    String role = getJsonString(author, "role");
                    if (isBlank(authorName)) {
                        continue;
                    }
                    authors.add(isBlank(role) ? authorName : authorName + " (" + role + ")");
                }
                if (!authors.isEmpty()) {
                    descriptor.authors = authors.toArray(new String[0]);
                }
            }
        }

        if (root.has("properties") && root.get("properties").isJsonObject()) {
            JsonObject properties = root.getAsJsonObject("properties");
            descriptor.widthScale = getJsonDouble(properties, "width_scale");
            descriptor.heightScale = getJsonDouble(properties, "height_scale");
            descriptor.free = getJsonBoolean(properties, "free");
            descriptor.previewAnimation = getJsonString(properties, "preview_animation");
            descriptor.disablePreviewRotation = getJsonBoolean(properties, "disable_preview_rotation");
            descriptor.guiForeground = getJsonString(properties, "gui_foreground");
            descriptor.guiBackground = getJsonString(properties, "gui_background");

            if (properties.has("extra_animation") && properties.get("extra_animation").isJsonObject()) {
                descriptor.extraAnimationNames = mapExtraAnimationNames(properties.getAsJsonObject("extra_animation"));
            }
        }

        return descriptor.isEmpty() ? null : descriptor;
    }

    private static void applyOpenYsmDescriptor(RawGeoModel rawModel, @Nullable OpenYsmDescriptor descriptor, @Nullable ExtraInfo legacyInfo) {
        if (rawModel == null || rawModel.getMinecraftGeometry() == null || rawModel.getMinecraftGeometry().length == 0) {
            return;
        }

        MinecraftGeometry geometry = rawModel.getMinecraftGeometry()[0];
        ModelProperties properties = geometry.getProperties();
        if (properties == null) {
            properties = new ModelProperties();
            geometry.setProperties(properties);
        }

        ExtraInfo embeddedInfo = properties.getExtraInfo();
        ExtraInfo mergedInfo = mergeExtraInfo(descriptor, legacyInfo, embeddedInfo);
        if (mergedInfo != null) {
            properties.setExtraInfo(mergedInfo);
        }

        if (descriptor != null) {
            if (descriptor.widthScale != null) {
                properties.setWidthScale(descriptor.widthScale);
            }
            if (descriptor.heightScale != null) {
                properties.setHeightScale(descriptor.heightScale);
            }
        }
    }

    @Nullable
    private static ExtraInfo mergeExtraInfo(@Nullable OpenYsmDescriptor descriptor, @Nullable ExtraInfo legacyInfo, @Nullable ExtraInfo embeddedInfo) {
        String name = firstNonBlank(descriptor != null ? descriptor.name : null,
                legacyInfo != null ? legacyInfo.getName() : null,
                embeddedInfo != null ? embeddedInfo.getName() : null);
        String tips = firstNonBlank(descriptor != null ? descriptor.tips : null,
                legacyInfo != null ? legacyInfo.getTips() : null,
                embeddedInfo != null ? embeddedInfo.getTips() : null);
        String[] extraAnimationNames = firstNonEmptyArray(descriptor != null ? descriptor.extraAnimationNames : null,
                legacyInfo != null ? legacyInfo.getExtraAnimationNames() : null,
                embeddedInfo != null ? embeddedInfo.getExtraAnimationNames() : null);
        String[] authors = firstNonEmptyArray(descriptor != null ? descriptor.authors : null,
                legacyInfo != null ? legacyInfo.getAuthors() : null,
                embeddedInfo != null ? embeddedInfo.getAuthors() : null);
        String license = firstNonBlank(descriptor != null ? descriptor.license : null,
                legacyInfo != null ? legacyInfo.getLicense() : null,
                embeddedInfo != null ? embeddedInfo.getLicense() : null);
        Boolean free = firstNonNull(descriptor != null ? descriptor.free : null,
                legacyInfo != null ? legacyInfo.getFree() : null,
                embeddedInfo != null ? embeddedInfo.getFree() : null);
        String previewAnimation = firstNonBlank(descriptor != null ? descriptor.previewAnimation : null,
                legacyInfo != null ? legacyInfo.getPreviewAnimation() : null,
                embeddedInfo != null ? embeddedInfo.getPreviewAnimation() : null);
        Boolean disablePreviewRotation = firstNonNull(descriptor != null ? descriptor.disablePreviewRotation : null,
                legacyInfo != null ? legacyInfo.getDisablePreviewRotation() : null,
                embeddedInfo != null ? embeddedInfo.getDisablePreviewRotation() : null);
        String guiForeground = firstNonBlank(descriptor != null ? descriptor.guiForeground : null,
                legacyInfo != null ? legacyInfo.getGuiForeground() : null,
                embeddedInfo != null ? embeddedInfo.getGuiForeground() : null);
        String guiBackground = firstNonBlank(descriptor != null ? descriptor.guiBackground : null,
                legacyInfo != null ? legacyInfo.getGuiBackground() : null,
                embeddedInfo != null ? embeddedInfo.getGuiBackground() : null);

        boolean hasAnyValue = !isBlank(name)
                || !isBlank(tips)
                || hasArrayValue(extraAnimationNames)
                || hasArrayValue(authors)
                || !isBlank(license)
                || free != null
                || !isBlank(previewAnimation)
                || disablePreviewRotation != null
                || !isBlank(guiForeground)
                || !isBlank(guiBackground);
        if (!hasAnyValue) {
            return null;
        }

        ExtraInfo merged = new ExtraInfo();
        if (!isBlank(name)) {
            merged.setName(name);
        }
        if (!isBlank(tips)) {
            merged.setTips(tips);
        }
        if (hasArrayValue(extraAnimationNames)) {
            merged.setExtraAnimationNames(extraAnimationNames);
        }
        if (hasArrayValue(authors)) {
            merged.setAuthors(authors);
        }
        if (!isBlank(license)) {
            merged.setLicense(license);
        }
        if (free != null) {
            merged.setFree(free);
        }
        if (!isBlank(previewAnimation)) {
            merged.setPreviewAnimation(previewAnimation);
        }
        if (disablePreviewRotation != null) {
            merged.setDisablePreviewRotation(disablePreviewRotation);
        }
        if (!isBlank(guiForeground)) {
            merged.setGuiForeground(guiForeground);
        }
        if (!isBlank(guiBackground)) {
            merged.setGuiBackground(guiBackground);
        }
        return merged;
    }

    @Nullable
    private static String[] mapExtraAnimationNames(JsonObject extraAnimationObject) {
        String[] names = new String[8];
        int nextIndex = 0;
        for (Map.Entry<String, JsonElement> entry : extraAnimationObject.entrySet()) {
            if (!entry.getValue().isJsonPrimitive()) {
                continue;
            }

            String displayName = entry.getValue().getAsString();
            if (isBlank(displayName)) {
                continue;
            }

            Integer index = parseExtraAnimationIndex(entry.getKey());
            if (index != null && index >= 0 && index < names.length && isBlank(names[index])) {
                names[index] = displayName;
                continue;
            }

            while (nextIndex < names.length && !isBlank(names[nextIndex])) {
                nextIndex++;
            }
            if (nextIndex >= names.length) {
                break;
            }
            names[nextIndex] = displayName;
        }
        return hasArrayValue(names) ? names : null;
    }

    @Nullable
    private static Integer parseExtraAnimationIndex(String key) {
        if (isBlank(key)) {
            return null;
        }
        int end = key.length() - 1;
        while (end >= 0 && Character.isDigit(key.charAt(end))) {
            end--;
        }
        if (end == key.length() - 1) {
            return null;
        }
        try {
            return Integer.parseInt(key.substring(end + 1));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Nullable
    private static String getJsonString(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            return null;
        }
        return object.get(key).getAsString();
    }

    @Nullable
    private static Double getJsonDouble(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            return null;
        }
        return object.get(key).getAsDouble();
    }

    @Nullable
    private static Boolean getJsonBoolean(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            return null;
        }
        return object.get(key).getAsBoolean();
    }

    private static boolean hasArrayValue(@Nullable String[] values) {
        if (values == null || values.length == 0) {
            return false;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static String[] firstNonEmptyArray(String[]... values) {
        for (String[] value : values) {
            if (hasArrayValue(value)) {
                return value;
            }
        }
        return null;
    }

    @SafeVarargs
    @Nullable
    private static <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    @Nullable
    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class OpenYsmDescriptor {
        private String name;
        private String tips;
        private String[] extraAnimationNames;
        private String[] authors;
        private String license;
        private Boolean free;
        private String previewAnimation;
        private Boolean disablePreviewRotation;
        private String guiForeground;
        private String guiBackground;
        private Double widthScale;
        private Double heightScale;

        private boolean isEmpty() {
            return isBlank(name)
                    && isBlank(tips)
                    && !hasArrayValue(extraAnimationNames)
                    && !hasArrayValue(authors)
                    && isBlank(license)
                    && free == null
                    && isBlank(previewAnimation)
                    && disablePreviewRotation == null
                    && isBlank(guiForeground)
                    && isBlank(guiBackground)
                    && widthScale == null
                    && heightScale == null;
        }
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

    @Nonnull
    private static ServerModelInfo cacheModernModel(RawYsmModel rawModel, boolean isAuth) throws IOException {
        if (rawModel.properties == null || isBlank(rawModel.properties.sha256)) {
            throw new IOException("Modern model is missing properties.sha256");
        }

        byte[] cacheKey = EncryptTools.deriveModernCacheKey(EncryptTools.writePassword());
        long[] hashes = YsmCrypt.calculateModelHashes(rawModel.properties.sha256, cacheKey);

        byte[] dataBytes;
        try (YSMByteBuf serialized = YSMBinarySerializer.serialize(rawModel, 32, true)) {
            ByteBuf raw = serialized.getRawBuf();
            if (raw.hasArray()) {
                int offset = raw.arrayOffset() + raw.readerIndex();
                int length = raw.readableBytes();
                dataBytes = YsmCrypt.encryptServerCache(raw.array(), offset, length, cacheKey, hashes[0], hashes[1]);
            } else {
                dataBytes = YsmCrypt.encryptServerCache(serialized.toArray(), cacheKey, hashes[0], hashes[1]);
            }
        } catch (Exception e) {
            throw new IOException("Failed to build modern server cache", e);
        }

        ServerModelInfo info = buildModernModelInfo(rawModel, isAuth);
        info.setMd5(Md5Utils.md5Hex(dataBytes).toUpperCase(Locale.US));
        FileUtils.writeByteArrayToFile(CACHE_SERVER.resolve(info.getMd5()).toFile(), dataBytes);
        return info;
    }

    private static ServerModelInfo buildModernModelInfo(RawYsmModel raw, boolean isAuth) {
        RawYsmModel.RawMetadata rawMetadata = raw.metadata;
        List<AuthorInfo> authors = new ArrayList<>();
        for (RawYsmModel.RawMetadata.Author author : rawMetadata.authors) {
            authors.add(new AuthorInfo(author.name, author.role, orderedStringMap(author.contacts), author.comment));
        }

        Metadata metadata = new Metadata(rawMetadata.name, rawMetadata.tips,
                new StringPair(rawMetadata.licenseType, rawMetadata.licenseDescription),
                authors.toArray(new AuthorInfo[0]), orderedStringMap(rawMetadata.links));

        RawYsmModel.RawProperties rawProperties = raw.properties;
        List<StringMapPair> classifyList = new ArrayList<>();
        for (RawYsmModel.ExtraAnimationClassify classify : rawProperties.extraAnimationClassifies) {
            classifyList.add(new StringMapPair(classify.id, orderedStringMap(classify.extras)));
        }

        List<ExtraAnimationButtons> buttonsList = new ArrayList<>();
        for (RawYsmModel.ExtraAnimationButton rawButton : rawProperties.extraAnimationButtons) {
            List<AbstractConfig> metaList = new ArrayList<>();
            for (RawYsmModel.ConfigForm form : rawButton.forms) {
                if ("checkbox".equals(form.type)) {
                    metaList.add(new CheckboxConfig(form.title, form.description, form.defaultValue));
                } else if ("radio".equals(form.type)) {
                    metaList.add(new RadioConfig(form.title, form.description, form.defaultValue, orderedStringMap(form.labels)));
                } else if ("range".equals(form.type)) {
                    metaList.add(new RangeConfig(form.title, form.description, form.defaultValue, form.step, form.min, form.max));
                }
            }
            buttonsList.add(new ExtraAnimationButtons(rawButton.id, rawButton.name, rawButton.description, metaList.toArray(new AbstractConfig[0])));
        }

        com.elfmcys.yesstevemodel.resource.models.ModelProperties properties =
                new com.elfmcys.yesstevemodel.resource.models.ModelProperties(rawProperties.heightScale, rawProperties.widthScale,
                        rawProperties.defaultTexture, rawProperties.previewAnimation, orderedStringMap(rawProperties.extraAnimations),
                        buttonsList.toArray(new ExtraAnimationButtons[0]), classifyList.toArray(new StringMapPair[0]),
                        rawProperties.isFree, rawProperties.renderLayersFirst, rawProperties.disablePreviewRotation);

        int bones = 0;
        int cubes = 0;
        int faces = 0;
        if (raw.mainEntity.mainModel != null) {
            bones = raw.mainEntity.mainModel.bones.size();
            for (RawYsmModel.RawBone bone : raw.mainEntity.mainModel.bones) {
                cubes += bone.cubes.size();
                for (RawYsmModel.RawCube cube : bone.cubes) {
                    faces += cube.faces.size();
                }
            }
        }

        MainModelInfo stats = new MainModelInfo(bones, cubes, faces);
        RawYsmModel.RawFooter footer = raw.footer;
        return new ServerModelInfo(metadata, properties, stats, footer.version,
                rawProperties.sha256 != null ? rawProperties.sha256 : "", footer.extra, footer.time, footer.rand, isAuth);
    }

    private static OrderedStringMap<String, String> orderedStringMap(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return new OrderedStringMap<String, String>(new String[0], new String[0]);
        }
        String[] keys = source.keySet().toArray(new String[0]);
        String[] values = source.values().toArray(new String[0]);
        return new OrderedStringMap<String, String>(keys, values);
    }
}
