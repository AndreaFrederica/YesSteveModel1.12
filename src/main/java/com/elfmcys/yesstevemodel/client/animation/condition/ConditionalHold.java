package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.List;

public class ConditionalHold {
    private static final String EMPTY = "";
    private final int preSize;
    private final String idPre;
    private final String tagPre;
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    private final List<String> tagTest = Lists.newArrayList();

    public ConditionalHold(EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            this.idPre = "hold_mainhand$";
            tagPre = "hold_mainhand#";
            this.preSize = 14;
        } else {
            this.idPre = "hold_offhand$";
            tagPre = "hold_offhand#";
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
        if (name.startsWith(tagPre) && ResourceUtil.isValidResourceLocation(substring)) {
            if (OreDictionary.getOres(substring,false).isEmpty()){
                return;
            }
            tagTest.add(substring);
        }
    }

    public String doTest(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).isEmpty()) {
            return EMPTY;
        }
        String result = this.doIdTest(player, hand);
        if (result.isEmpty()) {
            return doTagTest(player, hand);
        }
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

    private String doTagTest(EntityPlayer player, EnumHand hand) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getHeldItem(hand);
        return tagTest.stream().filter(itemTagKey -> {
            int[] oreIDs = OreDictionary.getOreIDs(itemInHand);
            if (oreIDs.length != 0) {
                return Arrays.stream(oreIDs).anyMatch(tagPre -> tagTest.contains(OreDictionary.getOreName(tagPre)));
            }
            return false;
        }).findFirst().map(itemTagKey -> tagPre + itemTagKey).orElse(EMPTY);
    }
}
