package com.elfmcys.yesstevemodel.geckolib3.core.molang.context;

import net.minecraft.util.text.ITextComponent;

public interface DebugOutputSink {
    void output(String message, Object... args);

    void output(ITextComponent component);
}
