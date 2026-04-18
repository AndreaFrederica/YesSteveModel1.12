package com.elfmcys.yesstevemodel.geckolib3.core.molang.binding;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.IValueEvaluator;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.LambdaVariable;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block.BlockStateVariable;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.block.BlockVariable;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.entity.*;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.item.ItemStackVariable;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.variable.item.ItemVariable;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ObjectBinding;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ContextBinding implements ObjectBinding {
    private final Object2ReferenceOpenHashMap<String, Object> bindings = new Object2ReferenceOpenHashMap<>();

    @Override
    public Object getProperty(String name) {
        return this.bindings.get(name);
    }

    public void function(String name, Function function) {
        this.bindings.put(name, function);
    }

    public void constValue(String name, Object value) {
        this.bindings.put(name, value);
    }

    public void var(String name, IValueEvaluator<?, IContext<Object>> evaluator) {
        this.bindings.put(name, new LambdaVariable<>(evaluator));
    }

    public void entityVar(String name, IValueEvaluator<?, IContext<Entity>> evaluator) {
        this.bindings.put(name, new EntityVariable(evaluator));
    }

    public void livingEntityVar(String name, IValueEvaluator<?, IContext<EntityLivingBase>> evaluator) {
        this.bindings.put(name, new LivingEntityVariable(evaluator));
    }

    public void mobEntityVar(String name, IValueEvaluator<?, IContext<EntityLiving>> evaluator) {
        this.bindings.put(name, new MobEntityVariable(evaluator));
    }

    public void tamableEntityVar(String name, IValueEvaluator<?, IContext<EntityTameable>> evaluator) {
        this.bindings.put(name, new TamableEntityVariable(evaluator));
    }

    public void playerEntityVar(String name, IValueEvaluator<?, IContext<EntityPlayer>> evaluator) {
        this.bindings.put(name, new PlayerEntityVariable(evaluator));
    }

    public void abstractClientPlayerVar(String name, IValueEvaluator<?, IContext<AbstractClientPlayer>> evaluator) {
        this.bindings.put(name, new AbstractClientPlayerVariable(evaluator));
    }

    public void localPlayerEntityVar(String name, IValueEvaluator<?, IContext<EntityPlayerSP>> evaluator) {
        this.bindings.put(name, new LocalPlayerEntityVariable(evaluator));
    }

    public void throwableEntityVar(String name, IValueEvaluator<?, IContext<EntityThrowable>> evaluator) {
        this.bindings.put(name, new ThrowableEntityVariable(evaluator));
    }

    public void fishingHookEntityVar(String name, IValueEvaluator<?, IContext<EntityFishHook>> evaluator) {
        this.bindings.put(name, new FishingHookEntityVariable(evaluator));
    }

    public void arrowEntityVar(String name, IValueEvaluator<?, IContext<EntityArrow>> evaluator) {
        this.bindings.put(name, new ArrowEntityVariable(evaluator));
    }

    public void itemVar(String name, IValueEvaluator<?, IContext<Item>> evaluator) {
        this.bindings.put(name, new ItemVariable(evaluator));
    }

    public void itemStackVar(String name, IValueEvaluator<?, IContext<ItemStack>> evaluator) {
        this.bindings.put(name, new ItemStackVariable(evaluator));
    }

    public void blockStateVar(String name, IValueEvaluator<?, IContext<IBlockState>> evaluator) {
        this.bindings.put(name, new BlockStateVariable(evaluator));
    }

    public void blockVar(String name, IValueEvaluator<?, IContext<Block>> evaluator) {
        this.bindings.put(name, new BlockVariable(evaluator));
    }
}
