package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.StringUtils;

import javax.annotation.Nonnull;

public abstract class BoneParamFunction extends EntityFunction {
    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }

    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        var str = arguments.getAsString(context, 0);
        if (StringUtils.isNullOrEmpty(str)) {
            return null;
        }

        var bone = context.entity().geoInstance().getAnimationProcessor().getBone(str);
        if (bone == null) {
            return null;
        }

        return this.getParam(bone);
    }

    protected abstract Vec3fStruct getParam(@Nonnull IBone bone);
}
