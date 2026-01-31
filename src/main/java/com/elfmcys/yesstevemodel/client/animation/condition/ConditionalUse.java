package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConditionalUse {
    private static final String EMPTY = "";
    private final int preSize;
    private final String idPre;
    //private final String tagPre;
    private final String extraPre;
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    private final List<ResourceLocation> tagTest = Lists.newArrayList();
    // UseAction - EnumAction
    private final List<EnumAction> extraTest = Lists.newArrayList();

    public ConditionalUse(EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            idPre = "use_mainhand$";
            //tagPre = "use_mainhand#";
            extraPre = "use_mainhand:";
            preSize = 13;
        } else {
            idPre = "use_offhand$";
            //tagPre = "use_offhand#";
            extraPre = "use_offhand:";
            preSize = 12;
        }
    }

    public void addTest(String name) {
        if (name.length() <= preSize) {
            return;
        }
        String substring = name.substring(preSize);
        if (name.startsWith(idPre) && ResourceUtil.isValidResourceLocation(substring)) {
            idTest.add(new ResourceLocation(substring));
        }
//        if (name.startsWith(tagPre) && ResourceUtil.isValidResourceLocation(substring)) {
//            ResourceLocation res = new ResourceLocation(substring);
//            ITag<Item> tag = ItemTags.getAllTags().getTag(res);
//            if (tag == null) {
//                return;
//            }
//            tagTest.add(res);
//        }
        if (name.startsWith(extraPre)) {
            if (substring.equals(EnumAction.NONE.name().toLowerCase(Locale.US))) {
                return;
            }
            Arrays.stream(EnumAction.values()).filter(a -> a.name().toLowerCase(Locale.US).equals(substring)).findFirst().ifPresent(extraTest::add);
        }
    }

    public String doTest(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).isEmpty()) {
            return EMPTY;
        }
        String result = doIdTest(player, hand);
        if (result.isEmpty()) {
            //result = doTagTest(player, hand);
            if (result.isEmpty()) {
                return doExtraTest(player, hand);
            }
            return result;
        }
        return result;
    }

    private String doIdTest(EntityPlayer player, EnumHand hand) {
        if (idTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getHeldItem(hand);
        ResourceLocation registryName = itemInHand.getItem().getRegistryName();
        if (registryName == null) {
            return EMPTY;
        }
        if (idTest.contains(registryName)) {
            return idPre + registryName;
        }
        return EMPTY;
    }

//    private String doTagTest(EntityPlayer player, EnumHand hand) {
//        if (tagTest.isEmpty()) {
//            return EMPTY;
//        }
//        Item itemInHand = player.getHeldItem(hand).getItem();
//        return tagTest.stream().filter(itemTagKey -> {
//            ITag<Item> tag = ItemTags.getAllTags().getTag(itemTagKey);
//            if (tag != null) {
//                return tag.contains(itemInHand);
//            }
//            return false;
//        }).findFirst().map(itemTagKey -> tagPre + itemTagKey).orElse(EMPTY);
//    }

    private String doExtraTest(EntityPlayer player, EnumHand hand) {
        if (extraTest.isEmpty()) {
            return EMPTY;
        }
        EnumAction anim = player.getHeldItem(hand).getItemUseAction();
        if (this.extraTest.contains(anim)) {
            return extraPre + anim.name().toLowerCase(Locale.US);
        }
        return EMPTY;
    }
}
