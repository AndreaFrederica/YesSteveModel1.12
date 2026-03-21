package com.elfmcys.yesstevemodel.client.compat;

import mod.acgaming.universaltweaks.config.UTConfigTweaks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Optional;

/**
 * {@link com.elfmcys.yesstevemodel.util.EntityUtil#getCameraPosition(Minecraft, float)} 的兼容
 */
public class CameraCompat {
    public static boolean bypassesNonSolidBlocks() {
        return Mods.UT_INSTALLED && utCamaraBypassesNonSolidBlocks();
    }

    @Optional.Method(modid = Mods.UNIVERSAL_TWEAKS)
    private static boolean utCamaraBypassesNonSolidBlocks() {
        return UTConfigTweaks.ENTITIES.utThirdPersonIgnoresNonSolidBlocks;
    }
}
