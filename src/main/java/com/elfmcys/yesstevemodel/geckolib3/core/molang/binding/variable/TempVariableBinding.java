package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import javax.annotation.Nonnull;

public class TempVariableBinding implements ObjectBinding {
    private final Object2ReferenceMap<String, TempVariable> variableMap = new Object2ReferenceOpenHashMap<>();
    private int topPointer = 0;

    @Override
    public Object getProperty(String name) {
        return this.variableMap.computeIfAbsent(name, k -> new TempVariable(this.topPointer++));
    }

    public void reset() {
        this.variableMap.clear();
        this.topPointer = 0;
    }

    @Desugar
    private record TempVariable(int address) implements AssignableVariable {
        @Override
        @SuppressWarnings("unchecked")
        public Object evaluate(final @Nonnull ExecutionContext<?> context) {
            return ((IContext<Object>) context.entity()).tempStorage().getTemp(this.address);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void assign(@Nonnull ExecutionContext<?> context, Object value) {
            ((IContext<Object>) context.entity()).tempStorage().setTemp(this.address, value);
        }
    }
}
