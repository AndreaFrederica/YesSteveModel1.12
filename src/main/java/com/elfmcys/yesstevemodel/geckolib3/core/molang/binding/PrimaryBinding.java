package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ForeignVariableBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.ScopedVariableBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable.TempVariableBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.MathBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.QueryBinding;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import com.elfmcys.yesstevemodel.molang.runtime.binding.StandardBindings;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import javax.annotation.Nullable;
import java.util.Map;

public class PrimaryBinding implements ObjectBinding {
    protected final Object2ReferenceOpenHashMap<String, Object> bindings = new Object2ReferenceOpenHashMap<>();
    protected final ScopedVariableBinding scopedBinding = new ScopedVariableBinding();
    protected final ForeignVariableBinding foreignBinding = new ForeignVariableBinding();
    protected final TempVariableBinding tempBinding = new TempVariableBinding();

    public PrimaryBinding(@Nullable Map<String, ObjectBinding> extraBindings) {
        if (extraBindings != null) {
            this.bindings.putAll(extraBindings);
        }
        this.bindings.put("math", MathBinding.INSTANCE);
        this.bindings.put("query", QueryBinding.INSTANCE);
        this.bindings.put("q", QueryBinding.INSTANCE);
        this.bindings.put("loop", StandardBindings.LOOP_FUNC);
        this.bindings.put("for_each", StandardBindings.FOR_EACH_FUNC);

        this.bindings.put("variable", this.scopedBinding);
        this.bindings.put("v", this.scopedBinding);

        this.bindings.put("context", this.foreignBinding);
        this.bindings.put("c", this.foreignBinding);

        this.bindings.put("temp", this.tempBinding);
        this.bindings.put("t", this.tempBinding);
    }

    @Override
    public Object getProperty(String name) {
        return this.bindings.get(name);
    }

    public void reset() {
        this.scopedBinding.reset();
        this.foreignBinding.reset();
        this.tempBinding.reset();
    }

    public void popStackFrame() {
        this.tempBinding.reset();
    }
}
