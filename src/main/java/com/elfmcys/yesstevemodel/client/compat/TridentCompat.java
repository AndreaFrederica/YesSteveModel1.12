package com.elfmcys.yesstevemodel.client.compat;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.sirsquidly.oe.capabilities.CapabilityRiptide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.trident.util.EntityHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import java.util.concurrent.atomic.AtomicBoolean;

public class TridentCompat {
    private static final String OCEANIC_EXPANSE = "oe";
    private static boolean OE_INSTALLED = false;
    private static final String TRIDENT_MOD = "trident";
    private static boolean TM_INSTALLED = false;

    public static void init() {
        OE_INSTALLED = Loader.isModLoaded(OCEANIC_EXPANSE);
        TM_INSTALLED = Loader.isModLoaded(TRIDENT_MOD);
    }

    public static boolean isInstalled() {
        return OE_INSTALLED || TM_INSTALLED;
    }

    public static boolean isAutoSpinAttack(EntityPlayer player) {
        return (OE_INSTALLED && isOEAttack(player)) ||
                (TM_INSTALLED && isTMAttack(player));
    }

    @Optional.Method(modid = OCEANIC_EXPANSE)
    private static boolean isOEAttack(EntityPlayer player) {
        AtomicBoolean flag = new AtomicBoolean(false);
        CapabilityEvent.getCapability(player, CapabilityRiptide.RIPTIDE_CAP).ifPresent(cap -> {
            flag.set(cap.getRiptideAnimate());
        });
        return flag.get();
    }

    @Optional.Method(modid = TRIDENT_MOD)
    private static boolean isTMAttack(EntityPlayer player) {
        return EntityHelper.isSpinAttacking(player);
    }
}
