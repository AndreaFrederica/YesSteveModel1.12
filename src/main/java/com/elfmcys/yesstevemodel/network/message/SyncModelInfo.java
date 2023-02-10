package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.util.ThreadTools;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncModelInfo {
    private final int entityId;
    private final ModelInfoCapability capability;

    public SyncModelInfo(int entityId, ModelInfoCapability capability) {
        this.entityId = entityId;
        this.capability = capability;
    }

    public static void encode(SyncModelInfo message, PacketBuffer buf) {
        buf.writeVarInt(message.entityId);
        buf.writeNbt(message.capability.serializeNBT());
    }

    public static SyncModelInfo decode(PacketBuffer buf) {
        int entityId = buf.readVarInt();
        CompoundNBT compoundTag = buf.readNbt();
        ModelInfoCapability cap = new ModelInfoCapability();
        if (compoundTag != null) {
            cap.deserializeNBT(compoundTag);
        }
        return new SyncModelInfo(entityId, cap);
    }

    public static void handle(SyncModelInfo message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleCapability(message));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleCapability(SyncModelInfo message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            ThreadTools.THREAD_POOL.submit(() -> {
                        try {
                            int time = 0;
                            while (mc.level.getEntity(message.entityId) == null && time < 5) {
                                Thread.sleep(500);
                                time++;
                            }
                            Entity entity = mc.level.getEntity(message.entityId);
                            if (entity instanceof PlayerEntity) {
                                PlayerEntity player = (PlayerEntity) entity;
                                player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> cap.copyFrom(message.capability));
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
    }
}
