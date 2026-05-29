package com.elfmcys.yesstevemodel.geckolib3.core.builder;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AnimationState {
    private static final int BUILTIN_ID = StringPool.computeIfAbsent("ysm-builtin");
    private static final String ENTRY_PREFIX = "ysm-entry-";

    private final String name;
    private final int hashId;
    private final boolean builtin;
    @Nullable
    private final String subName;
    private final List<Pair<String, IValue>> animations;
    private final List<Transition> transitions;
    private final List<String> soundEffects;
    private final List<IValue> onEntry;
    private final List<IValue> onExit;
    private final IInterpolable blendTransition;
    private final boolean blendViaShortestPath;

    public static final class Transition {
        private final int stateId;
        private final IValue value;

        public Transition(int stateId, IValue value) {
            this.stateId = stateId;
            this.value = value;
        }

        public int getStateId() {
            return this.stateId;
        }

        public IValue getValue() {
            return this.value;
        }
    }

    public AnimationState(String name, Pair<String, IValue>[] animations, Pair<String, IValue>[] transitions, String[] soundEffects, IValue[] onEntry, IValue[] onExit, IInterpolable blendTransition, boolean blendViaShortestPath) {
        this.name = name;
        this.hashId = StringPool.computeIfAbsent(name);
        this.builtin = this.hashId == BUILTIN_ID;
        this.subName = name.startsWith(ENTRY_PREFIX) ? name.substring(ENTRY_PREFIX.length()) : null;
        this.animations = Collections.unmodifiableList(Arrays.asList(animations));
        this.transitions = Collections.unmodifiableList(Arrays.stream(transitions)
                .map(pair -> new Transition(StringPool.computeIfAbsent(pair.getKey()), pair.getValue()))
                .collect(Collectors.toList()));
        this.soundEffects = Collections.unmodifiableList(Arrays.asList(soundEffects));
        this.onEntry = Collections.unmodifiableList(Arrays.asList(onEntry));
        this.onExit = Collections.unmodifiableList(Arrays.asList(onExit));
        this.blendTransition = blendTransition;
        this.blendViaShortestPath = blendViaShortestPath;
    }

    public String getName() {
        return this.name;
    }

    public int getHashId() {
        return this.hashId;
    }

    public boolean isBuiltinEntry() {
        return this.builtin;
    }

    @Nullable
    public String getSubName() {
        return this.subName;
    }

    public List<Pair<String, IValue>> getAnimations() {
        return this.animations;
    }

    public List<Transition> getTransitions() {
        return this.transitions;
    }

    public List<String> getSoundEffects() {
        return this.soundEffects;
    }

    public List<IValue> getPreExpressions() {
        return this.onEntry;
    }

    public List<IValue> getPostExpressions() {
        return this.onExit;
    }

    public IInterpolable getBlendTransition() {
        return this.blendTransition;
    }

    public boolean isBlendViaShortestPath() {
        return this.blendViaShortestPath;
    }
}