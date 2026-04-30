package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query;

import com.elfmcys.yesstevemodel.client.util.EntityUtil;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.ContextFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;

// TODO：测试
public class RotationToCamera extends ContextFunction<Object> {
    @Override
    protected Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
        int axis = arguments.getAsInt(context, 0);
        if (axis >= 0 && axis <= 1) {
            return axis == 0 ? -EntityUtil.getCameraXRot(context.entity().mc(), context.entity().animationEvent().getPartialTick()) :
                    180.0F + EntityUtil.getCameraYRot(context.entity().mc(), context.entity().animationEvent().getPartialTick());
        }
        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
