package com.elfmcys.yesstevemodel.util;

import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.util.EnumHelper;

public final class ComponentUtils {
    public static final ClickEvent.Action COPY_TO_CLIPBOARD = EnumHelper.addEnum(
            ClickEvent.Action.class,
            "COPY_TO_CLIPBOARD",
            new Class<?>[]{String.class, boolean.class},
            "copy_to_clipboard", true
    );

    /**
     * Wraps the text with square brackets.
     */
    public static ITextComponent wrapInSquareBrackets(ITextComponent toWrap) {
        return new TextComponentTranslation("message.yes_steve_model.model.square_brackets", toWrap);
    }

    public static ITextComponent copyOnClickText(String text) {
        Style style = new Style().setColor(TextFormatting.GREEN);
        if (COPY_TO_CLIPBOARD != null) {
            style.setClickEvent(new ClickEvent(COPY_TO_CLIPBOARD, text));
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("message.yes_steve_model.model.copy_to_clipboard")));
        }
        return wrapInSquareBrackets(new TextComponentString(text).setStyle(style));
    }
}
