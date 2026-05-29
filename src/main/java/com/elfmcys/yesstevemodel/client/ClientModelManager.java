package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.model.MainModelData;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.model.ModelAssemblyFactory;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle;
import com.elfmcys.yesstevemodel.client.model.ProjectileModelBundle;
import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets;
import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.FirstPersonArmAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.FormatVersion;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree.RawGeometryTree;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.GeoBuilder;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.json.JsonAnimationUtils;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.model.format.FormatManager;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.model.format.Type;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SyncModelFiles;
import com.elfmcys.yesstevemodel.resource.YSMClientMapper;
import com.elfmcys.yesstevemodel.resource.YSMFolderDeserializer;
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ThreadTools;
import com.elfmcys.yesstevemodel.util.UuidUtils;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClientModelManager {
    public static Map<ResourceLocation, List<ResourceLocation>> MODELS = Maps.newHashMap();
    private static volatile Map<ResourceLocation, ModelAssembly> MODERN_MODELS = Collections.emptyMap();
    public static Map<ResourceLocation, Pair<Double, Double>> SCALE_INFO = Maps.newHashMap();
    public static Map<ResourceLocation, List<String>> METADATA = Maps.newHashMap();
    public static Map<ResourceLocation, ExtraInfo> EXTRA_INFO = Maps.newHashMap();
    public static AnimationFile DEFAULT_ANIMATION_FILE = new AnimationFile();
    public static List<String> CACHE_MD5 = Lists.newArrayList();
    public static List<String> AUTH_MODELS = Lists.newArrayList();
    public static byte[] PASSWORD;

    public static CacheExportResult exportAllCachedModels() {
        if (PASSWORD == null) {
            return CacheExportResult.failure("commands.yes_steve_model.client.cache.dump.password_missing");
        }

        if (Minecraft.getMinecraft().player == null) {
            return CacheExportResult.failure("commands.yes_steve_model.client.cache.dump.no_player");
        }

        Collection<File> files = FileUtils.listFiles(ServerModelManager.CACHE_CLIENT.toFile(), FileFileFilter.FILE, null);
        if (files.isEmpty()) {
            return CacheExportResult.failure("commands.yes_steve_model.client.cache.dump.empty");
        }

        UUID uuid = Minecraft.getMinecraft().player.getUniqueID();
        Path exportRoot = ServerModelManager.EXPORT.resolve("cache");
        int successCount = 0;
        for (File file : files) {
            try {
                byte[] fileBytes = FileUtils.readFileToByteArray(file);
                ModelData data = EncryptTools.decryptModel(UuidUtils.asBytes(uuid), PASSWORD, fileBytes);
                if (data == null) {
                    continue;
                }
                exportCachedModel(data, exportRoot.resolve(data.getModelId()));
                successCount++;
            } catch (Exception e) {
                YesSteveModel.LOGGER.warn("Failed to export cached model {}", file.getName(), e);
            }
        }

        if (successCount == 0) {
            return CacheExportResult.failure("commands.yes_steve_model.client.cache.dump.failure");
        }

        return CacheExportResult.success(successCount, exportRoot.toString().replace('\\', '/'));
    }

    public static void registerAll(ModelData data) {
        ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, data.getModelId());
        if (data.isAuth()) {
            AUTH_MODELS.add(data.getModelId());
        }
        ClientModelManager.registerGeo(modelId, data.getModel());
        ClientModelManager.registerAnimations(ModelIdUtil.getMainId(modelId), data.getAnimation());
        ClientModelManager.registerTexture(modelId, data.getTexture());
        registerLegacyAssembly(modelId, data);
    }

    private static void registerLegacyAssembly(ResourceLocation modelId, ModelData data) {
        ResourceLocation mainId = ModelIdUtil.getMainId(modelId);
        GeoModel mainModel = GeckoLibCache.getInstance().getGeoModels().get(mainId);
        if (mainModel == null) {
            return;
        }

        GeoModel armModel = GeckoLibCache.getInstance().getGeoModels().get(ModelIdUtil.getArmId(modelId));
        if (armModel == null) {
            armModel = mainModel;
        }

        AnimationFile animationFile = GeckoLibCache.getInstance().getAnimations().get(mainId);
        Object2ReferenceOpenHashMap<String, Animation> mainAnimations = new Object2ReferenceOpenHashMap<>();
        if (animationFile != null) {
            mainAnimations.putAll(animationFile.animations());
        }
        if ("default".equals(modelId.getPath())) {
            mainAnimations.values().forEach(animation -> animation.isFromPrimaryAssembly = true);
        }
        Object2ReferenceOpenHashMap<String, Animation> armAnimations = new Object2ReferenceOpenHashMap<>(mainAnimations);
        Object2ReferenceOpenHashMap<String, AnimationController> controllers = new Object2ReferenceOpenHashMap<>();

        OrderedStringMap<String, OuterFileTexture> textureMap = buildLegacyTextureMap(modelId, data);
        List<AbstractTexture> textures = new java.util.ArrayList<>(new LinkedHashSet<>(textureMap.values()));
        String defaultTexture = data.getInfo().getTexture().filter(textureMap::containsKey)
                .orElseGet(() -> textureMap.isEmpty() ? "" : textureMap.getKeyAt(0));

        ConditionManager conditionManager = new ConditionManager();
        mainAnimations.keySet().forEach(conditionManager::addTest);

        ArmorConditions modelProcessor = new ArmorConditions();
        armAnimations.keySet().forEach(modelProcessor::addCondition);

        ModelResourceBundle resourceBundle = new ModelResourceBundle(Collections.emptyMap(), new Object2ReferenceOpenHashMap<>(), new Object2ReferenceOpenHashMap<>(), Collections.emptyMap());
        PlayerModelBundle seedBundle = new PlayerModelBundle(mainModel, armModel, mainAnimations, armAnimations, controllers, textureMap, defaultTexture, textureMap.get(defaultTexture), conditionManager, modelProcessor, null, null, null);
        var playerInstaller = PlayerAnimationController.buildControllers(seedBundle, resourceBundle);
        var armInstaller = FirstPersonArmAnimationController.buildControllers(seedBundle, resourceBundle);
        PlayerModelBundle bundle = new PlayerModelBundle(mainModel, armModel, mainAnimations, armAnimations, controllers, textureMap, defaultTexture, textureMap.get(defaultTexture), conditionManager, modelProcessor, playerInstaller, armInstaller, null);
        ModelAssembly assembly = new ModelAssembly(bundle, Collections.emptyMap(), Collections.emptyMap(), resourceBundle, data.getInfo(), new ModelDisplayAssets("", data.isAuth(), Collections.emptyMap(), Collections.emptyMap()), textures);

        Map<ResourceLocation, ModelAssembly> copy = new LinkedHashMap<>(MODERN_MODELS);
        copy.put(modelId, assembly);
        MODERN_MODELS = Collections.unmodifiableMap(copy);
        YesSteveModel.LOGGER.info("Registered legacy compatibility assembly {} with {} animations", modelId, mainAnimations.size());
    }

    private static OrderedStringMap<String, OuterFileTexture> buildLegacyTextureMap(ResourceLocation modelId, ModelData data) {
        List<String> names = new java.util.ArrayList<>();
        List<OuterFileTexture> textures = new java.util.ArrayList<>();
        ResourceLocation infoId = ModelIdUtil.getInfoId(modelId);
        data.getTexture().forEach((name, bytes) -> {
            if (isModelTexture(infoId, name)) {
                names.add(name);
                textures.add(new OuterFileTexture(bytes));
            }
        });
        return new OrderedStringMap<>(names.toArray(new String[0]), textures.toArray(new OuterFileTexture[0]));
    }

    public static void registerAll(String modelName, ClientModelInfo data) {
        ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, modelName);
        ServerModelInfo info = data.getInfo();
        if (info.isNeedAuth()) {
            AUTH_MODELS.add(modelName);
        }

        registerModernAssembly(modelId, data, info.isNeedAuth());
        addModernInfo(modelId, info);
        registerGeo(modelId, data.getMainModelData(), info);
        registerModernAnimations(ModelIdUtil.getMainId(modelId), data.getMainModelData().getAnimations());
        registerTexture(modelId, data.getMainModelData().getTextureMap(), true);
        registerTexture(modelId, data.getGuiTextures(), false);
        List<ResourceLocation> textures = MODELS.get(modelId);
        YesSteveModel.LOGGER.info("Registered modern client model {} with {} textures", modelName, textures == null ? 0 : textures.size());
    }

    private static void registerModernAssembly(ResourceLocation modelId, ClientModelInfo data, boolean auth) {
        ModelAssembly assembly = ModelAssemblyFactory.buildAssembly(data, "default".equals(modelId.getPath()), auth);
        registerModernProjectileAssets(modelId, assembly);
        Map<ResourceLocation, ModelAssembly> copy = new LinkedHashMap<>(MODERN_MODELS);
        copy.put(modelId, assembly);
        MODERN_MODELS = Collections.unmodifiableMap(copy);
        YesSteveModel.LOGGER.info("Registered modern model assembly {} with {} textures", modelId, assembly.getTextures().size());
    }

    public static Map<ResourceLocation, ModelAssembly> getModernModels() {
        return MODERN_MODELS;
    }

    public static ModelAssembly getModernModel(ResourceLocation modelId) {
        return MODERN_MODELS.get(modelId);
    }

    public static boolean hasModernProjectileModel(ResourceLocation modelId, ResourceLocation projectileId) {
        return getModernProjectileModel(modelId, projectileId) != null;
    }

    @Nullable
    public static ProjectileModelBundle getModernProjectileModel(ResourceLocation modelId, ResourceLocation projectileId) {
        ModelAssembly assembly = getModernModel(modelId);
        return assembly != null ? assembly.getProjectileModels().get(projectileId) : null;
    }

    public static ResourceLocation getModernProjectileModelId(ResourceLocation modelId, ResourceLocation projectileId) {
        return ModelIdUtil.getSubModelId(modelId, "projectile/" + sanitizeResourceId(projectileId));
    }

    public static ResourceLocation getModernProjectileTextureId(ResourceLocation modelId, ResourceLocation projectileId) {
        return ModelIdUtil.getSubModelId(modelId, "projectile/" + sanitizeResourceId(projectileId) + "/texture");
    }

    private static void registerModernProjectileAssets(ResourceLocation modelId, ModelAssembly assembly) {
        Map<ResourceLocation, GeoModel> geoModels = GeckoLibCache.getInstance().getGeoModels();
        Map<ResourceLocation, AnimationFile> animations = GeckoLibCache.getInstance().getAnimations();
        assembly.getProjectileModels().forEach((projectileId, bundle) -> {
            ResourceLocation assetId = getModernProjectileModelId(modelId, projectileId);
            if (bundle.getModel() != null) {
                geoModels.put(assetId, bundle.getModel());
            }
            animations.put(assetId, new AnimationFile(bundle.getAnimations()));
            if (bundle.getTexture() != null) {
                registerTexture(getModernProjectileTextureId(modelId, projectileId), bundle.getTexture());
            }
        });
    }

    private static String sanitizeResourceId(ResourceLocation id) {
        return id.getNamespace() + "_" + id.getPath().replace('/', '_');
    }

    public static void registerGeo(ResourceLocation modelId, Map<String, byte[]> modelData) {
        for (Map.Entry<String, byte[]> entry : modelData.entrySet()) {
            String partName = entry.getKey();
            if (FormatManager.INFO_NAME.equals(partName)) continue;
            registerGeo(modelId, partName, entry.getValue());
        }
        byte[] infoData = modelData.get(FormatManager.INFO_NAME);
        if (infoData != null && ObjectStreamUtil.toObject(infoData) instanceof ExtraInfo extraInfo) {
            addExtraInfo(modelId, extraInfo);
        }
    }

    public static void registerGeo(ResourceLocation modelId, MainModelData modelData, ServerModelInfo info) {
        Map<ResourceLocation, GeoModel> geoModels = GeckoLibCache.getInstance().getGeoModels();
        List<GeoModel> models = modelData.getModels();
        if (!models.isEmpty()) {
            geoModels.put(ModelIdUtil.getMainId(modelId), models.get(0));
        }
        if (models.size() > 1) {
            geoModels.put(ModelIdUtil.getArmId(modelId), models.get(1));
        } else if (!models.isEmpty()) {
            geoModels.put(ModelIdUtil.getArmId(modelId), models.get(0));
        }

        if (info.getModelProperties() != null) {
            SCALE_INFO.put(ModelIdUtil.getMainId(modelId), Pair.of((double) info.getModelProperties().getHeightScale(), (double) info.getModelProperties().getWidthScale()));
        }
    }

    private static void registerGeo(ResourceLocation modelId, String partName, byte[] partData) {
        Map<ResourceLocation, GeoModel> geoModels = GeckoLibCache.getInstance().getGeoModels();
        try {
            Object obj = ObjectStreamUtil.toObject(partData);
            if (obj instanceof RawGeoModel rawModel) {
                if (rawModel.getFormatVersion() == FormatVersion.NEW) {
                    RawGeometryTree rawGeometryTree = RawGeometryTree.parseHierarchy(rawModel);
                    ResourceLocation partId = ModelIdUtil.getSubModelId(modelId, partName);
                    GeoModel geoModel = GeoBuilder.getGeoBuilder().constructGeoModel(rawGeometryTree);
                    if (FormatManager.MAIN_MODEL_NAME.equals(partName)) {
                        SCALE_INFO.put(partId, Pair.of(rawGeometryTree.properties.getHeightScale(), rawGeometryTree.properties.getWidthScale()));
                        addExtraInfo(modelId, rawGeometryTree.properties.getExtraInfo());
                    }
                    geoModels.put(partId, geoModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerTexture(ResourceLocation modelId, Map<String, byte[]> mapData) {
        List<ResourceLocation> textures = Lists.newArrayList();
        for (String name : mapData.keySet()) {
            if (isModelTexture(ModelIdUtil.getInfoId(modelId), name)) {
                ResourceLocation textureId = ModelIdUtil.getSubModelId(modelId, name);
                textures.add(textureId);
            }
        }
        MODELS.put(modelId, textures);
        for (Map.Entry<String, byte[]> entry : mapData.entrySet()) {
            ResourceLocation textureId = ModelIdUtil.getSubModelId(modelId, entry.getKey());
            registerTexture(textureId, entry.getValue());
        }
    }

    private static boolean isModelTexture(ResourceLocation infoId, String name) {
        if (name.equals(FormatManager.ARROW_TEXTURE_FILE_NAME)) return false;
        final ExtraInfo extraInfo = EXTRA_INFO.get(infoId);
        return extraInfo == null || (!name.equals(extraInfo.getGuiBackground()) && !name.equals(extraInfo.getGuiForeground()));
    }

    private static void registerTexture(ResourceLocation textureId, byte[] data) {
        // 确保主线程上传
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            mc.getTextureManager().loadTexture(textureId, new OuterFileTexture(data));
        });
    }

    private static void registerTexture(ResourceLocation modelId, Map<String, OuterFileTexture> mapData, boolean includeModelTextures) {
        if (mapData == null || mapData.isEmpty()) {
            return;
        }

        if (includeModelTextures) {
            List<ResourceLocation> textures = Lists.newArrayList();
            for (String name : mapData.keySet()) {
                if (isModelTexture(ModelIdUtil.getInfoId(modelId), name)) {
                    textures.add(ModelIdUtil.getSubModelId(modelId, name));
                }
            }
            MODELS.put(modelId, textures);
        }

        for (Map.Entry<String, OuterFileTexture> entry : mapData.entrySet()) {
            registerTexture(ModelIdUtil.getSubModelId(modelId, entry.getKey()), entry.getValue());
        }
    }

    private static void registerTexture(ResourceLocation textureId, OuterFileTexture texture) {
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> mc.getTextureManager().loadTexture(textureId, texture));
    }

    private static void registerAnimations(ResourceLocation mainId, Map<String, byte[]> mapData) {
        Map<ResourceLocation, AnimationFile> animations = GeckoLibCache.getInstance().getAnimations();

        if (mapData.containsKey("arrow")) {
            byte[] arrowBytes = mapData.get("arrow");
            AnimationFile arrowsAnimationFile = getAnimationFile(new String(arrowBytes, StandardCharsets.UTF_8));
            animations.put(ModelIdUtil.getArrowId(ModelIdUtil.getModelIdFromMainId(mainId)), arrowsAnimationFile);
            mapData.remove("arrow");
        }

        AnimationFile main = new AnimationFile();
        mapData.forEach((name, bytes) -> {
            AnimationFile other = getAnimationFile(new String(bytes, StandardCharsets.UTF_8));
            mergeAnimationFile(main, other);
        });
        // 从默认补全缺失的动画
        DEFAULT_ANIMATION_FILE.animations().forEach((name, action) -> {
            if (!main.animations().containsKey(name)) {
                main.putAnimation(name, action);
            }
        });
        animations.put(mainId, main);
    }

    private static void registerModernAnimations(ResourceLocation mainId, Map<String, AnimationFile> mapData) {
        Map<ResourceLocation, AnimationFile> animations = GeckoLibCache.getInstance().getAnimations();

        AnimationFile arrowAnimation = mapData.get("arrow");
        if (arrowAnimation != null) {
            animations.put(ModelIdUtil.getArrowId(ModelIdUtil.getModelIdFromMainId(mainId)), arrowAnimation);
        }

        AnimationFile main = new AnimationFile();
        mapData.forEach((name, animationFile) -> {
            if (!"arrow".equals(name)) {
                mergeAnimationFile(main, animationFile);
            }
        });
        DEFAULT_ANIMATION_FILE.animations().forEach((name, action) -> {
            if (!main.animations().containsKey(name)) {
                main.putAnimation(name, action);
            }
        });
        animations.put(mainId, main);
    }

    private static AnimationFile getAnimationFile(String file) {
        AnimationFile animationFile = new AnimationFile();
        MolangParser parser = GeckoLibCache.getInstance().parser;
        JsonObject jsonObject = JsonUtils.fromJson(YesSteveModel.GSON, file, JsonObject.class, false);
        if (jsonObject != null) {
            for (Map.Entry<String, JsonElement> entry : JsonAnimationUtils.getAnimations(jsonObject)) {
                String animationName = entry.getKey();
                Animation animation;
                try {
                    animation = JsonAnimationUtils.deserializeJsonToAnimation(JsonAnimationUtils.getAnimation(jsonObject, animationName), parser);
                    animationFile.putAnimation(animationName, animation);
                } catch (JsonException e) {
                    e.printStackTrace();
                }
            }
        }
        return animationFile;
    }

    /**
     * @return main animation
     */
    private static AnimationFile mergeAnimationFile(AnimationFile main, AnimationFile other) {
        other.animations().forEach(main::putAnimation);
        return main;
    }

    public static void loadDefaultModel() {
        try {
            Path defaultPath = ServerModelManager.BUILTIN.resolve("default");
            try (YSMFolderDeserializer deserializer = new YSMFolderDeserializer(defaultPath)) {
                RawYsmModel rawModel = deserializer.deserialize();
                ClientModelInfo parsedBundle = YSMClientMapper.buildParsedBundle(rawModel, "default");
                parsedBundle.getMainModelData().getAnimations().forEach((name, animationFile) -> {
                    if (!"arrow".equals(name)) {
                        mergeAnimationFile(DEFAULT_ANIMATION_FILE, animationFile);
                    }
                });
                DEFAULT_ANIMATION_FILE.animations().values().forEach(animation -> animation.isFromPrimaryAssembly = true);
                ClientModelManager.registerAll("default", parsedBundle);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendSyncModelMessage() {
        MODELS.clear();
        MODERN_MODELS = Collections.emptyMap();
        CACHE_MD5.clear();
        AUTH_MODELS.clear();
        SCALE_INFO.clear();
        METADATA.clear();
        EXTRA_INFO.clear();
        String[] md5Info = getMd5Info();
        SyncModelFiles syncModelFiles = new SyncModelFiles(md5Info);
        ThreadTools.THREAD_POOL.submit(() -> {
            try {
                while (Minecraft.getMinecraft().getConnection() == null) {
                    Thread.sleep(500);
                }
                NetworkHandler.CHANNEL.sendToServer(syncModelFiles);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static String[] getMd5Info() {
        Collection<File> files = FileUtils.listFiles(ServerModelManager.CACHE_CLIENT.toFile(), FileFileFilter.FILE, null);
        String[] output = new String[files.size()];
        int i = 0;
        for (File file : files) {
            output[i] = file.getName();
            i++;
        }
        return output;
    }

    private static byte[] getBytes(Path root, String fileName) throws IOException {
        return FileUtils.readFileToByteArray(root.resolve(fileName).toFile());
    }

    private static void exportCachedModel(ModelData data, Path outputDir) throws IOException {
        FileUtils.deleteDirectory(outputDir.toFile());
        Files.createDirectories(outputDir);

        for (Map.Entry<String, byte[]> entry : data.getModel().entrySet()) {
            String name = entry.getKey();
            Object object = ObjectStreamUtil.toObject(entry.getValue());
            if (FormatManager.INFO_NAME.equals(name)) {
                if (object instanceof ExtraInfo extraInfo) {
                    writeUtf8(outputDir.resolve(FormatManager.INFO_FILE_NAME), YesSteveModel.GSON.toJson(extraInfo));
                }
                continue;
            }

            if (object instanceof RawGeoModel rawGeoModel) {
                writeUtf8(outputDir.resolve(FormatManager.getModelFileName(name)), YesSteveModel.GSON.toJson(rawGeoModel));
            }
        }

        for (Map.Entry<String, byte[]> entry : data.getTexture().entrySet()) {
            writeBytes(outputDir.resolve(entry.getKey()), entry.getValue());
        }

        for (Map.Entry<String, byte[]> entry : data.getAnimation().entrySet()) {
            writeBytes(outputDir.resolve(FormatManager.getAnimFileName(entry.getKey())), entry.getValue());
        }
    }

    private static void writeUtf8(Path path, String content) throws IOException {
        writeBytes(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeBytes(Path path, byte[] data) throws IOException {
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(path, data);
    }

    private static void addExtraInfo(ResourceLocation modelId, @Nullable ExtraInfo extraInfo) {
        if (extraInfo == null) return;
        ResourceLocation infoId = ModelIdUtil.getInfoId(modelId);
        EXTRA_INFO.put(infoId, extraInfo);
        METADATA.put(infoId, readMetaData(extraInfo));
        if (extraInfo.getFree()) {
            AUTH_MODELS.remove(modelId.getPath());
        }
        if (modelId.getPath().equals("default") && extraInfo.getPreviewAnimation() != null) {
            DEFAULT_ANIMATION_FILE.animations().remove(extraInfo.getPreviewAnimation());
        }
    }

    private static void addModernInfo(ResourceLocation modelId, ServerModelInfo info) {
        addExtraInfo(modelId, toLegacyExtraInfo(info));
    }

    private static ExtraInfo toLegacyExtraInfo(ServerModelInfo info) {
        ExtraInfo extraInfo = new ExtraInfo();
        if (info.getExtraInfo() != null) {
            extraInfo.setName(info.getExtraInfo().getName());
            extraInfo.setTips(info.getExtraInfo().getTips());
            extraInfo.setAuthors(info.getExtraInfo().getAuthors().stream().map(AuthorInfo::getName).toArray(String[]::new));
            if (info.getExtraInfo().getLicense() != null) {
                extraInfo.setLicense(info.getExtraInfo().getLicense().getFirst());
            }
        }
        if (info.getModelProperties() != null) {
            extraInfo.setExtraAnimationNames(info.getModelProperties().getExtraAnimation().keySet().toArray(new String[0]));
            extraInfo.setFree(info.getModelProperties().isFree());
            extraInfo.setPreviewAnimation(info.getModelProperties().getPreviewAnimation());
            extraInfo.setDisablePreviewRotation(info.getModelProperties().isDisablePreviewRotation());
        }
        return extraInfo;
    }

    @Nullable
    private static List<String> readMetaData(@Nullable ExtraInfo extraInfo) {
        if (extraInfo == null || StringUtils.isBlank(extraInfo.getName())) {
            return null;
        }
        List<String> component = Lists.newArrayList();
        component.add(TextFormatting.GOLD + extraInfo.getName());
        if (extraInfo.getTips() != null && StringUtils.isNoneBlank(extraInfo.getTips())) {
            String[] split = extraInfo.getTips().split("\n");
            Arrays.stream(split).forEach(s -> component.add(TextFormatting.GRAY + I18n.format(s)));
        }
        if (extraInfo.getAuthors() != null && extraInfo.getAuthors().length != 0) {
            component.add(I18n.format("gui.yes_steve_model.model.authors", TextFormatting.RESET + StringUtils.join(extraInfo.getAuthors(), "丨")));
        }
        if (StringUtils.isNoneBlank(extraInfo.getLicense())) {
            component.add(I18n.format("gui.yes_steve_model.model.license", TextFormatting.RESET + extraInfo.getLicense()));
        }
        return component;
    }

    public static final class CacheExportResult {
        private final boolean success;
        private final int exportedCount;
        private final String filePath;
        private final String messageKey;

        private CacheExportResult(boolean success, int exportedCount, String filePath, String messageKey) {
            this.success = success;
            this.exportedCount = exportedCount;
            this.filePath = filePath;
            this.messageKey = messageKey;
        }

        public static CacheExportResult success(int exportedCount, String filePath) {
            return new CacheExportResult(true, exportedCount, filePath, null);
        }

        public static CacheExportResult failure(String messageKey) {
            return new CacheExportResult(false, 0, "", messageKey);
        }

        public boolean isSuccess() {
            return success;
        }

        public int getExportedCount() {
            return exportedCount;
        }

        public String getFilePath() {
            return filePath;
        }

        public String getMessageKey() {
            return messageKey;
        }
    }
}
