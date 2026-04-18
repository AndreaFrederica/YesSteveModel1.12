package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.ContextFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;

public class DebugOutputFunction extends ContextFunction<Object> {
    @Override
    protected Object eval(ExecutionContext<IContext<Object>> context, ArgumentCollection arguments) {
        if (context.entity().hasDebugOutput()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < arguments.size(); i++) {
                Object value = arguments.getValue(context, i);
                builder.append(value == null ? "null" : value);
            }
            context.entity().debugOutput(builder.toString());
        }
        return null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size > 0;
    }
}
