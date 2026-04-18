package com.elfmcys.yesstevemodel.geckolib3.core.molang.context;

import com.elfmcys.yesstevemodel.client.capability.CustomPlayerEntityCapabilityProvider;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationControllerContext;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IScopedVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.ITempVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.VariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

import java.util.Random;

public class AnimationContext<TEntity> implements IContext<TEntity> {
    protected final TEntity entity;
    protected final AnimatableEntity<?> instance;
    protected final AnimationEvent<?> animationEvent;
    protected final EntityModelData data;

    protected AnimationControllerContext animationControllerContext;
    protected Random random;
    protected VariableStorage storage;
    protected IForeignVariableStorage foreignStorage;
    /// Debug 输出池，输出调试信息到聊天界面
    protected DebugOutputSink debugOutputSink;
    protected boolean allowImpureOperations;

    public AnimationContext(
            TEntity entity, AnimatableEntity<?> instance,
            AnimationEvent<?> animationEvent, EntityModelData data
    ) {
        this.entity = entity;
        this.instance = instance;
        this.animationEvent = animationEvent;
        this.data = data;
        this.debugOutputSink = instance.getDebugOutputSink();
    }

    private AnimationContext(
            TEntity entity, AnimatableEntity<?> instance,
            AnimationEvent<?> animationEvent, EntityModelData data,
            AnimationControllerContext animationControllerContext, Random random, VariableStorage storage,
            IForeignVariableStorage foreignStorage, DebugOutputSink debugOutputSink, boolean allowImpureOperations
    ) {
        this.entity = entity;
        this.instance = instance;
        this.animationEvent = animationEvent;
        this.data = data;
        this.animationControllerContext = animationControllerContext;
        this.random = random;
        this.storage = storage;
        this.foreignStorage = foreignStorage;
        this.debugOutputSink = debugOutputSink;
        this.allowImpureOperations = allowImpureOperations;
        if (entity instanceof EntityPlayer player) {
            CapabilityEvent.getCapability(player, CustomPlayerEntityCapabilityProvider.CAP)
                    .ifPresent(cap -> this.foreignStorage = cap.getAnimationProcessor().getPublicVariableStorage());
        }
    }

    @Override
    public AnimationEvent<?> animationEvent() {
        return this.animationEvent;
    }

    @Override
    public AnimatableEntity<?> geoInstance() {
        return this.instance;
    }

    @Override
    public EntityModelData data() {
        return this.data;
    }

    @Override
    public AnimationControllerContext animationControllerContext() {
        return this.animationControllerContext;
    }

    @Override
    public Random random() {
        return this.random;
    }

    @Override
    public boolean hasDebugOutput() {
        return this.debugOutputSink != null;
    }

    @Override
    public boolean allowImpureOperations() {
        return this.allowImpureOperations;
    }

    @Override
    public void debugOutput(String message, Object... args) {
        if (this.debugOutputSink != null) {
            this.debugOutputSink.output(message, args);
        }
    }

    @Override
    public void debugOutput(ITextComponent component) {
        if (this.debugOutputSink != null) {
            this.debugOutputSink.output(component);
        }
    }

    @Override
    public TEntity entity() {
        return this.entity;
    }

    @Override
    public Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    @Override
    public WorldClient level() {
        Minecraft mc = this.mc();
        if (mc != null) {
            return mc.world;
        } else {
            return null;
        }
    }

    @Override
    public <TChild> IContext<TChild> createChild(TChild child) {
        return new AnimationContext<>(child, this.instance, this.animationEvent, this.data, this.animationControllerContext,
                this.random, this.storage, this.foreignStorage, this.debugOutputSink, this.allowImpureOperations);
    }

    @Override
    public ITempVariableStorage tempStorage() {
        return this.storage;
    }

    @Override
    public IScopedVariableStorage scopedStorage() {
        return this.storage;
    }

    @Override
    public IForeignVariableStorage foreignStorage() {
        return this.foreignStorage;
    }

    public void setAnimationControllerContext(AnimationControllerContext animationControllerContext) {
        this.animationControllerContext = animationControllerContext;
    }

    public void setStorage(VariableStorage storage) {
        this.storage = storage;
        this.foreignStorage = storage;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public void setDebugOutputSink(DebugOutputSink debugOutputSink) {
        this.debugOutputSink = debugOutputSink;
    }

    public void setAllowImpureOperations(boolean allowImpureOperations) {
        this.allowImpureOperations = allowImpureOperations;
    }
}
