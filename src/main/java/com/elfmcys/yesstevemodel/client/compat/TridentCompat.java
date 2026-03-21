package com.elfmcys.yesstevemodel.client.compat;

import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.sirsquidly.oe.capabilities.CapabilityRiptide;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.trident.util.EntityHelper;
import net.minecraftforge.fml.common.Optional;

import java.util.concurrent.atomic.AtomicBoolean;

public class TridentCompat {
    public static boolean hasRiptide() {
        return Mods.OE_INSTALLED || Mods.TM_INSTALLED;
    }

    public static boolean isAutoSpinAttack(EntityPlayer player) {
        return (Mods.OE_INSTALLED && isOEAttack(player)) ||
                (Mods.TM_INSTALLED && isTMAttack(player));
    }

    @Optional.Method(modid = Mods.OCEANIC_EXPANSE)
    private static boolean isOEAttack(EntityPlayer player) {
        AtomicBoolean flag = new AtomicBoolean(false);
        CapabilityEvent.getCapability(player, CapabilityRiptide.RIPTIDE_CAP).ifPresent(cap -> {
            flag.set(cap.getRiptideAnimate());
        });
        return flag.get();
    }

    @Optional.Method(modid = Mods.TRIDENT_MOD)
    private static boolean isTMAttack(EntityPlayer player) {
        return EntityHelper.isSpinAttacking(player);
    }
}
