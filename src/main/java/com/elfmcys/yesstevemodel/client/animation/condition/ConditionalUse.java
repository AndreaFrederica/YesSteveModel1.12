package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.compat.SpyglassCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ConditionalUse {
    private static final Set<String> ACTIONS = new HashSet<>();
    private static final String EMPTY = "";
    private final int preSize;
    private final String idPre;
    //private final String tagPre;
    private final String extraPre;
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    //private final List<ResourceLocation> tagTest = Lists.newArrayList();
    private final List<String> extraTest = Lists.newArrayList();

    public ConditionalUse(EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            this.idPre = "use_mainhand$";
            //tagPre = "use_mainhand#";
            this.extraPre = "use_mainhand:";
            this.preSize = 13;
        } else {
            this.idPre = "use_offhand$";
            //tagPre = "use_offhand#";
            this.extraPre = "use_offhand:";
            this.preSize = 12;
        }
    }

    public void addTest(String name) {
        initActions();
        if (name.length() <= this.preSize) {
            return;
        }
        String substring = name.substring(this.preSize);
        if (name.startsWith(this.idPre) && ResourceUtil.isValidResourceLocation(substring)) {
            this.idTest.add(new ResourceLocation(substring));
        }
//        if (name.startsWith(tagPre) && ResourceUtil.isValidResourceLocation(substring)) {
//            ResourceLocation res = new ResourceLocation(substring);
//            ITag<Item> tag = ItemTags.getAllTags().getTag(res);
//            if (tag == null) {
//                return;
//            }
//            tagTest.add(res);
//        }
        if (name.startsWith(this.extraPre)) {
            if (substring.equals(EnumAction.NONE.name().toLowerCase(Locale.US))) {
                return;
            }
            ACTIONS.stream().filter(s -> s.equals(substring)).findFirst().ifPresent(this.extraTest::add);
        }
    }

    public String doTest(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).isEmpty()) {
            return EMPTY;
        }
        String result = this.doIdTest(player, hand);
        if (result.isEmpty()) {
            //result = doTagTest(player, hand);
            if (result.isEmpty()) {
                return this.doExtraTest(player, hand);
            }
            return result;
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
        if (this.extraTest.isEmpty()) {
            return EMPTY;
        }
        String anim = getAction(player.getHeldItem(hand));
        if (this.extraTest.contains(anim)) {
            return this.extraPre + anim;
        }
        return EMPTY;
    }

    private static String getAction(ItemStack stack) {
        if (CrossbowCompat.isCrossbowAction(stack)) return CrossbowCompat.CROSSBOW_ACTION;
        else if (SpyglassCompat.isSpyglassAction(stack)) return SpyglassCompat.SPYGLASS_ACTION;
        else if (TridentCompat.isSpearAction(stack)) return TridentCompat.SPEAR_ACTION;
        else return stack.getItemUseAction().name().toLowerCase(Locale.US);
    }

    private static void initActions() {
        if (!ACTIONS.isEmpty()) return;
        // 尽量晚地初始化它，这样可以获取到最多的 EnumAction
        for (EnumAction action : EnumAction.values()) {
            ACTIONS.add(action.name().toLowerCase(Locale.US));
        }
        if (CrossbowCompat.isInstalled()) ACTIONS.add(CrossbowCompat.CROSSBOW_ACTION);
        if (SpyglassCompat.isInstalled()) ACTIONS.add(SpyglassCompat.SPYGLASS_ACTION);
        if (TridentCompat.isInstalled()) ACTIONS.add(TridentCompat.SPEAR_ACTION);
    }
}
