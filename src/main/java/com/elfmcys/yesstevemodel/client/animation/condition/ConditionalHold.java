package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class ConditionalHold {
    private static final String EMPTY = "";
    private final int preSize;
    private final String idPre;
    //private final String tagPre;
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    //private final List<ResourceLocation> tagTest = Lists.newArrayList();

    public ConditionalHold(EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            this.idPre = "hold_mainhand$";
            //tagPre = "hold_mainhand#";
            this.preSize = 14;
        } else {
            this.idPre = "hold_offhand$";
            //tagPre = "hold_offhand#";
            this.preSize = 13;
        }
    }

    public void addTest(String name) {
        if (name.length() <= this.preSize) {
            return;
        }
        String substring = name.substring(this.preSize);
        if (name.startsWith(this.idPre) && ResourceUtil.isValidResourceLocation(substring)) {
            this.idTest.add(new ResourceLocation(substring));
        }
        // TODO: Tag 转矿词系统，道阻且长
//        if (name.startsWith(tagPre) && ResourceUtil.isValidResourceLocation(substring)) {
//            ResourceLocation res = new ResourceLocation(substring);
//            ITag<Item> tag = ItemTags.getAllTags().getTag(res);
//            if (tag == null) {
//                return;
//            }
//            tagTest.add(res);
//        }
    }

    public String doTest(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).isEmpty()) {
            return EMPTY;
        }
        String result = this.doIdTest(player, hand);
//        if (result.isEmpty()) {
//            return doTagTest(player, hand);
//        }
        return result;
    }

    private String doIdTest(EntityPlayer player, EnumHand hand) {
        if (this.idTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getHeldItem(hand);
        ResourceLocation registryName = itemInHand.getItem().getRegistryName();
        if (registryName == null) {
            return EMPTY;
        }
        if (this.idTest.contains(registryName)) {
            return this.idPre + registryName;
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
}
