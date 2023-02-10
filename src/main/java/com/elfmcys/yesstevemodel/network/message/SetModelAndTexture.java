package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.capability.AuthModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetModelAndTexture {
    private final ResourceLocation modelId;
    private final ResourceLocation selectTexture;

    public SetModelAndTexture(ResourceLocation modelId, ResourceLocation selectTexture) {
        this.modelId = modelId;
        this.selectTexture = selectTexture;
    }

    public static void encode(SetModelAndTexture message, PacketBuffer buf) {
        buf.writeResourceLocation(message.modelId);
        buf.writeResourceLocation(message.selectTexture);
    }

    public static SetModelAndTexture decode(PacketBuffer buf) {
        return new SetModelAndTexture(buf.readResourceLocation(), buf.readResourceLocation());
    }

    public static void handle(SetModelAndTexture message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            context.enqueueWork(() -> {
                ServerPlayerEntity sender = context.getSender();
                if (sender == null) {
                    return;
                }
                handleCapability(message, sender);
            });
        }
        context.setPacketHandled(true);
    }

    private static void handleCapability(SetModelAndTexture message, ServerPlayerEntity sender) {
        sender.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelIdCap -> sender.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(ownModelsCap -> {
            if (!ServerModelManager.AUTH_MODELS.contains(message.modelId.getPath()) || ownModelsCap.containModel(message.modelId)) {
                modelIdCap.setModelAndTexture(message.modelId, message.selectTexture);
            }
        }));
    }
}
