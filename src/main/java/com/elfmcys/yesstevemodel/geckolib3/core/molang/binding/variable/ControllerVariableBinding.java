package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ResetVariable;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IControllerVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.molang.runtime.AssignableVariable;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;

import javax.annotation.Nonnull;

@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class ControllerVariableBinding implements ObjectBinding, ResetVariable {
    private final Int2ReferenceOpenHashMap<ControllerVariable> variableMap = new Int2ReferenceOpenHashMap<>();

    @Override
    public Object getProperty(String name) {
        return this.variableMap.computeIfAbsent(StringPool.computeIfAbsent(name), ControllerVariable::new);
    }

    @Override
    public void reset() {
        this.variableMap.clear();
    }

    @Desugar
    private record ControllerVariable(int name) implements AssignableVariable {
        @Override
        @SuppressWarnings("unchecked")
        public Object evaluate(@Nonnull ExecutionContext<?> context) {
            IControllerVariableStorage storage = ((IContext<Object>) context.entity()).controllerStorage();
            if (storage != null) {
                return storage.getControllerVariable(this.name);
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void assign(@Nonnull ExecutionContext<?> context, Object value) {
            IControllerVariableStorage storage = ((IContext<Object>) context.entity()).controllerStorage();
            if (storage != null) {
                storage.setControllerVariable(this.name, value);
            }
        }
    }
}