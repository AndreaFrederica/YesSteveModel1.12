package com.elfmcys.yesstevemodel.geckolib3.model;

import com.elfmcys.yesstevemodel.geckolib3.animation.AnimationTicker;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatableModel;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.AnimationProcessor;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.exception.GeckoLibException;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.GeoModelProvider;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.IAnimatableModelProvider;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.Collections;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AnimatedGeoModel<T extends IAnimatable> extends GeoModelProvider<T> implements IAnimatableModel<T>, IAnimatableModelProvider<T> {
    private final AnimationProcessor animationProcessor;
    private GeoModel currentModel;

    protected AnimatedGeoModel() {
        this.animationProcessor = new AnimationProcessor(this);
    }

    public void registerBone(GeoBone bone) {
        this.registerModelRenderer(bone);
        for (GeoBone childBone : bone.childBones) {
            this.registerBone(childBone);
        }
    }

    @Override
    public void setCustomAnimations(T animatable, int instanceId, AnimationEvent animationEvent) {
        AnimationData manager = animatable.getFactory().getOrCreateAnimationData(instanceId);
        if (manager.ticker == null) {
            AnimationTicker ticker = new AnimationTicker(manager);
            manager.ticker = ticker;
            MinecraftForge.EVENT_BUS.register(ticker);
        }
        Minecraft mc = Minecraft.getMinecraft();
        this.seekTime = !mc.isGamePaused() || manager.shouldPlayWhilePaused ?
                manager.tick + mc.getRenderPartialTicks() :
                manager.tick;

        AnimationEvent<T> predicate;
        predicate = animationEvent == null ? new AnimationEvent<>(animatable, 0, 0, (float) (manager.tick - this.lastGameTickTime), false, Collections.emptyList()) : animationEvent;
        predicate.animationTick = this.seekTime;
        this.getAnimationProcessor().preAnimationSetup(predicate.getAnimatable(), this.seekTime);
        if (!this.getAnimationProcessor().getModelRendererList().isEmpty()) {
            this.getAnimationProcessor().tickAnimation(animatable, instanceId, this.seekTime, predicate, GeckoLibCache.getInstance().parser, this.shouldCrashOnMissing);
        }
    }

    @Override
    public AnimationProcessor getAnimationProcessor() {
        return this.animationProcessor;
    }

    public void registerModelRenderer(IBone modelRenderer) {
        this.animationProcessor.registerModelRenderer(modelRenderer);
    }

    @Override
    public Animation getAnimation(String name, IAnimatable animatable) {
        AnimationFile animation = GeckoLibCache.getInstance().getAnimations().get(this.getAnimationFileLocation((T) animatable));
        if (animation == null) {
            throw new GeckoLibException(this.getAnimationFileLocation((T) animatable), "Could not find animation file. Please double check name.");
        }
        return animation.getAnimation(name);
    }

    @Override
    public GeoModel getModel(ResourceLocation location) {
        GeoModel model = super.getModel(location);
        if (model == null) {
            throw new GeckoLibException(location, "Could not find model. If you are getting this with a built mod, please just restart your game.");
        }
        if (model != this.currentModel) {
            this.animationProcessor.clearModelRendererList();
            this.currentModel = model;
            for (GeoBone bone : model.topLevelBones) {
                this.registerBone(bone);
            }
        }
        return model;
    }

    public GeoModel getCurrentModel() {
        return this.currentModel;
    }

    @Override
    public double getCurrentTick() {
        return Minecraft.getSystemTime() / 50d;
    }
}
