package com.elfmcys.yesstevemodel.client.animation.condition;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConditionalUse {
    private static final String EMPTY = "";
    private final int preSize;
    private final String idPre;
    private final String tagPre;
    private final String extraPre;
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    private final List<ResourceLocation> tagTest = Lists.newArrayList();
    private final List<UseAction> extraTest = Lists.newArrayList();

    public ConditionalUse(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            idPre = "use_mainhand$";
            tagPre = "use_mainhand#";
            extraPre = "use_mainhand:";
            preSize = 13;
        } else {
            idPre = "use_offhand$";
            tagPre = "use_offhand#";
            extraPre = "use_offhand:";
            preSize = 12;
        }
    }

    public void addTest(String name) {
        if (name.length() <= preSize) {
            return;
        }
        String substring = name.substring(preSize);
        if (name.startsWith(idPre) && ResourceLocation.isValidResourceLocation(substring)) {
            idTest.add(new ResourceLocation(substring));
        }
        if (name.startsWith(tagPre) && ResourceLocation.isValidResourceLocation(substring)) {
            ResourceLocation res = new ResourceLocation(substring);
            ITag<Item> tag = ItemTags.getAllTags().getTag(res);
            if (tag == null) {
                return;
            }
            tagTest.add(res);
        }
        if (name.startsWith(extraPre)) {
            if (substring.equals(UseAction.NONE.name().toLowerCase(Locale.US))) {
                return;
            }
            Arrays.stream(UseAction.values()).filter(a -> a.name().toLowerCase(Locale.US).equals(substring)).findFirst().ifPresent(extraTest::add);
        }
    }

    public String doTest(PlayerEntity player, Hand hand) {
        if (player.getItemInHand(hand).isEmpty()) {
            return EMPTY;
        }
        String result = doIdTest(player, hand);
        if (result.isEmpty()) {
            result = doTagTest(player, hand);
            if (result.isEmpty()) {
                return doExtraTest(player, hand);
            }
            return result;
        }
        return result;
    }

    private String doIdTest(PlayerEntity player, Hand hand) {
        if (idTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        ResourceLocation registryName = itemInHand.getItem().getRegistryName();
        if (registryName == null) {
            return EMPTY;
        }
        if (idTest.contains(registryName)) {
            return idPre + registryName;
        }
        return EMPTY;
    }

    private String doTagTest(PlayerEntity player, Hand hand) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        Item itemInHand = player.getItemInHand(hand).getItem();
        return tagTest.stream().filter(itemTagKey -> {
            ITag<Item> tag = ItemTags.getAllTags().getTag(itemTagKey);
            if (tag != null) {
                return tag.contains(itemInHand);
            }
            return false;
        }).findFirst().map(itemTagKey -> tagPre + itemTagKey).orElse(EMPTY);
    }

    private String doExtraTest(PlayerEntity player, Hand hand) {
        if (extraTest.isEmpty()) {
            return EMPTY;
        }
        UseAction anim = player.getItemInHand(hand).getUseAnimation();
        if (this.extraTest.contains(anim)) {
            return extraPre + anim.name().toLowerCase(Locale.US);
        }
        return EMPTY;
    }
}
