package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetPlayAnimation {
    private static final int STOP = -1;
    private final int extraAnimationId;

    public SetPlayAnimation(int extraAnimationId) {
        this.extraAnimationId = extraAnimationId;
    }

    public static SetPlayAnimation stop() {
        return new SetPlayAnimation(STOP);
    }

    public static void encode(SetPlayAnimation message, PacketBuffer buf) {
        buf.writeInt(message.extraAnimationId);
    }

    public static SetPlayAnimation decode(PacketBuffer buf) {
        return new SetPlayAnimation(buf.readInt());
    }

    public static void handle(SetPlayAnimation message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayerEntity sender = context.getSender();
                if (sender == null) {
                    return;
                }
                if (STOP <= message.extraAnimationId && message.extraAnimationId < 8) {
                    handleCapability(message, sender);
                }
            });
        }
        context.setPacketHandled(true);
    }

    private static void handleCapability(SetPlayAnimation message, ServerPlayerEntity sender) {
        sender.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelIdCap -> {
            if (message.extraAnimationId == STOP) {
                modelIdCap.stopAnimation();
            } else {
                modelIdCap.playAnimation("extra" + message.extraAnimationId);
            }
        });
    }
}
