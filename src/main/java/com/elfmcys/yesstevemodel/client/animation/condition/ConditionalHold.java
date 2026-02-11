package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.compat.SpyglassCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;

import java.util.LinkedHashMap;
import java.util.Locale;

public class ConditionalHold extends ConditionItem {
    /**
     * 代表一种特殊的动画，空手时应用，要求为空，用于刷新动画<br>
     * 根据 YSM Wiki，只有手持有这个
     */
    private static final String EMPTY_ANIM = "empty";
    private static final LinkedHashMap<String, IItemStackMatcher> EXTRAS = new LinkedHashMap<>();

    public ConditionalHold(EnumHand hand) {
        super("hold_" + (hand == EnumHand.MAIN_HAND ? "mainhand" : "offhand"));
    }

    /**
     * @return 不会返回 {@code ""}，而是返回 {@link #EMPTY_ANIM}，以刷新动画
     */
    @Override
    public String doTest(EntityPlayer player, EnumHand hand) {
        String anim = super.doTest(player, hand);
        return anim.isEmpty() ? this.extraPre + EMPTY_ANIM : anim;
    }

    @Override
    protected LinkedHashMap<String, IItemStackMatcher> getExtras() {
        initToolTypes();
        return EXTRAS;
    }

    private static void initToolTypes() {
        if (!EXTRAS.isEmpty()) return;

        // 越往上优先级越高
        if (CrossbowCompat.isInstalled()) {
            EXTRAS.put("charged_crossbow", (player, stack) ->
                    CrossbowCompat.isCharged(stack));
            EXTRAS.put("crossbow", (player, stack) ->
                    CrossbowCompat.isCrossbowAction(stack) || isSameActionName(stack, CrossbowCompat.CROSSBOW_ACTION));
        }
        EXTRAS.put("fishing", (player, stack) ->
                player.fishEntity != null);
        EXTRAS.put("fishing_rod", (player, stack) ->
                stack.getItem() instanceof ItemFishingRod);
        EXTRAS.put("sword", (player, stack) ->
                stack.getItem() instanceof ItemSword);
        EXTRAS.put("axe", (player, stack) ->
                stack.getItem() instanceof ItemAxe);
        EXTRAS.put("pickaxe", (player, stack) ->
                stack.getItem() instanceof ItemPickaxe);
        EXTRAS.put("shovel", (player, stack) ->
                stack.getItem() instanceof ItemSpade);
        EXTRAS.put("hoe", (player, stack) ->
                stack.getItem() instanceof ItemHoe);
        EXTRAS.put("shield", (player, stack) ->
                stack.getItem() instanceof ItemShield || isSameActionName(stack, "shield"));
        EXTRAS.put("throwable_potion", (player, stack) ->
                stack.getItem() instanceof ItemSplashPotion || stack.getItem() instanceof ItemLingeringPotion);
        if (TridentCompat.isInstalled()) {
            EXTRAS.put(TridentCompat.SPEAR_ACTION, (player, stack) ->
                    TridentCompat.isSpearAction(stack) || isSameActionName(stack, TridentCompat.SPEAR_ACTION));
        }
        if (SpyglassCompat.isInstalled()) {
            EXTRAS.put(SpyglassCompat.SPYGLASS_ACTION, (player, stack) ->
                    SpyglassCompat.isSpyglassAction(stack) || isSameActionName(stack, SpyglassCompat.SPYGLASS_ACTION));
        }
        for (EnumAction action : EnumAction.values()) {
            // YSM Wiki 没有这两个
            if (action == EnumAction.NONE || action == EnumAction.BLOCK) continue;
            if (action == EnumAction.BOW) {
                EXTRAS.put(action.name().toLowerCase(Locale.US), (player, stack) ->
                        stack.getItem() instanceof ItemBow || isSameActionName(stack, action));
            }
            EXTRAS.putIfAbsent(action.name().toLowerCase(Locale.US), (player, stack) ->
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
