package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModVersion implements Function {
    @Nullable
    @Override
    public Object evaluate(@Nonnull ExecutionContext<?> context, @Nonnull ArgumentCollection arguments) {
        String modId = arguments.getAsString(context, 0);
        if (modId == null) {
            return null;
        }

        ModContainer modContainer = Loader.instance().getIndexedModList().get(modId);
        return modContainer != null ? modContainer.getVersion() : null;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }
}
