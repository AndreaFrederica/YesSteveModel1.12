package com.elfmcys.yesstevemodel.client.animation.condition;

import net.minecraft.item.*;
import net.minecraft.util.EnumHand;

import java.util.HashMap;
import java.util.Map;

public class ConditionalSwing extends ConditionItem {
    private static final Map<String, IItemStackMatcher> TOOL_TYPES = new HashMap<>();

    public ConditionalSwing(EnumHand hand) {
        super(hand == EnumHand.MAIN_HAND ? "swing" : "swing_offhand");
    }

    @Override
    protected Map<String, IItemStackMatcher> getExtras() {
        initToolTypes();
        return TOOL_TYPES;
    }

    private static void initToolTypes() {
        if (!TOOL_TYPES.isEmpty()) return;

        TOOL_TYPES.put("sword", (player, stack) ->
                stack.getItem() instanceof ItemSword);
        TOOL_TYPES.put("axe", (player, stack) ->
                stack.getItem() instanceof ItemAxe);
        TOOL_TYPES.put("pickaxe", (player, stack) ->
                stack.getItem() instanceof ItemPickaxe);
        TOOL_TYPES.put("shovel", (player, stack) ->
                stack.getItem() instanceof ItemSpade);
        TOOL_TYPES.put("hoe", (player, stack) ->
                stack.getItem() instanceof ItemHoe);
        TOOL_TYPES.put("shield", (player, stack) ->
                stack.getItem() instanceof ItemShield);
        TOOL_TYPES.put("throwable_potion", (player, stack) ->
                stack.getItem() instanceof ItemSplashPotion || stack.getItem() instanceof ItemLingeringPotion);
        TOOL_TYPES.put("fishing_rod", (player, stack) ->
                stack.getItem() instanceof ItemFishingRod);
    }
}
