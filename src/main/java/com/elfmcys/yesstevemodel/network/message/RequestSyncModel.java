package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSyncModel {
    public RequestSyncModel() {
    }

    public static void encode(RequestSyncModel message, PacketBuffer buf) {
    }

    public static RequestSyncModel decode(PacketBuffer buf) {
        return new RequestSyncModel();
    }

    public static void handle(RequestSyncModel message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(ClientModelManager::sendSyncModelMessage);
        }
        context.setPacketHandled(true);
    }
}
