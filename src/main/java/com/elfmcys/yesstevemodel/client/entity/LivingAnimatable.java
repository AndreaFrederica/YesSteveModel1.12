package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.molang.MolangEventDispatcher;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LivingAnimatable<T extends EntityLivingBase> extends GeoEntity<T> {

    private ConditionManager modelConfig;
    private boolean needsInit = true;
    private IValue playerUpdateIValue;
    private final List<Object> updateExpressionArgs = new ArrayList<>();
    {
        this.updateExpressionArgs.add(false);
    }

    public LivingAnimatable(@Nullable T entity, int fps) {
        super(entity, fps);
    }

    public void onModelLoaded(ModelAssembly modelAssembly) {
        this.setAnimationMap(modelAssembly.getResourceBundle().getEvents());
        List<IValue> values = modelAssembly.getResourceBundle().getEvents().get(MolangEventDispatcher.PLAYER_UPDATE);
        if (values != null) {
            this.playerUpdateIValue = MolangEventDispatcher.createUpdateExpression(values, this.updateExpressionArgs);
        } else {
            this.playerUpdateIValue = null;
        }
        this.setRenderLayers(modelAssembly.getResourceBundle().getEvents().get(MolangEventDispatcher.DEFER));
    }

    @Override
    public void setupAnim(float seekTime, boolean isFirstPerson) {
        super.setupAnim(seekTime, isFirstPerson);
        if (this.needsInit) {
            this.needsInit = false;
            List<IValue> values = getAnimationExpressions(MolangEventDispatcher.PLAYER_INIT);
            if (values != null) {
                scheduleMolangExecution(MolangEventDispatcher.createInitExpression(values), true, true, null);
            }
        }
        if (this.playerUpdateIValue != null) {
            this.updateExpressionArgs.set(0, isFirstPerson);
            scheduleMolangExecution(this.playerUpdateIValue, true, true, null);
        }
    }

    public void resetInitFlag() {
        this.needsInit = true;
    }

    @Nullable
    public ConditionManager getModelConfig() {
        return this.modelConfig;
    }

    public void setModelConfig(ConditionManager modelConfig) {
        this.modelConfig = modelConfig;
    }

    @Override
    public EntityFrameStateTracker<T> createPositionTracker(@Nullable T entity) {
        return new LivingEntityFrameState<>(entity);
    }

    @Nullable
    @Override
    public LivingEntityFrameState<T> getPositionTracker() {
        return (LivingEntityFrameState<T>) super.getPositionTracker();
    }

    @Nullable
    @Override
    public ModelAssembly getModelAssembly() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getModelLocation() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAnimationFileLocation() {
        return null;
    }
}
