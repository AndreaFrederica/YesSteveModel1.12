package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Locale;

public final class MolangUtils {
    private static final int MAX_DISTANCE = 5;

    public static float normalizeTime(long timestamp) {
        return ((float) (timestamp + 6000L) / 24000) % 1;
    }

    public static ResourceLocation parseResourceLocation(IContext<?> context, String value) {
        if (value == null) {
            return null;
        }
        return ResourceUtil.tryParse(value);
    }

    /**
     * 获取实体相对位置的方块 (默认从参数索引0开始)
     */
    @Nullable
    public static IBlockState getRelativeBlock(ExecutionContext<IContext<Entity>> context, Function.ArgumentCollection arguments) {
        return getRelativeBlock(context, arguments, 0);
    }

    /**
     * 获取实体相对位置的方块 (带参数偏移索引)
     * 限制范围在 5 格以内
     */
    @Nullable
    public static IBlockState getRelativeBlock(ExecutionContext<IContext<Entity>> context, Function.ArgumentCollection arguments, int indexOffset) {
        double offsetX = arguments.getAsDouble(context, indexOffset);
        double offsetY = arguments.getAsDouble(context, indexOffset + 1);
        double offsetZ = arguments.getAsDouble(context, indexOffset + 2);

        if (Math.abs(offsetX) <= MAX_DISTANCE && Math.abs(offsetY) <= MAX_DISTANCE && Math.abs(offsetZ) <= MAX_DISTANCE) {
            Entity entity = context.entity().entity();
            BlockPos pos = new BlockPos(
                    (int) Math.round(entity.posX + offsetX - 0.5D),
                    (int) Math.round(entity.posY + offsetY - 0.5D),
                    (int) Math.round(entity.posZ + offsetZ - 0.5D)
            );
            return entity.world.getBlockState(pos);
        }

        return null;
    }

    public static EntityEquipmentSlot parseSlotType(IContext<?> context, String value) {
        if (value == null) {
            return null;
        }
        try {
            return EntityEquipmentSlot.fromString(value.toLowerCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            context.debugOutput("Illegal slot type: %s.", value);
            return null;
        }
    }
}
