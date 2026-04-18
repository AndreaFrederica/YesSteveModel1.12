package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.variable;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IForeignVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Variable;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;

import javax.annotation.Nonnull;

@SuppressWarnings("MapOrSetKeyShouldOverrideHashCodeEquals")
public class ForeignVariableBinding implements ObjectBinding {
    private final Int2ReferenceOpenHashMap<ForeignVariable> variableMap = new Int2ReferenceOpenHashMap<>();

    @Override
    public Object getProperty(String name) {
        return this.variableMap.computeIfAbsent(StringPool.computeIfAbsent(name), ForeignVariable::new);
    }

    public void reset() {
        this.variableMap.clear();
    }

    @Desugar
    private record ForeignVariable(int name) implements Variable {
        @Override
        @SuppressWarnings("unchecked")
        public Object evaluate(final @Nonnull ExecutionContext<?> context) {
            IForeignVariableStorage storage = ((IContext<Object>) context.entity()).foreignStorage();
            if (storage != null) {
                return storage.getPublic(this.name);
            } else {
                return null;
            }
        }
    }
}
