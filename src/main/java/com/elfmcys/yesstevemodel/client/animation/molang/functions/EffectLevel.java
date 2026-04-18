package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.ContextFunction;
import com.elfmcys.yesstevemodel.geckolib3.util.MolangUtils;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EffectLevel extends ContextFunction<Entity> {
    @Override
    public boolean validateArgumentSize(int size) {
        return size == 1;
    }

    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> context, Function.ArgumentCollection arguments) {
        ResourceLocation effectId = MolangUtils.parseResourceLocation(context.entity(), arguments.getAsString(context, 0));
        if (effectId == null) {
            return null;
        }

        Potion effect = ForgeRegistries.POTIONS.getValue(effectId);
        if (effect == null) {
            return 0;
        }

        if (context.entity().entity() instanceof EntityLivingBase living) {
            PotionEffect instance = living.getActivePotionEffect(effect);
            if (instance != null) {
                return instance.getAmplifier() + 1;
            }
        } else {
            return null;
        }

        return 0;
    }
}
