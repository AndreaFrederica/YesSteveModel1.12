package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;

public class LivingEntityFrameState<T extends EntityLivingBase> extends EntityFrameStateTracker<T> {

    private final ItemStack[] handItems = {ItemStack.EMPTY, ItemStack.EMPTY};

    public LivingEntityFrameState(@Nullable T entity) {
        super(entity);
    }

    public ItemStack getHandItemsForAnimation(EnumHand hand) {
        return this.handItems[hand.ordinal()];
    }

    public void setHandItemsForAnimation(ItemStack stack, EnumHand hand) {
        this.handItems[hand.ordinal()] = stack.copy();
    }

    @Nullable
    public T getEntity() {
        return this.entity;
    }
}
