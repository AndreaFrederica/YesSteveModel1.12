package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionArmor {
    private static final Pattern ID_PRE_REG = Pattern.compile("^(.+?)\\$(.*?)$");
    private static final Pattern TAG_PRE_REG = Pattern.compile("^(.+?)#(.*?)$");
    private static final String EMPTY = "";

    private final Map<EntityEquipmentSlot, List<ResourceLocation>> idTest = Maps.newHashMap();
    private final Map<EntityEquipmentSlot, List<String>> tagTest = Maps.newHashMap();

    public void addTest(String name) {
        Matcher matcherId = ID_PRE_REG.matcher(name);
        if (matcherId.find()) {
            EntityEquipmentSlot type = getType(matcherId.group(1));
            if (type == null) {
                return;
            }
            String id = matcherId.group(2);
            if (!ResourceUtil.isValidResourceLocation(id)) {
                return;
            }
            ResourceLocation res = new ResourceLocation(id);
            if (this.idTest.containsKey(type)) {
                this.idTest.get(type).add(res);
            } else {
                this.idTest.put(type, Lists.newArrayList(res));
            }
        }

        Matcher matcherTag = TAG_PRE_REG.matcher(name);
        if (matcherTag.find()) {
            EntityEquipmentSlot type = getType(matcherTag.group(1));
            if (type == null) {
                return;
            }
            String id = matcherTag.group(2);
            if (!ResourceUtil.isValidResourceLocation(id)) {
                return;
            }
            if (OreDictionary.getOres(id, false).isEmpty()) {
                return;
            }
            if (tagTest.containsKey(type)) {
                tagTest.get(type).add(id);
            } else {
                tagTest.put(type, Lists.newArrayList(id));
            }
        }
    }

    public String doTest(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack item = player.getItemStackFromSlot(slot);
        if (item.isEmpty()) {
            return EMPTY;
        }
        String result = this.doIdTest(player, slot);
        if (result.isEmpty()) {
            return doTagTest(player, slot);
        }
        return result;
    }

    private String doIdTest(EntityPlayer player, EntityEquipmentSlot slot) {
        if (this.idTest.isEmpty()) {
            return EMPTY;
        }
        if (!this.idTest.containsKey(slot) || this.idTest.get(slot).isEmpty()) {
            return EMPTY;
        }
        List<ResourceLocation> idListTest = this.idTest.get(slot);
        ItemStack item = player.getItemStackFromSlot(slot);
        ResourceLocation registryName = item.getItem().getRegistryName();
        if (registryName == null) {
            return EMPTY;
        }
        if (idListTest.contains(registryName)) {
            return slot.getName() + "$" + registryName;
        }
        return EMPTY;
    }

    private String doTagTest(EntityPlayer player, EntityEquipmentSlot slot) {
        if (tagTest.isEmpty()) {
            return EMPTY;
        }
        if (!tagTest.containsKey(slot) || tagTest.get(slot).isEmpty()) {
            return EMPTY;
        }
        List<String> tagListTest = tagTest.get(slot);
        ItemStack item = player.getItemStackFromSlot(slot);
        return tagListTest.stream().filter(itemTagKey -> {
            int[] oreIDs = OreDictionary.getOreIDs(item);
            if (oreIDs.length != 0) {
                return Arrays.stream(oreIDs).anyMatch(tagPre -> tagListTest.contains(OreDictionary.getOreName(tagPre)));
            }
            return false;
        }).findFirst().map(itemTagKey -> slot.getName() + "#" + itemTagKey).orElse(EMPTY);
    }


    @Nullable
    public static EntityEquipmentSlot getType(String type) {
        for (EntityEquipmentSlot slotType : EntityEquipmentSlot.values()) {
            if (slotType.getName().equals(type)) {
                return slotType;
            }
        }
        return null;
    }
}
