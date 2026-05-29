package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.TransitionPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlaybackFlags;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.AnimationPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.NewBoneAnimationQueue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.util.MathUtil;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.ConstantPoint;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.geckolib3.core.util.TransitionVector3f;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class AnimationControllerRuntime<T extends AnimatableEntity<?>> implements IAnimationController<T> {

    private static final int MAX_DEPTH = 5;

    private final T animatable;

    private final String name;

    private final float transitionLengthTicks;

    @Nullable
    private List<BoneTopLevelSnapshot> boneTargets;

    @Nullable
    private AnimationController animationEntries;

    @Nullable
    private AnimationState currentEntry;

    @Nullable
    private String displayName;

    @Nullable
    private AnimationControllerRuntime<T> childController;

    private final ReferenceArrayList<AnimationSlot> animationSlots = new ReferenceArrayList<>(8);

    private int activeSlotCount = 0;

    private final Int2ReferenceOpenHashMap<BoneBlendState> boneTransformMap = new Int2ReferenceOpenHashMap<>(64);

    private final ReferenceArrayList<BoneBlendState> activeBoneTransforms = new ReferenceArrayList<>(16);

    private boolean needsRebuild = false;

    private final IntOpenHashSet visitedEntries = new IntOpenHashSet(4);

    @Nullable
    private String parentName = null;

    private int depth = 1;

    private final PlaybackFlags playbackFlags = new PlaybackFlags(true);

    public AnimationControllerRuntime(T animatable, String name, float transitionLengthTicks) {
        this.animatable = animatable;
        this.name = name;
        this.transitionLengthTicks = transitionLengthTicks;
    }

    @Override
    public void process(AnimationEvent<T> event, ExpressionEvaluator<AnimationContext<?>> evaluator, boolean isMoving) {
        if (this.animationEntries == null) {
            return;
        }
        evaluator.entity().setAnimationControllerContext(null);
        evaluator.entity().setPlaybackFlags(this.playbackFlags);
        float currentTick = (float) event.animationTick;
        this.visitedEntries.clear();
        boolean transitioned = false;
        while (evaluateTransitions(evaluator)) {
            transitioned = true;
            if (this.activeSlotCount != 0) {
                break;
            }
        }
        if (this.currentEntry != null && this.currentEntry.getSubName() != null && this.depth != MAX_DEPTH) {
            if (transitioned) {
                String subControllerName = this.depth > 1 ? String.format("%s.%s", this.parentName, this.currentEntry.getSubName()) : this.currentEntry.getSubName();
                AnimationController childController = this.animatable.getAnimationEntries(String.format("%s.%s", this.name, subControllerName));
                if (childController != null) {
                    if (this.childController == null) {
                        this.childController = new AnimationControllerRuntime<>(this.animatable, this.name, this.transitionLengthTicks);
                        this.childController.initWithBones(this.boneTargets, childController);
                    } else {
                        this.childController.updateAnimationEntries(childController);
                    }
                    this.childController.setParentInfo(subControllerName, this.depth + 1);
                }
            }
            if (this.childController != null) {
                this.childController.process(event, evaluator, isMoving);
                return;
            }
            return;
        }
        for (int slotIndex = 0; slotIndex < this.activeSlotCount; slotIndex++) {
            AnimationSlot slot = this.animationSlots.get(slotIndex);
            slot.getCondition().evaluate(evaluator);
            slot.getResampler().process(currentTick, evaluator, isMoving && slot.getCondition().isActive());
        }
        if (this.needsRebuild) {
            for (int i2 = 0; i2 < this.activeSlotCount; i2++) {
                AnimationSlot slot2 = this.animationSlots.get(i2);
                Iterator<NewBoneAnimationQueue> it = slot2.animationControllerInstance.getActiveBoneAnimationQueues().iterator();
                while (it.hasNext()) {
                    NewBoneAnimationQueue boneQueue = it.next();
                    BoneBlendState blendState = this.boneTransformMap.get(boneQueue.topLevelSnapshot.bone.getBoneId());
                    if (!blendState.checkMarked()) {
                        blendState.mark();
                        this.activeBoneTransforms.add(blendState);
                    }
                    blendState.addBlendSource(slot2.condition, boneQueue);
                }
            }
            this.needsRebuild = false;
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getCurrentAnimationName() {
        if (this.currentEntry != null) {
            if (this.currentEntry.getSubName() != null && this.childController != null) {
                return this.childController.getCurrentAnimationName();
            }
            return this.displayName;
        }
        return "(null)";
    }

    @Override
    @Nullable
    public com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation getCurrentAnimation() {
        return null;
    }

    private void updateDisplayName(String stateName) {
        if (this.depth > 1) {
            this.displayName = String.format("[%s] %s", this.parentName, stateName);
        } else {
            this.displayName = stateName;
        }
    }

    @Nullable
    public AnimationState getCurrentEntry() {
        return this.currentEntry;
    }

    public boolean isBuiltinAnimation() {
        return this.currentEntry != null && this.currentEntry.isBuiltinEntry();
    }

    @Override
    public void init(List<BoneTopLevelSnapshot> list, Object2ReferenceMap<String, List<IValue>> object2ReferenceMap) {
        AnimationController childController = this.animatable.getAnimationEntries(this.name);
        if (childController != null) {
            initWithBones(list, childController);
        } else {
            reset();
        }
    }

    public void initWithBones(List<BoneTopLevelSnapshot> list, AnimationController controller) {
        reset();
        this.animationEntries = controller;
        for (BoneTopLevelSnapshot snapshot : list) {
            this.boneTransformMap.put(snapshot.bone.getBoneId(), new BoneBlendState(snapshot));
        }
        this.boneTargets = list;
    }

    private void updateAnimationEntries(AnimationController controller) {
        this.animationEntries = controller;
    }

    private void setParentInfo(String parentName, int depth) {
        this.parentName = parentName;
        this.depth = depth;
    }

    private boolean evaluateTransitions(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        if (this.currentEntry == null) {
            AnimationState nextState = this.animationEntries.getStates().get(this.animationEntries.getStateId());
            if (nextState == null) {
                return false;
            }
            this.visitedEntries.add(nextState.getHashId());
            updateDisplayName(nextState.getName());
            transitionToEntry(nextState, evaluator);
            return true;
        }
        int activeCount = 0;
        this.playbackFlags.setStopped(false);
        this.playbackFlags.setPaused(true);
        for (int slotIndex = 0; slotIndex < this.activeSlotCount; slotIndex++) {
            AnimationSlot slot = this.animationSlots.get(slotIndex);
            if (slot.condition.isActive()) {
                activeCount++;
                if (slot.animationControllerInstance.isAnimationFinished()) {
                    this.playbackFlags.setStopped(true);
                } else {
                    this.playbackFlags.setPaused(false);
                }
            }
        }
        if (activeCount == 0) {
            this.playbackFlags.setStopped(true);
        }
        for (AnimationState.Transition transition : this.currentEntry.getTransitions()) {
            if (transition.getValue().evalAsBoolean(evaluator)) {
                AnimationState nextState2 = this.animationEntries.getStates().get(transition.getStateId());
                if (nextState2 == null || !this.visitedEntries.add(nextState2.getHashId())) {
                    return false;
                }
                updateDisplayName(nextState2.getName());
                transitionToEntry(nextState2, evaluator);
                return true;
            }
        }
        return false;
    }

    private void transitionToEntry(@Nullable AnimationState nextState, ExpressionEvaluator<AnimationContext<?>> evaluator) {
        if (nextState == null && this.currentEntry == null) {
            return;
        }
        evaluator.entity().setIsClientSide(true);
        if (this.currentEntry != null) {
            if (this.currentEntry.getSubName() != null && this.childController != null) {
                this.childController.transitionToEntry(null, evaluator);
            }
            for (IValue value : this.currentEntry.getPostExpressions()) {
                value.evalSafe(evaluator);
            }
        }
        if (nextState != null) {
            for (IValue value : nextState.getPreExpressions()) {
                value.evalSafe(evaluator);
            }
            for (String str : nextState.getSoundEffects()) {
                if (StringUtils.isNotBlank(str) && this.playbackFlags.isAudioEnabled()) {
                    this.playbackFlags.getAudioPlayerManager().playSound(this.animatable, 0, str, false, null);
                }
            }
        }
        evaluator.entity().setIsClientSide(false);
        this.currentEntry = nextState;
        for (BoneBlendState activeBoneTransform : this.activeBoneTransforms) {
            activeBoneTransform.resetAndClear();
        }
        this.activeBoneTransforms.clear();
        this.playbackFlags.getAudioPlayerManager().stopAll();
        this.needsRebuild = true;
        int size = (nextState == null || nextState.isBuiltinEntry() || nextState.getSubName() != null) ? 0 : nextState.getAnimations().size();
        for (int size2 = this.animationSlots.size(); size2 < size; size2++) {
            this.animationSlots.add(new AnimationSlot(this.animatable, this.transitionLengthTicks));
        }
        for (int i = size; i < this.activeSlotCount; i++) {
            AnimationControllerInstance animInstance = this.animationSlots.get(i).animationControllerInstance;
            animInstance.executeRenderLayers(evaluator);
            animInstance.cancelAnimation();
        }
        this.activeSlotCount = size;
        for (int i = 0; i < size; i++) {
            AnimationSlot slot = this.animationSlots.get(i);
            Pair<String, IValue> pair = nextState.getAnimations().get(i);
            if (slot.isNewSlot()) {
                slot.getResampler().initBoneQueues(this.boneTargets);
                slot.markInitialized();
            }
            slot.getCondition().setExpression(pair.getRight());
            slot.getResampler().executeRenderLayers(evaluator);
            slot.getResampler().setTransitionInterpolator(nextState.getBlendTransition().asInterpolator());
            slot.getResampler().resetRequestedAnimation();
            slot.getResampler().setAnimation(pair.getLeft());
        }
    }

    @Override
    public void forEachTransform(Consumer<BoneTransformProvider> consumer) {
        if (this.currentEntry != null) {
            if (this.currentEntry.getSubName() != null && this.childController != null) {
                this.childController.forEachTransform(consumer);
                return;
            }
            for (BoneBlendState blendState : this.activeBoneTransforms) {
                if (blendState.hasActiveSources()) {
                    consumer.accept(blendState);
                }
            }
        }
    }

    @Override
    public void reset() {
        this.boneTargets = Collections.emptyList();
        this.animationEntries = null;
        this.currentEntry = null;
        this.activeSlotCount = 0;
        this.activeBoneTransforms.clear();
        this.boneTransformMap.clear();
        if (this.depth == 1) {
            this.childController = null;
        }
        for (AnimationSlot animationSlot : this.animationSlots) {
            animationSlot.animationControllerInstance.fullReset();
        }
        this.animationSlots.clear();
    }

    private static class AnimationSlot {

        private final AnimationControllerInstance animationControllerInstance;

        private final ConditionalEvaluator condition = new ConditionalEvaluator();

        private boolean isNew = true;

        private AnimationSlot(AnimatableEntity<?> entity, float f) {
            this.animationControllerInstance = new AnimationControllerInstance(entity, f);
        }

        public ConditionalEvaluator getCondition() {
            return this.condition;
        }

        public AnimationControllerInstance getResampler() {
            return this.animationControllerInstance;
        }

        public boolean isNewSlot() {
            return this.isNew;
        }

        public void markNew() {
            this.isNew = true;
        }

        public void markInitialized() {
            this.isNew = false;
        }
    }

    private static class ConditionalEvaluator {

        @Nullable
        private IValue IValue;

        private boolean active = true;

        public void setExpression(@Nullable IValue value) {
            this.IValue = value;
            if (value == null) {
                this.active = true;
            }
        }

        public void evaluate(ExpressionEvaluator<?> evaluator) {
            if (this.IValue != null) {
                this.active = this.IValue.evalAsBoolean(evaluator);
            }
        }

        public boolean isActive() {
            return this.active;
        }
    }

    private static class BoneBlendState implements BoneTransformProvider {

        private final BoneTopLevelSnapshot boneTarget;

        private final ReferenceArrayList<Pair<ConditionalEvaluator, NewBoneAnimationQueue>> blendSources = new ReferenceArrayList<>(4);

        private boolean isMarked;

        public BoneBlendState(BoneTopLevelSnapshot snapshot) {
            this.boneTarget = snapshot;
        }

        public int getBoneId() {
            return this.boneTarget.bone.getBoneId();
        }

        public void addBlendSource(ConditionalEvaluator evaluator, NewBoneAnimationQueue queue) {
            this.blendSources.add(Pair.of(evaluator, queue));
        }

        public boolean hasActiveSources() {
            if (this.blendSources.isEmpty()) {
                return false;
            }
            for (Pair<ConditionalEvaluator, NewBoneAnimationQueue> blendSource : this.blendSources) {
                if (blendSource.getLeft().isActive()) {
                    return true;
                }
            }
            return false;
        }

        public boolean checkMarked() {
            return this.isMarked;
        }

        public void mark() {
            this.isMarked = true;
        }

        public void resetAndClear() {
            this.isMarked = false;
            this.blendSources.clear();
        }

        @Override
        public BoneTopLevelSnapshot getBoneTarget() {
            return this.boneTarget;
        }

        @Override
        public Optional<TransitionVector3f> getRotation(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            AnimationPoint animationPoint;
            TransitionVector3f transitionVector3f = new TransitionVector3f();
            boolean hasData = false;
            boolean isFirst = true;
            boolean isTransition = false;
            Vector3f offsetPoint = null;
            Vector3f initialRotation = null;
            float lerpFactor = 0.0f;
            for (Pair<ConditionalEvaluator, NewBoneAnimationQueue> pair : this.blendSources) {
                if (pair.getLeft().isActive()) {
                    NewBoneAnimationQueue boneQueue = pair.getRight();
                    if (boneQueue.isActive() && (animationPoint = boneQueue.rotationQueue) != null) {
                        hasData = true;
                        if (isFirst) {
                            isFirst = false;
                            if (animationPoint instanceof TransitionPoint transition) {
                                isTransition = true;
                                offsetPoint = transition.getOffsetPoint();
                                lerpFactor = transition.getLerpFactor();
                                transitionVector3f.setPercentCompleted(0.0f);
                                initialRotation = boneQueue.topLevelSnapshot.bone.getInitialRotation();
                            }
                        }
                        if (!isTransition) {
                            Vector3f lerpPoint = animationPoint.getLerpPoint(evaluator);
                            float blendWeight = pair.getRight().getBlendWeight();
                            if (animationPoint instanceof ConstantPoint) {
                                float percentCompleted = (float) animationPoint.getPercentCompleted();
                                blendWeight *= 1.0f - percentCompleted;
                                transitionVector3f.setPercentCompleted(percentCompleted);
                            } else {
                                transitionVector3f.setPercentCompleted(0.0f);
                            }
                            transitionVector3f.x += blendWeight * lerpPoint.x;
                            transitionVector3f.y += blendWeight * lerpPoint.y;
                            transitionVector3f.z += blendWeight * lerpPoint.z;
                        } else if (lerpFactor <= -1.0E-5f || lerpFactor >= 1.0E-5f) {
                            float bw = pair.getRight().getBlendWeight();
                            Vector3f raw = ((TransitionPoint) animationPoint).evaluateRaw(evaluator);
                            transitionVector3f.x += bw * raw.x;
                            transitionVector3f.y += bw * raw.y;
                            transitionVector3f.z += bw * raw.z;
                        } else {
                            transitionVector3f.set(offsetPoint);
                            return Optional.of(transitionVector3f);
                        }
                    }
                }
            }
            if (hasData) {
                if (isTransition) {
                    MathUtil.nlerpEulerAngles(lerpFactor, offsetPoint, transitionVector3f, initialRotation, transitionVector3f);
                }
                return Optional.of(transitionVector3f);
            }
            return Optional.empty();
        }

        @Override
        public Optional<TransitionVector3f> getPosition(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            AnimationPoint point;
            TransitionVector3f result = new TransitionVector3f();
            boolean hasData = false;
            boolean isFirst = true;
            boolean isTransition = false;
            Vector3f offsetPoint = null;
            float lerpFactor = 0.0f;
            for (Pair<ConditionalEvaluator, NewBoneAnimationQueue> pair : this.blendSources) {
                if (pair.getLeft().isActive()) {
                    NewBoneAnimationQueue boneQueue = pair.getRight();
                    if (boneQueue.isActive() && (point = boneQueue.positionQueue) != null) {
                        hasData = true;
                        if (isFirst) {
                            isFirst = false;
                            if (point instanceof TransitionPoint transition) {
                                isTransition = true;
                                offsetPoint = transition.getOffsetPoint();
                                lerpFactor = transition.getLerpFactor();
                                result.setPercentCompleted(0.0f);
                            }
                        }
                        if (!isTransition) {
                            Vector3f lerpPoint = point.getLerpPoint(evaluator);
                            float blendWeight = pair.getRight().getBlendWeight();
                            if (point instanceof ConstantPoint) {
                                float percentCompleted = (float) point.getPercentCompleted();
                                blendWeight *= 1.0f - percentCompleted;
                                result.setPercentCompleted(percentCompleted);
                            } else {
                                result.setPercentCompleted(0.0f);
                            }
                            result.x += blendWeight * lerpPoint.x;
                            result.y += blendWeight * lerpPoint.y;
                            result.z += blendWeight * lerpPoint.z;
                        } else if (lerpFactor <= -1.0E-5f || lerpFactor >= 1.0E-5f) {
                            float bw = pair.getRight().getBlendWeight();
                            Vector3f raw = ((TransitionPoint) point).evaluateRaw(evaluator);
                            result.x += bw * raw.x;
                            result.y += bw * raw.y;
                            result.z += bw * raw.z;
                        } else {
                            result.set(offsetPoint);
                            return Optional.of(result);
                        }
                    }
                }
            }
            if (hasData) {
                if (isTransition) {
                    MathUtil.lerpValues(lerpFactor, offsetPoint, result, result);
                }
                return Optional.of(result);
            }
            return Optional.empty();
        }

        @Override
        public Optional<TransitionVector3f> getScale(ExpressionEvaluator<AnimationContext<?>> evaluator) {
            AnimationPoint point;
            TransitionVector3f result = new TransitionVector3f(1.0f, 1.0f, 1.0f);
            boolean hasData = false;
            boolean isFirst = true;
            boolean isTransition = false;
            Vector3f offsetPoint = null;
            float lerpFactor = 0.0f;
            for (Pair<ConditionalEvaluator, NewBoneAnimationQueue> pair : this.blendSources) {
                if (pair.getLeft().isActive()) {
                    NewBoneAnimationQueue boneQueue = pair.getRight();
                    if (boneQueue.isActive() && (point = boneQueue.scaleQueue) != null) {
                        hasData = true;
                        if (isFirst) {
                            isFirst = false;
                            if (point instanceof TransitionPoint transition) {
                                isTransition = true;
                                offsetPoint = transition.getOffsetPoint();
                                lerpFactor = transition.getLerpFactor();
                                result.setPercentCompleted(0.0f);
                            }
                        }
                        if (!isTransition) {
                            Vector3f lerpPoint = point.getLerpPoint(evaluator);
                            float blendWeight = pair.getRight().getBlendWeight();
                            if (point instanceof ConstantPoint) {
                                float percentCompleted = (float) point.getPercentCompleted();
                                blendWeight *= 1.0f - percentCompleted;
                                result.setPercentCompleted(percentCompleted);
                            } else {
                                result.setPercentCompleted(0.0f);
                            }
                            if (blendWeight == 1.0f) {
                                result.x *= lerpPoint.x;
                                result.y *= lerpPoint.y;
                                result.z *= lerpPoint.z;
                            } else {
                                Vector3f interpolated = MathUtil.lerpAngles(lerpPoint, blendWeight);
                                result.x *= interpolated.x;
                                result.y *= interpolated.y;
                                result.z *= interpolated.z;
                            }
                        } else if (lerpFactor <= -1.0E-5f || lerpFactor >= 1.0E-5f) {
                            Vector3f raw = ((TransitionPoint) point).evaluateRaw(evaluator);
                            Vector3f interpolated = MathUtil.lerpAngles(raw, pair.getRight().getBlendWeight());
                            result.x *= interpolated.x;
                            result.y *= interpolated.y;
                            result.z *= interpolated.z;
                        } else {
                            result.set(offsetPoint);
                            return Optional.of(result);
                        }
                    }
                }
            }
            if (hasData) {
                if (isTransition) {
                    MathUtil.lerpValues(lerpFactor, offsetPoint, result, result);
                }
                return Optional.of(result);
            }
            return Optional.empty();
        }
    }
}
