package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.util.List;

public abstract class GeoEntity<T extends Entity> extends AnimatableEntity<T> {

    private List<IValue> renderLayers;

    public GeoEntity(@Nullable T entity, int fps) {
        super(entity, fps);
    }

    @Nullable
    public ModelAssembly getModelAssembly() {
        return null;
    }

    @Override
    @Nullable
    public final IValue resolveExpression(String str) {
        ModelAssembly assembly = getModelAssembly();
        if (assembly != null) {
            return assembly.getResourceBundle().getFunctions().get(str);
        }
        return null;
    }

    @Nullable
    public List<IValue> getRenderLayers() {
        return this.renderLayers;
    }

    public void setRenderLayers(@Nullable List<IValue> renderLayers) {
        this.renderLayers = renderLayers;
    }
}
