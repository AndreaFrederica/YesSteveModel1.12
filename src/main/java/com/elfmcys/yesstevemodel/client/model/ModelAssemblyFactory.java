package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.ClientModelInfo;
import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.FirstPersonArmAnimationController;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationControllerFile;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.file.ProjectileModelFiles;
import com.elfmcys.yesstevemodel.geckolib3.file.VehicleModelFiles;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModelAssemblyFactory {
    private static final String FIRST_PERSON_ARM_BONE = "fp_arm";
    private static ModelAssembly primaryAssembly;

    private ModelAssemblyFactory() {
    }

    public static ModelAssembly buildAssembly(ClientModelInfo clientModelInfo, boolean primary, boolean auth) {
        List<AbstractTexture> textures = new ArrayList<>();
        ModelResourceBundle resourceBundle = buildResourceBundle(clientModelInfo);
        ModelAssembly assembly = new ModelAssembly(
                buildPlayerModelBundle(clientModelInfo, primary, textures, resourceBundle),
                buildProjectileModels(clientModelInfo, textures, resourceBundle),
                buildVehicleModels(clientModelInfo, textures),
                resourceBundle,
                clientModelInfo.getInfo(),
                buildDisplayAssets(clientModelInfo, auth, textures),
                Collections.unmodifiableList(textures)
        );
        if (primary) {
            primaryAssembly = assembly;
            primaryAssembly.getAnimationBundle().getMainAnimations().values().forEach(animation -> animation.isFromPrimaryAssembly = true);
        }
        return assembly;
    }

    private static PlayerModelBundle buildPlayerModelBundle(ClientModelInfo clientModelInfo, boolean primary, List<AbstractTexture> textures, ModelResourceBundle resourceBundle) {
        MainModelData data = clientModelInfo.getMainModelData();
        GeoModel mainModel = data.getModels().isEmpty() ? null : data.getModels().get(0);
        GeoModel armModel = data.getModels().size() > 1 ? data.getModels().get(1) : mainModel;

        Object2ReferenceOpenHashMap<String, Animation> mainAnimations = new Object2ReferenceOpenHashMap<>();
        Object2ReferenceOpenHashMap<String, Animation> armAnimations = new Object2ReferenceOpenHashMap<>();
        for (Map.Entry<String, AnimationFile> entry : data.getAnimations().entrySet()) {
            if (FIRST_PERSON_ARM_BONE.equals(entry.getKey())) {
                armAnimations.putAll(entry.getValue().animations());
            } else {
                mainAnimations.putAll(entry.getValue().animations());
            }
        }
        if (!primary && primaryAssembly != null) {
            primaryAssembly.getAnimationBundle().getMainAnimations().forEach(mainAnimations::putIfAbsent);
            primaryAssembly.getAnimationBundle().getArmAnimations().forEach(armAnimations::putIfAbsent);
        }

        Object2ReferenceOpenHashMap<String, AnimationController> controllers = new Object2ReferenceOpenHashMap<>();
        for (AnimationControllerFile file : data.getAnimationControllers()) {
            controllers.putAll(file.getAnimationControllers());
        }
        Object2ReferenceOpenHashMap<String, AnimationController> normalizedControllers = new Object2ReferenceOpenHashMap<>(controllers);
        controllers.forEach((key, value) -> {
            if (key.indexOf('.') < 0) {
                normalizedControllers.putIfAbsent("player." + key, value);
            }
        });

        OrderedStringMap<String, OuterFileTexture> textureMap = data.getTextureMap();
        for (OuterFileTexture texture : textureMap.values()) {
            textures.add(texture);
            textures.addAll(texture.getSuffixTextures().values());
        }

        String defaultTexture = clientModelInfo.getInfo().getModelProperties() != null ? clientModelInfo.getInfo().getModelProperties().getDefaultTexture() : "";
        if (StringUtils.isEmpty(defaultTexture) || !textureMap.containsKey(defaultTexture)) {
            defaultTexture = textureMap.isEmpty() ? "" : textureMap.getKeyAt(0);
        }

        ConditionManager conditionManager = new ConditionManager();
        mainAnimations.keySet().forEach(conditionManager::addTest);

        ArmorConditions modelProcessor = new ArmorConditions();
        armAnimations.keySet().forEach(modelProcessor::addCondition);
        // Create bundle with null installers first, then build the installer from it
        PlayerModelBundle bundle = new PlayerModelBundle(mainModel, armModel, mainAnimations, armAnimations, normalizedControllers, textureMap, defaultTexture, textureMap.get(defaultTexture), conditionManager, modelProcessor, null, null, null);
        var playerInstaller = PlayerAnimationController.buildControllers(bundle, resourceBundle);
        var armInstaller = FirstPersonArmAnimationController.buildControllers(bundle, resourceBundle);

        return new PlayerModelBundle(mainModel, armModel, mainAnimations, armAnimations, normalizedControllers, textureMap, defaultTexture, textureMap.get(defaultTexture), conditionManager, modelProcessor, playerInstaller, armInstaller, null);
    }

    private static Map<ResourceLocation, ProjectileModelBundle> buildProjectileModels(ClientModelInfo clientModelInfo, List<AbstractTexture> textures, ModelResourceBundle resourceBundle) {
        Map<ResourceLocation, ProjectileModelBundle> result = new LinkedHashMap<>();
        for (ProjectileModelFiles files : clientModelInfo.getExtraItemModels()) {
            if (files.getTexture() != null) {
                textures.add(files.getTexture());
                textures.addAll(files.getTexture().getSuffixTextures().values());
            }
            ProjectileModelBundle bundle = new ProjectileModelBundle(
                    files.getModel(),
                    new Object2ReferenceOpenHashMap<>(files.getAnimations() != null ? files.getAnimations().animations() : Object2ReferenceMaps.emptyMap()),
                    new Object2ReferenceOpenHashMap<>(files.getAnimationController() != null ? files.getAnimationController().getAnimationControllers() : Object2ReferenceMaps.emptyMap()),
                    files.getTexture(),
                    resourceBundle
            );
            for (String name : files.getTextureNames()) {
                result.put(new ResourceLocation(name), bundle);
            }
        }
        return result;
    }

    private static Map<ResourceLocation, VehicleModelBundle> buildVehicleModels(ClientModelInfo clientModelInfo, List<AbstractTexture> textures) {
        Map<ResourceLocation, VehicleModelBundle> result = new LinkedHashMap<>();
        for (VehicleModelFiles files : clientModelInfo.getVehicleModelFiles()) {
            if (files.getTexture() != null) {
                textures.add(files.getTexture());
                textures.addAll(files.getTexture().getSuffixTextures().values());
            }
            VehicleModelBundle bundle = new VehicleModelBundle(
                    files.getModel(),
                    new Object2ReferenceOpenHashMap<>(files.getAnimations() != null ? files.getAnimations().animations() : Object2ReferenceMaps.emptyMap()),
                    new Object2ReferenceOpenHashMap<>(files.getAnimationController() != null ? files.getAnimationController().getAnimationControllers() : Object2ReferenceMaps.emptyMap()),
                    files.getTexture()
            );
            for (String name : files.getTextureNames()) {
                result.put(new ResourceLocation(name), bundle);
            }
        }
        return result;
    }

    private static ModelResourceBundle buildResourceBundle(ClientModelInfo clientModelInfo) {
        Object2ReferenceOpenHashMap<String, IValue> functions = new Object2ReferenceOpenHashMap<>();
        Object2ReferenceOpenHashMap<String, List<IValue>> events = new Object2ReferenceOpenHashMap<>();
        clientModelInfo.getExtraResources().getFunctions().forEach((key, value) -> {
            int atIndex = key.indexOf('@');
            if (atIndex != 0) {
                functions.put(atIndex == -1 ? key : key.substring(0, atIndex), value);
            }
            if (atIndex != -1 && atIndex + 1 < key.length()) {
                events.computeIfAbsent(key.substring(atIndex + 1).toLowerCase(), ignored -> new ArrayList<>()).add(value);
            }
        });
        return new ModelResourceBundle(clientModelInfo.getExtraResources().getAudioTracks(), functions, events, clientModelInfo.getExtraResources().getTranslations());
    }

    private static ModelDisplayAssets buildDisplayAssets(ClientModelInfo clientModelInfo, boolean auth, List<AbstractTexture> textures) {
        Map<String, AbstractTexture> guiTextures = new LinkedHashMap<>();
        clientModelInfo.getGuiTextures().forEach((name, texture) -> {
            if (texture != null) {
                textures.add(texture);
                guiTextures.put(name, texture);
            }
        });
        Metadata metadata = clientModelInfo.getInfo().getExtraInfo();
        return new ModelDisplayAssets(metadata == null ? "" : metadata.getName(), auth, clientModelInfo.getAvatarTextures(), guiTextures);
    }
}
