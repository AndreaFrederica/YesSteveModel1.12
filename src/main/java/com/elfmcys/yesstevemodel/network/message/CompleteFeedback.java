package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CompleteFeedback {
    public CompleteFeedback() {
    }

    public static void encode(CompleteFeedback message, PacketBuffer buf) {
    }

    public static CompleteFeedback decode(PacketBuffer buf) {
        return new CompleteFeedback();
    }

    public static void handle(CompleteFeedback message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(UploadManager::finishUpload);
        }
        context.setPacketHandled(true);
    }
}
