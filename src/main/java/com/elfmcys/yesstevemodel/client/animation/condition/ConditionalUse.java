package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.compat.SpyglassCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.LinkedHashMap;
import java.util.Locale;

public class ConditionalUse extends ConditionItem {
    private static final LinkedHashMap<String, IItemStackMatcher> ACTIONS = new LinkedHashMap<>();

    public ConditionalUse(EnumHand hand) {
        super("use_" + (hand == EnumHand.MAIN_HAND ? "mainhand" : "offhand"));
    }

    @Override
    protected LinkedHashMap<String, IItemStackMatcher> getExtras() {
        initActions();
        return ACTIONS;
    }

    private static void initActions() {
        if (!ACTIONS.isEmpty()) return;

        // 越往上优先级越高
        ACTIONS.put("shield", (player, stack) ->
                stack.getItem() instanceof ItemShield || isSameActionName(stack, "shield"));
        if (CrossbowCompat.isInstalled()) {
            ACTIONS.put(CrossbowCompat.CROSSBOW_ACTION, (player, stack) ->
                    CrossbowCompat.isCrossbowAction(stack) || isSameActionName(stack, CrossbowCompat.CROSSBOW_ACTION));
        }
        if (SpyglassCompat.isInstalled()) {
            ACTIONS.put(SpyglassCompat.SPYGLASS_ACTION, (player, stack) ->
                    SpyglassCompat.isSpyglassAction(stack) || isSameActionName(stack, SpyglassCompat.SPYGLASS_ACTION));
        }
        if (TridentCompat.isInstalled()) {
            ACTIONS.put(TridentCompat.SPEAR_ACTION, (player, stack) ->
                    TridentCompat.isSpearAction(stack) || isSameActionName(stack, TridentCompat.SPEAR_ACTION));
        }
        for (EnumAction action : EnumAction.values()) {
            ACTIONS.putIfAbsent(action.name().toLowerCase(Locale.US), (player, stack) ->
                    isSameActionName(stack, action));
        }
    }

    private static boolean isSameActionName(ItemStack stack, EnumAction action) {
        return isSameActionName(stack, action.name());
    }

    private static boolean isSameActionName(ItemStack stack, String action) {
        return stack.getItemUseAction().name().equalsIgnoreCase(action);
    }
}
