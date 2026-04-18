package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.DebugOutputSink;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class ClientChatDebugOutputSink implements DebugOutputSink {
    public static final ClientChatDebugOutputSink INSTANCE = new ClientChatDebugOutputSink();

    @Override
    public void output(String message, Object... args) {
        this.output(new TextComponentString(String.format(message, args)));
    }

    @Override
    public void output(ITextComponent component) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null) {
            mc.addScheduledTask(() -> {
                mc.player.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.debug_animation.output", component));
            });
        }
    }
}
