package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;

import java.util.function.Consumer;

public class PlayerModelBundle {
    private final GeoModel mainModel;
    private final GeoModel armModel;
    private final Object2ReferenceMap<String, Animation> mainAnimations;
    private final Object2ReferenceMap<String, Animation> armAnimations;
    private final Object2ReferenceMap<String, AnimationController> animationControllers;
    private final OrderedStringMap<String, OuterFileTexture> textures;
    private final String defaultTextureName;
    private final OuterFileTexture defaultTexture;
    private final ConditionManager conditionManager;
    private final ArmorConditions modelProcessor;
    private final Consumer<CustomPlayerEntity> playerControllerInstaller;
    private final Consumer<PlayerGeoEntity> armControllerInstaller;
    private final Object maidControllerInstaller;

    public PlayerModelBundle(GeoModel mainModel, GeoModel armModel, Object2ReferenceMap<String, Animation> mainAnimations, Object2ReferenceMap<String, Animation> armAnimations, Object2ReferenceMap<String, AnimationController> animationControllers, OrderedStringMap<String, OuterFileTexture> textures, String defaultTextureName, OuterFileTexture defaultTexture, ConditionManager conditionManager, ArmorConditions modelProcessor, Consumer<CustomPlayerEntity> playerControllerInstaller, Consumer<PlayerGeoEntity> armControllerInstaller, Object maidControllerInstaller) {
        this.mainModel = mainModel;
        this.armModel = armModel;
        this.mainAnimations = mainAnimations;
        this.armAnimations = armAnimations;
        this.animationControllers = animationControllers;
        this.textures = textures;
        this.defaultTextureName = defaultTextureName;
        this.defaultTexture = defaultTexture;
        this.conditionManager = conditionManager;
        this.modelProcessor = modelProcessor;
        this.playerControllerInstaller = playerControllerInstaller;
        this.armControllerInstaller = armControllerInstaller;
        this.maidControllerInstaller = maidControllerInstaller;
    }

    public GeoModel getMainModel() {
        return this.mainModel;
    }

    public GeoModel getArmModel() {
        return this.armModel;
    }

    public Object2ReferenceMap<String, Animation> getMainAnimations() {
        return this.mainAnimations;
    }

    public Object2ReferenceMap<String, Animation> getArmAnimations() {
        return this.armAnimations;
    }

    public Object2ReferenceMap<String, AnimationController> getAnimationControllers() {
        return this.animationControllers;
    }

    public OrderedStringMap<String, OuterFileTexture> getTextures() {
        return this.textures;
    }

    public String getDefaultTextureName() {
        return this.defaultTextureName;
    }

    public OuterFileTexture getDefaultTexture() {
        return this.defaultTexture;
    }

    public ConditionManager getConditionManager() {
        return this.conditionManager;
    }

    public ArmorConditions getModelProcessor() {
        return this.modelProcessor;
    }

    public Consumer<CustomPlayerEntity> getPlayerControllerInstaller() {
        return this.playerControllerInstaller;
    }

    public Consumer<PlayerGeoEntity> getArmControllerInstaller() {
        return this.armControllerInstaller;
    }

    public Object getMaidControllerInstaller() {
        return this.maidControllerInstaller;
    }
}
