package com.elfmcys.yesstevemodel.geckolib3.core;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;
import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.DebugOutputSink;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.AnimationProcessor;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import com.elfmcys.yesstevemodel.geckolib3.core.util.RateLimiter;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unchecked,rawtypes")
public abstract class AnimatableEntity<E extends Entity> {
    private final AnimationData manager = new AnimationData();
    private final AnimationProcessor animationProcessor;
    private final RateLimiter rateLimiter;
    private final EntityFrameStateTracker<E> positionTracker;

    /// 若为 null，则代表在 UI 中渲染，所有动画都不播放
    protected final @Nullable E entity;
    protected final PhysicsManager physicsManager;
    private final AudioPlayerManager audioPlayerManager = new AudioPlayerManager();

    private AnimatedGeoModel currentModel;
    private double seekTime;
    private double lastGameTickTime;
    private final Object2ReferenceOpenHashMap<String, AnimationState> animationStates = new Object2ReferenceOpenHashMap<>();
    private Object2ReferenceMap<String, List<IValue>> animationMap = new Object2ReferenceOpenHashMap<>();

    public AnimatableEntity(@Nullable E entity, int fps) {
        this.entity = entity;
        this.positionTracker = this.createPositionTracker(entity);
        this.rateLimiter = new RateLimiter(fps);
        this.animationProcessor = new AnimationProcessor(this);
        this.physicsManager = new PhysicsManager();
    }

    public EntityFrameStateTracker<E> createPositionTracker(@Nullable E entity) {
        return new EntityFrameStateTracker<>(entity);
    }

    public EntityFrameStateTracker<E> getPositionTracker() {
        return this.positionTracker;
    }

    /**
     * 注册动画控制器
     */
    public void addAnimationController(IAnimationController value) {
        this.manager.addAnimationController(value);
    }

    public void clearAnimationControllers() {
        this.manager.clear();
        this.animationStates.clear();
    }

    public AnimationData getAnimationData() {
        return this.manager;
    }

    public abstract ResourceLocation getModelLocation();

    public abstract ResourceLocation getTextureLocation();

    public boolean setCustomAnimations(AnimationContext<?> ctx, @Nonnull AnimationEvent<?> animationEvent) {
        if (!this.updateModel()) {
            return false;
        }
        Minecraft mc = Minecraft.getMinecraft();
        double currentTick = this.getCurrentTick(animationEvent);

        if (this.manager.startTick == -1) {
            this.manager.startTick = currentTick;
        }

        if (!mc.isGamePaused() || this.manager.shouldPlayWhilePaused) {
            this.manager.tick = Math.max(this.manager.tick, currentTick);
            double gameTick = this.manager.tick;
            double deltaTicks = gameTick - this.lastGameTickTime;
            this.seekTime += deltaTicks;
            this.lastGameTickTime = gameTick;
            this.codeAnimations(animationEvent);
        }

        animationEvent.animationTick = this.seekTime;
        this.animationProcessor.preAnimationSetup(this, this.seekTime);
        if (this.animationProcessor.isModelRendererEmpty()) {
            return false;
        }
        if (!this.forceUpdate(animationEvent) && !this.rateLimiter.request((float) this.seekTime * 20)) {
            return false;
        }

        this.physicsManager.update((float) this.seekTime);
        if (this.entity != null) {
            this.positionTracker.updateState(this.entity.ticksExisted, (float) this.seekTime, animationEvent.getPartialTick());
        }
        this.setupAnim((float) this.seekTime, false);
        this.animationProcessor.tickAnimation(this.seekTime, animationEvent, ctx);
        this.afterSetupAnim((float) this.seekTime, false);
        return true;
    }

    public void codeAnimations(AnimationEvent<?> customPredicate) {
    }

    public AnimationProcessor getAnimationProcessor() {
        return this.animationProcessor;
    }

    @Nullable
    public Animation getAnimation(String name) {
        AnimationFile animation = GeckoLibCache.getInstance().getAnimations().get(this.getAnimationFileLocation());
        if (animation == null) {
            YesSteveModel.LOGGER.debug("{}: Could not find animation file. Please double check name.", this.getAnimationFileLocation());
            return null;
        }
        return animation.getAnimation(name);
    }

    @Nullable
    public com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController getAnimationEntries(String controllerName) {
        return null;
    }

    public abstract ResourceLocation getAnimationFileLocation();

    public boolean updateModel() {
        var model = GeckoLibCache.getInstance().getGeoModels().get(this.getModelLocation());
        if (model == null) {
            return false;
        }
        if (this.currentModel == null || model != this.currentModel.geoModel()) {
            this.currentModel = new AnimatedGeoModel(model);
            this.animationProcessor.registerModelRenderer(this.currentModel.bones());
            this.physicsManager.reset();
        }
        return true;
    }

    @Nullable
    public AnimatedGeoModel getCurrentModel() {
        return this.currentModel;
    }

    @Nullable
    public final List<IValue> getAnimationExpressions(String str) {
        return this.animationMap.get(str);
    }

    public Object2ReferenceMap<String, List<IValue>> getAnimationExpressionMap() {
        return this.animationMap;
    }

    public void setAnimationMap(Object2ReferenceMap<String, List<IValue>> animationMap) {
        this.animationMap = animationMap;
    }

    public void setupAnim(float seekTime, boolean isFirstPerson) {
    }

    public void afterSetupAnim(float seekTime, boolean isFirstPerson) {
    }

    public double getCurrentTick(AnimationEvent<?> animationEvent) {
        float partialTick = animationEvent.getPartialTick();
        if (partialTick == 1.0f && partialTick != Minecraft.getMinecraft().getRenderPartialTicks()) {
            partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
        }
        double currentTick = this.entity != null ? this.entity.ticksExisted : this.getCurrentTick();
        return currentTick + partialTick;
    }

    protected double getCurrentTick() {
        return System.nanoTime() / 1000000.0 / 50.0;
    }

    /**
     * 无需确保幂等
     */
    protected boolean forceUpdate(AnimationEvent<?> animationEvent) {
        return false;
    }

    @Nullable
    public E getEntity() {
        return this.entity;
    }

    public double getSeekTime() {
        return this.seekTime;
    }

    public PhysicsManager getPhysicsManager() {
        return this.physicsManager;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return this.audioPlayerManager;
    }

    @Nullable
    public DebugOutputSink getDebugOutputSink() {
        return null;
    }

    public void scheduleMolangExecution(
            IValue value,
            boolean allowImpureOperations,
            boolean executeBeforeAnimation,
            @Nullable Consumer<String> resultConsumer
    ) {
        this.animationProcessor.queueExpression(value, allowImpureOperations, executeBeforeAnimation, resultConsumer);
    }

    public void setAnimationState(String name, AnimationState state) {
        this.animationStates.put(name, state);
    }

    public AnimationState getAnimationState(String name) {
        return this.animationStates.getOrDefault(name, AnimationState.IDLE);
    }

    public void resetAnimationState() {
        this.animationStates.clear();
    }

    @Nullable
    public IValue resolveExpression(String str) {
        return null;
    }
}
