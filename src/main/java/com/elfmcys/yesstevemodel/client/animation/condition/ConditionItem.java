package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Map;

public abstract class ConditionItem {
    /**
     * 代表继续下一个测试，如果作为返回结果，外部不会播放该动画
     */
    protected static final String EMPTY = "";
    protected final int preSize;
    protected final String idPre;
    protected final String orePre;
    //protected final String tagPre;
    protected final String extraPre;
    protected final List<ResourceLocation> idTest = Lists.newArrayList();
    protected final List<String> oreTest = Lists.newArrayList();
    //protected final List<ResourceLocation> tagTest = Lists.newArrayList();
    protected final List<String> extraTest = Lists.newArrayList();

    public ConditionItem(String generalPre) {
        this.preSize = generalPre.length() + 1;
        this.idPre = generalPre + "$";
        this.orePre = generalPre + "~";
        //this.tagPre = generalPre + "#";
        this.extraPre = generalPre + ":";
    }

    public void addTest(String name) {
        if (name.length() <= this.preSize) {
            return;
        }
        String substring = name.substring(this.preSize);
        if (name.startsWith(this.idPre)) {
            if (ResourceUtil.isValidResourceLocation(substring)) {
                this.idTest.add(new ResourceLocation(substring));
            }
            return;
        }
        if (name.startsWith(this.orePre)) {
            if (OreDictionary.doesOreNameExist(substring)) {
                this.oreTest.add(substring);
            }
            return;
        }
//        if (name.startsWith(this.tagPre)) {
//            if (ResourceUtil.isValidResourceLocation(substring)) {
//                ResourceLocation res = new ResourceLocation(substring);
//                ITag<Item> tag = ItemTags.getAllTags().getTag(res);
//                if (tag == null) {
//                    return;
//                }
//                this.tagTest.add(res);
//            }
//            return;
//        }
        if (name.startsWith(this.extraPre)) {
            if (this.getExtras().containsKey(substring)) {
                this.extraTest.add(substring);
            }
            //return;
        }
    }

    public String doTest(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).isEmpty()) {
            return EMPTY;
        }
        String result;
        result = this.doIdTest(player, hand);
        if (!result.isEmpty()) return result;
        result = this.doOreTest(player, hand);
        if (!result.isEmpty()) return result;
//        result = this.doTagTest(player, hand);
//        if (!result.isEmpty()) return result;
        result = this.doExtraTest(player, hand);
        if (!result.isEmpty()) return result;
        return EMPTY;
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

    private String doOreTest(EntityPlayer player, EnumHand hand) {
        if (this.oreTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getHeldItem(hand);
        for (int id : OreDictionary.getOreIDs(itemInHand)) {
            String name = OreDictionary.getOreName(id);
            if ("Unknown".equals(name)) continue;
            if (this.oreTest.contains(name)) {
                return this.orePre + name;
            }
        }
        return EMPTY;
    }

    // TODO: Tag 转矿词系统，道阻且长
//    private String doTagTest(EntityPlayer player, EnumHand hand) {
//        if (this.tagTest.isEmpty()) {
//            return EMPTY;
//        }
//        Item itemInHand = player.getHeldItem(hand).getItem();
//        return this.tagTest.stream().filter(itemTagKey -> {
//            ITag<Item> tag = ItemTags.getAllTags().getTag(itemTagKey);
//            if (tag != null) {
//                return tag.contains(itemInHand);
//            }
//            return false;
//        }).findFirst().map(itemTagKey -> this.tagPre + itemTagKey).orElse(EMPTY);
//    }

    private String doExtraTest(EntityPlayer player, EnumHand hand) {
        if (this.extraTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack stack = player.getHeldItem(hand);
        Map<String, IItemStackMatcher> matchers = this.getExtras();
        for (Map.Entry<String, IItemStackMatcher> entry : matchers.entrySet()) {
            String key = entry.getKey();
            if (this.extraTest.contains(key)) {
                IItemStackMatcher matcher = entry.getValue();
                if (matcher != null && matcher.matches(player, stack)) {
                    return this.extraPre + key;
                }
            }
        }
        return EMPTY;
    }

    protected interface IItemStackMatcher {
        boolean matches(EntityPlayer player, ItemStack stack);
    }

    protected abstract Map<String, IItemStackMatcher> getExtras();
}
