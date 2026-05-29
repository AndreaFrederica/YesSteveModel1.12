/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.InstructionKeyFrameExecutor;
import com.elfmcys.yesstevemodel.geckolib3.core.event.ParticleKeyFrameEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.event.SoundKeyframeEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.*;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.EasingType;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AnimationController<T extends AnimatableEntity<?>> implements IAnimationController<T> {
    /**
     * 动画控制器名称
     */
    private final String name;
    private final Object2ReferenceOpenHashMap<String, BoneAnimationQueue> boneAnimationQueues = new Object2ReferenceOpenHashMap<>();
    private final ReferenceArrayList<BoneAnimationQueue> activeBoneAnimationQueues = new ReferenceArrayList<>();
    private InstructionKeyFrameExecutor instructionKeyFrameExecutor;
    /**
     * 在动画之间过渡需要多长时间
     */
    public double transitionLengthTicks;
    public boolean isJustStarting = false;
    public double tickOffset;
    public double animationSpeed = 1D;
    /**
     * 默认情况下，动画将使用关键帧的 EasingType <br>
     * 复写此数值将用于全局
     */
    public EasingType easingType = EasingType.LINEAR;
    /**
     * 实体对象
     */
    protected final T animatable;
    /**
     * 动画谓词，每次触发前都会调用一次
     */
    protected IAnimationPredicate<T> animationPredicate;
    protected AnimationState animationState = AnimationState.STOPPED;
    protected Queue<Pair<ILoopType, Animation>> animationQueue = new LinkedList<>();
    protected Animation currentAnimation;
    protected ILoopType currentAnimationLoop;
    protected AnimationBuilder currentAnimationBuilder = new AnimationBuilder();
    public boolean shouldResetTick = false;
    protected boolean justStartedTransition = false;
    protected boolean needsAnimationReload = false;
    /**
     * 播放声音关键帧时触发的 Sound Listener
     */
    private ISoundListener<T> soundListener;
    /**
     * 播放粒子关键帧时触发的 Particle Listener
     */
    private IParticleListener<T> particleListener;
    private boolean justStopped = false;

    /**
     * 实例化动画控制器，每个控制器同一时间只能播放一个动画 <br>
     * 你可以为一个实体附加多个动画控制器 <br>
     * 比如一个控制器控制实体大小，另一个控制移动，攻击等等
     *
     * @param animatable            实体
     * @param name                  动画控制器名称
     * @param transitionLengthTicks 动画过渡时间（tick）
     */
    public AnimationController(T animatable, String name, float transitionLengthTicks,
                               IAnimationPredicate<T> animationPredicate) {
        this.animatable = animatable;
        this.name = name;
        this.transitionLengthTicks = transitionLengthTicks;
        this.animationPredicate = animationPredicate;
        this.tickOffset = 0.0d;
    }

    /**
     * 实例化动画控制器，每个控制器同一时间只能播放一个动画 <br>
     * 你可以为一个实体附加多个动画控制器 <br>
     * 比如一个控制器控制实体大小，另一个控制移动，攻击等等
     *
     * @param animatable            实体
     * @param name                  动画控制器名称
     * @param transitionLengthTicks 动画过渡时间（tick）
     * @param easingtype            动画过渡插值类型，默认没有
     */
    public AnimationController(T animatable, String name, float transitionLengthTicks, EasingType easingtype,
                               IAnimationPredicate<T> animationPredicate) {
        this.animatable = animatable;
        this.name = name;
        this.transitionLengthTicks = transitionLengthTicks;
        this.easingType = easingtype;
        this.animationPredicate = animationPredicate;
        this.tickOffset = 0.0d;
    }

    /**
     * 此方法使用 AnimationBuilder 设置当前动画
     * 你可以每帧运行此方法，如果每次都传入相同的 AnimationBuilder，它将不会重新启动。
     * 此外，它还可以在动画状态之间平滑过渡
     */
    @Override
    public void setAnimation(String animationName, ILoopType loopType) {
        if (animationName == null || animationName.isEmpty()) {
            this.animationState = AnimationState.STOPPED;
            return;
        }
        AnimationBuilder builder = new AnimationBuilder().addAnimation(animationName, loopType);
        this.setAnimation(builder);
    }

    @Override
    public void setAnimation(String animationName) {
        if (animationName == null || animationName.isEmpty()) {
            this.animationState = AnimationState.STOPPED;
            return;
        }
        AnimationBuilder builder = new AnimationBuilder().addAnimation(animationName);
        this.setAnimation(builder);
    }

    public void setAnimation(AnimationBuilder builder) {
        if (builder == null || builder.getRawAnimationList().isEmpty()) {
            this.animationState = AnimationState.STOPPED;
        } else if (!builder.getRawAnimationList().equals(this.currentAnimationBuilder.getRawAnimationList()) || this.needsAnimationReload) {
            AtomicBoolean encounteredError = new AtomicBoolean(false);
            // 将动画名称列表转换为实际列表，并在此过程中跟踪循环布尔值
            LinkedList<Pair<ILoopType, Animation>> animations = builder.getRawAnimationList().stream().map((rawAnimation) -> {
                Animation animation = this.animatable.getAnimation(rawAnimation.animationName());
                if (animation == null) {
                    YesSteveModel.LOGGER.warn("Could not load animation: {}. Is it missing?", rawAnimation.animationName());
                    encounteredError.set(true);
                    return null;
                } else {
                    ILoopType loopType = animation.loop();
                    if (rawAnimation.loopType() != null) {
                        loopType = rawAnimation.loopType();
                    }
                    return Pair.of(loopType, animation);
                }
            }).collect(Collectors.toCollection(LinkedList::new));
            if (encounteredError.get()) {
                return;
            }
            this.animationQueue = animations;
            this.currentAnimationBuilder = builder;
            // 下一个动画调用时，将其重置为 0
            this.shouldResetTick = true;
            this.animationState = AnimationState.TRANSITIONING;
            this.justStartedTransition = true;
            this.needsAnimationReload = false;
        }
    }

    /**
     * 获取动画控制器名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 当前动画，可以为 null
     */
    @Nullable
    public Animation getCurrentAnimation() {
        return this.currentAnimation;
    }

    /**
     * 当前动画控制器状态
     */
    public AnimationState getAnimationState() {
        return this.animationState;
    }

    /**
     * 当前动画骨骼动画队列
     */
    public List<BoneAnimationQueue> getBoneAnimationQueues() {
        return this.activeBoneAnimationQueues;
    }

    /**
     * 注册 Sound Listener
     */
    public void registerSoundListener(ISoundListener<T> soundListener) {
        this.soundListener = soundListener;
    }

    /**
     * 注册 Particle Listener
     */
    public void registerParticleListener(IParticleListener<T> particleListener) {
        this.particleListener = particleListener;
    }

    /**
     * 此方法每帧调用一次，以便填充动画点队列并处理动画状态逻辑。
     *
     * @param tick              当前 tick + 插值 tick
     * @param event             动画测试事件
     * @param modelRendererList 所有的 AnimatedModelRender 列表
     */
    public void process(final double tick, AnimationEvent<T> event, ExpressionEvaluator<AnimationContext<?>> evaluator, List<BoneTopLevelSnapshot> modelRendererList,
                        boolean crashWhenCantFindBone, boolean isRendererDirty, boolean scheduledUpdate) {
        AnimationControllerContext context = new AnimationControllerContext();
        if (this.currentAnimation != null) {
            Animation animation = this.animatable.getAnimation(this.currentAnimation.animationName());
            if (animation != null && this.currentAnimation != animation) {
                this.currentAnimation = animation;
                this.instructionKeyFrameExecutor = new InstructionKeyFrameExecutor(animation.customInstructionKeyframes());
            }
        }

        if (isRendererDirty) {
            this.switchRenderer(modelRendererList);
            if (this.currentAnimation != null) {
                this.switchAnimation();
            }
        }

        double adjustedTick = this.adjustTick(tick);
        // 过渡结束，重置 tick 并将动画设置为运行
        if (this.animationState == AnimationState.TRANSITIONING && adjustedTick >= this.transitionLengthTicks) {
            this.shouldResetTick = true;
            this.animationState = AnimationState.RUNNING;
            adjustedTick = this.adjustTick(tick);
        }
        assert adjustedTick >= 0 : "GeckoLib: Tick was less than zero";

        // 测试动画谓词
        PlayState playState = this.testAnimationPredicate(event);
        if (playState == PlayState.STOP || (this.currentAnimation == null && this.animationQueue.isEmpty())) {
            // 动画过渡到模型的初始状态
            this.animationState = AnimationState.STOPPED;
            this.justStopped = true;
            return;
        }

        boolean resetTick = this.shouldResetTick;
        if (this.justStartedTransition && (this.shouldResetTick || this.justStopped)) {
            this.justStopped = false;
            adjustedTick = this.adjustTick(tick);
        } else if (this.currentAnimation == null && !this.animationQueue.isEmpty()) {
            this.shouldResetTick = true;
            resetTick = true;
            this.animationState = AnimationState.TRANSITIONING;
            this.justStartedTransition = true;
            this.needsAnimationReload = false;
            adjustedTick = this.adjustTick(tick);
        } else if (this.animationState != AnimationState.TRANSITIONING) {
            this.animationState = AnimationState.RUNNING;
        }

        // 处理过渡到其他动画（或仅开始一个动画）
        if (this.animationState == AnimationState.TRANSITIONING) {
            // 刚开始过渡，所以将当前动画设置为第一个
            if (resetTick || this.isJustStarting) {
                this.justStartedTransition = false;
                Pair<ILoopType, Animation> current = this.animationQueue.poll();
                if (current != null) {
                    this.currentAnimationLoop = current.getLeft();
                    this.currentAnimation = current.getRight();
                    this.instructionKeyFrameExecutor = new InstructionKeyFrameExecutor(current.getRight().customInstructionKeyframes());
                    this.resetEventKeyFrames(false, null, false);
                    this.switchAnimation();
                } else {
                    this.currentAnimation = null;
                    this.instructionKeyFrameExecutor = null;
                }
            }
            if (this.currentAnimation != null) {
                context.setAnimTime(0);
                for (BoneAnimationQueue boneAnimationQueue : this.activeBoneAnimationQueues) {
                    BoneAnimation boneAnimation = boneAnimationQueue.animation;
                    if (boneAnimation == null) {
                        continue;
                    }
                    BoneSnapshot boneSnapshot = boneAnimationQueue.snapshot();
                    BoneSnapshot initialSnapshot = boneAnimationQueue.topLevelSnapshot.bone.getInitialSnapshot();

                    // 添加即将出现的动画的初始位置，以便模型转换到新动画的初始状态
                    List<BoneKeyFrame> rotationKeyFrames = boneAnimation.rotationKeyFrames();
                    if (!rotationKeyFrames.isEmpty()) {
                        AnimationPoint point = this.getTransitionPointAtTick(rotationKeyFrames, adjustedTick,
                                new Vector3f(boneSnapshot.rotationValueX - initialSnapshot.rotationValueX,
                                        boneSnapshot.rotationValueY - initialSnapshot.rotationValueY,
                                        boneSnapshot.rotationValueZ - initialSnapshot.rotationValueZ),
                                context);
                        boneAnimationQueue.rotationQueue().add(point);
                    }

                    List<BoneKeyFrame> positionKeyFrames = boneAnimation.positionKeyFrames();
                    if (!positionKeyFrames.isEmpty()) {
                        AnimationPoint point = this.getTransitionPointAtTick(positionKeyFrames, adjustedTick,
                                new Vector3f(boneSnapshot.positionOffsetX,
                                        boneSnapshot.positionOffsetY,
                                        boneSnapshot.positionOffsetZ),
                                context);
                        boneAnimationQueue.positionQueue().add(point);
                    }

                    List<BoneKeyFrame> scaleKeyFrames = boneAnimation.scaleKeyFrames();
                    if (!scaleKeyFrames.isEmpty()) {
                        AnimationPoint point = this.getTransitionPointAtTick(scaleKeyFrames, adjustedTick,
                                new Vector3f(boneSnapshot.scaleValueX,
                                        boneSnapshot.scaleValueY,
                                        boneSnapshot.scaleValueZ),
                                context);
                        boneAnimationQueue.scaleQueue().add(point);
                    }
                }
            }
        } else if (this.getAnimationState() == AnimationState.RUNNING) {
            this.resetQueues();
            // 开始运行动画
            this.processCurrentAnimation(context, evaluator, adjustedTick, tick, crashWhenCantFindBone, scheduledUpdate);
        }
    }

    protected PlayState testAnimationPredicate(AnimationEvent<T> event) {
        return this.animationPredicate.test(event);
    }

    private void processCurrentAnimation(AnimationControllerContext context, ExpressionEvaluator<AnimationContext<?>> evaluator, double tick, double actualTick, boolean crashWhenCantFindBone, boolean scheduledUpdate) {
        assert this.currentAnimation != null;
        evaluator.entity().setAnimationControllerContext(context);

        // 如果动画已经结束了
        if (tick >= this.currentAnimation.animationLength()) {
            context.setAnimTime(this.currentAnimation.animationLength() / 20.0f);
            // 如果动画为循环播放，继续重头播放
            if (!this.currentAnimationLoop.isRepeatingAfterEnd()) {
                // 从队列中提取下一个动画
                Pair<ILoopType, Animation> peek = this.animationQueue.peek();
                if (peek == null) {
                    // 没有动画了，那么停止
                    this.animationState = AnimationState.STOPPED;
                    return;
                } else {
                    // 否则，将状态设置为过渡并开始过渡到下一个动画为下一帧
                    this.animationState = AnimationState.TRANSITIONING;
                    this.shouldResetTick = true;
                    this.currentAnimation = peek.getRight();
                    this.instructionKeyFrameExecutor = new InstructionKeyFrameExecutor(peek.getRight().customInstructionKeyframes());
                    this.currentAnimationLoop = peek.getLeft();
                }
            } else {
                // 重置 tick，以便下一个动画从刻度 0 开始
                this.shouldResetTick = true;
                tick = this.adjustTick(actualTick);
                this.resetEventKeyFrames(true, evaluator, scheduledUpdate);
            }
        }
        context.setAnimTime(tick / 20.0f);

        // 循环遍历当前动画中的每个骨骼动画并处理值
        for (BoneAnimationQueue boneAnimationQueue : this.activeBoneAnimationQueues) {
            BoneAnimation boneAnimation = boneAnimationQueue.animation;

            List<BoneKeyFrame> rotationKeyFrames = boneAnimation.rotationKeyFrames();
            if (!rotationKeyFrames.isEmpty()) {
                boneAnimationQueue.rotationQueue().add(this.getKeyFramePointAtTick(rotationKeyFrames, tick, context));
            }

            List<BoneKeyFrame> positionKeyFrames = boneAnimation.positionKeyFrames();
            if (!positionKeyFrames.isEmpty()) {
                boneAnimationQueue.positionQueue().add(this.getKeyFramePointAtTick(positionKeyFrames, tick, context));
            }

            List<BoneKeyFrame> scaleKeyFrames = boneAnimation.scaleKeyFrames();
            if (!scaleKeyFrames.isEmpty()) {
                boneAnimationQueue.scaleQueue().add(this.getKeyFramePointAtTick(scaleKeyFrames, tick, context));
            }
        }

        // TODO: 声音关键帧和粒子关键帧暂不支持

        // 计划外更新不执行指令关键帧
        if (this.instructionKeyFrameExecutor != null && scheduledUpdate) {
            boolean previousAllowImpureOperations = evaluator.entity().allowImpureOperations();
            evaluator.entity().setAllowImpureOperations(true);
            try {
                this.instructionKeyFrameExecutor.executeTo(evaluator, tick);
            } finally {
                evaluator.entity().setAllowImpureOperations(previousAllowImpureOperations);
            }
        }

        if (this.transitionLengthTicks == 0 && this.shouldResetTick && this.animationState == AnimationState.TRANSITIONING) {
            Pair<ILoopType, Animation> current = this.animationQueue.poll();
            if (current != null) {
                this.currentAnimation = current.getRight();
                this.currentAnimationLoop = current.getLeft();
                this.instructionKeyFrameExecutor = new InstructionKeyFrameExecutor(current.getRight().customInstructionKeyframes());
            } else {
                this.currentAnimation = null;
                this.instructionKeyFrameExecutor = null;
            }
        }
    }

    // 在开始新的过渡时，将模型的初始旋转、位置和缩放存储为快照
    private void switchAnimation() {
        this.activeBoneAnimationQueues.clear();
        for (BoneAnimation animation : this.currentAnimation.boneAnimations()) {
            BoneAnimationQueue queue = this.boneAnimationQueues.get(animation.boneName());
            if (queue == null) {
                continue;
            }
            queue.animation = animation;
            queue.updateSnapshot();
            queue.resetQueues();
            this.activeBoneAnimationQueues.add(queue);
        }
    }

    // 切换模型，重新填充所有初始动画点队列
    private void switchRenderer(List<BoneTopLevelSnapshot> modelRendererList) {
        this.boneAnimationQueues.clear();
        for (BoneTopLevelSnapshot modelRenderer : modelRendererList) {
            this.boneAnimationQueues.put(modelRenderer.name, new BoneAnimationQueue(modelRenderer));
        }
        this.activeBoneAnimationQueues.clear();
        this.markNeedsReload();
    }

    private void resetQueues() {
        for (BoneAnimationQueue queue : this.activeBoneAnimationQueues) {
            queue.resetQueues();
        }
    }

    // 在新动画开始、过渡开始或者其他情况下重置 tick
    public double adjustTick(double tick) {
        if (this.shouldResetTick) {
            if (this.getAnimationState() == AnimationState.TRANSITIONING) {
                this.tickOffset = tick;
            } else if (this.getAnimationState() == AnimationState.RUNNING) {
                this.tickOffset = tick;
            }
            this.shouldResetTick = false;
            return 0;
        } else {
            return this.animationSpeed * Math.max(tick - this.tickOffset, 0.0D);
        }
    }

    /**
     * 返回当前关键帧播放进度
     **/
    private AnimationPoint getKeyFramePointAtTick(List<BoneKeyFrame> frames, double tick, AnimationControllerContext context) {
        for (int i = 0; i < frames.size(); i++) {
            if (frames.get(i).getStartTick() > tick) {
                BoneKeyFrame frame = frames.get(i - 1);
                return new KeyFramePoint(tick - frame.getStartTick(), frame, context);
            }
        }
        BoneKeyFrame frame = frames.get(frames.size() - 1);
        return new KeyFramePoint(tick - frame.getStartTick(), frame, context);
    }

    /**
     * 返回过渡进度
     **/
    private TransitionPoint getTransitionPointAtTick(List<BoneKeyFrame> frames, double tick, Vector3f offsetPoint, AnimationControllerContext context) {
        BoneKeyFrame dstFrame = frames.get(0);
        return new TransitionPoint(tick, this.transitionLengthTicks, offsetPoint, dstFrame, context);
    }

    private void resetEventKeyFrames(boolean reachEnd, ExpressionEvaluator<AnimationContext<?>> evaluator, boolean allowImpureOperations) {
        if (this.instructionKeyFrameExecutor != null) {
            if (reachEnd) {
                boolean previousAllowImpureOperations = evaluator.entity().allowImpureOperations();
                evaluator.entity().setAllowImpureOperations(allowImpureOperations);
                try {
                    this.instructionKeyFrameExecutor.executeRemaining(evaluator);
                } finally {
                    evaluator.entity().setAllowImpureOperations(previousAllowImpureOperations);
                }
            }
            this.instructionKeyFrameExecutor.reset();
        }
    }

    public void markNeedsReload() {
        this.needsAnimationReload = true;
    }

    public void reset() {
        this.animationState = AnimationState.STOPPED;
        this.currentAnimation = null;
        this.currentAnimationLoop = null;
        this.currentAnimationBuilder = new AnimationBuilder();
        this.animationQueue.clear();
        this.activeBoneAnimationQueues.clear();
        this.shouldResetTick = false;
        this.justStartedTransition = false;
        this.needsAnimationReload = false;
        this.justStopped = false;
        this.tickOffset = 0.0d;
        this.isJustStarting = false;
    }

    public void clearAnimationCache() {
        this.currentAnimationBuilder = new AnimationBuilder();
    }

    /**
     * 旧版控制器不实现 forEachTransform，由 AnimationProcessor 直接轮询骨队列。
     * 新版控制器（如 PredicateBasedController）会覆写此方法。
     */
    @Override
    public void forEachTransform(java.util.function.Consumer<BoneTransformProvider> consumer) {
        // 默认空实现 — 旧版 AnimationProcessor 通过 getBoneAnimationQueues() 直接消费
    }

    public double getAnimationSpeed() {
        return this.animationSpeed;
    }

    public void setAnimationSpeed(double animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    /**
     * 每个 AnimationController 每个关键帧都会运行一次 AnimationPredicate
     * test 方法就是你改变动画、停止动画、重置的地方
     */
    @FunctionalInterface
    public interface IAnimationPredicate<P extends AnimatableEntity<?>> {
        PlayState test(AnimationEvent<P> event);
    }

    @FunctionalInterface
    public interface ISoundListener<A extends AnimatableEntity<?>> {
        void playSound(SoundKeyframeEvent<A> event);
    }

    @FunctionalInterface
    public interface IParticleListener<A extends AnimatableEntity<?>> {
        void summonParticle(ParticleKeyFrameEvent<A> event);
    }
}
