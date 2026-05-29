package com.elfmcys.yesstevemodel.geckolib3.core.molang.context;

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IControllerVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IScopedVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.ITempVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.text.ITextComponent;

import java.util.List;
import java.util.Random;

public interface IContext<TEntity> {
    TEntity entity();

    AnimatableEntity<?> geoInstance();

    Minecraft mc();

    WorldClient level();

    AnimationEvent<?> animationEvent();

    EntityModelData data();

    AnimationControllerContext animationControllerContext();

    Random random();

    boolean hasDebugOutput();

    /// 是否允许执行有实际作用的 molang 操作，如输出信息、播放声音、生成粒子或触发同步。
    boolean allowImpureOperations();

    boolean isClientSide();

    void debugOutput(String message, Object... args);

    void debugOutput(ITextComponent component);

    <TChild> IContext<TChild> createChild(TChild child);

    ITempVariableStorage tempStorage();

    IScopedVariableStorage scopedStorage();

    IControllerVariableStorage controllerStorage();

    IForeignVariableStorage foreignStorage();

    @javax.annotation.Nullable
    IValue resolveExpression(String str);

    Object callFunction(ExecutionContext<?> context, IValue value, List<?> list);

    Object callFunctionWithArgs(ExecutionContext<?> context, IValue value, Function.ArgumentCollection arguments);

    void logWarning(String str, Object... objArr);

    @javax.annotation.Nullable
    AudioPlayerManager getAudioPlayerManager(boolean global);
}
