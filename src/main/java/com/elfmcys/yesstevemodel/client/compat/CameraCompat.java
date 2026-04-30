package com.elfmcys.yesstevemodel.client.compat;

import com.elfmcys.yesstevemodel.client.util.EntityUtil;
import mod.acgaming.universaltweaks.config.UTConfigBugfixes;
import mod.acgaming.universaltweaks.config.UTConfigTweaks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Optional;

/**
 * {@link EntityUtil#getCameraPosition(Minecraft, float)} 的兼容
 */
public class CameraCompat {
    public static boolean thirdPersonIgnoresNonSolidBlocks() {
        return Mods.UT_INSTALLED && utThirdPersonIgnoresNonSolidBlocks();
    }

    @Optional.Method(modid = Mods.UNIVERSAL_TWEAKS)
    private static boolean utThirdPersonIgnoresNonSolidBlocks() {
        return UTConfigTweaks.ENTITIES.utThirdPersonIgnoresNonSolidBlocks;
    }

    public static boolean cameraOrientation() {
        return Mods.UT_INSTALLED && utCameraOrientation();
    }

    @Optional.Method(modid = Mods.UNIVERSAL_TWEAKS)
    private static boolean utCameraOrientation() {
        return UTConfigBugfixes.MISC.utCameraOrientation;
    }
}
