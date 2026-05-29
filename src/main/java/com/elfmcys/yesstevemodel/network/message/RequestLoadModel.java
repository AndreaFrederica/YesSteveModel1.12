package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelInfo;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.resource.YSMBinaryDeserializer;
import com.elfmcys.yesstevemodel.resource.YSMClientMapper;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import com.elfmcys.yesstevemodel.util.ByteInteger;
import com.elfmcys.yesstevemodel.util.ThreadTools;
import com.elfmcys.yesstevemodel.util.UuidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.UUID;

import rip.ysm.security.YSMByteBuf;
import rip.ysm.security.YsmCrypt;

public class RequestLoadModel implements IPacketBufferMessage {
    private String fileName;
    private String modelId;
    private boolean auth;

    public RequestLoadModel() {
    }

    public RequestLoadModel(String fileName) {
        this(fileName, null, false);
    }

    public RequestLoadModel(String fileName, @Nullable String modelId) {
        this(fileName, modelId, false);
    }

    public RequestLoadModel(String fileName, @Nullable String modelId, boolean auth) {
        this.fileName = fileName;
        this.modelId = modelId;
        this.auth = auth;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeString(this.fileName);
        buf.writeString(this.modelId == null ? "" : this.modelId);
        buf.writeBoolean(this.auth);
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        this.fileName = buf.readString(Short.MAX_VALUE);
        this.modelId = buf.readString(Short.MAX_VALUE);
        this.auth = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<RequestLoadModel, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(RequestLoadModel message, MessageContext ctx) {
            if (ctx.side.isClient()) {
                handleClient(message);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private static void handleClient(RequestLoadModel message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                ClientModelManager.CACHE_MD5.add(message.fileName);
                loadModel(message.fileName, message.modelId, message.auth);
            });
        }
    }

    @SideOnly(Side.CLIENT)
    public static void loadModel(String fileName) {
        loadModel(fileName, null, false);
    }

    @SideOnly(Side.CLIENT)
    public static void loadModel(String fileName, @Nullable String modelId) {
        loadModel(fileName, modelId, false);
    }

    @SideOnly(Side.CLIENT)
    public static void loadModel(String fileName, @Nullable String modelId, boolean auth) {
        ThreadTools.THREAD_POOL.submit(() -> {
            try {
                while (ClientModelManager.PASSWORD == null) {
                    Thread.sleep(500);
                }
                if (Minecraft.getMinecraft().player != null) {
                    UUID uuid = Minecraft.getMinecraft().player.getUniqueID();
                    Path modelFile = ServerModelManager.CACHE_CLIENT.resolve(fileName);
                    byte[] fileBytes = FileUtils.readFileToByteArray(modelFile.toFile());
                    if (modelId != null && !modelId.isEmpty() && !isLegacyCache(fileBytes) && tryLoadModernModel(fileBytes, modelId, auth, UuidUtils.asBytes(uuid))) {
                        return;
                    }
                    ModelData data = EncryptTools.decryptModel(UuidUtils.asBytes(uuid), ClientModelManager.PASSWORD, fileBytes);
                    if (data != null) {
                        Minecraft.getMinecraft().addScheduledTask(() -> ClientModelManager.registerAll(data));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @SideOnly(Side.CLIENT)
    private static boolean tryLoadModernModel(byte[] fileBytes, String modelId, boolean auth, byte[] uuid) {
        try {
            long startTime = System.currentTimeMillis();
            byte[] modernCacheKey = EncryptTools.deriveModernCacheKey(uuid, ClientModelManager.PASSWORD);
            if (modernCacheKey.length != 56) {
                return false;
            }
            byte[] decompressed = YsmCrypt.read(fileBytes, modernCacheKey);
            try (YSMBinaryDeserializer deserializer = new YSMBinaryDeserializer(decompressed, 32)) {
                RawYsmModel rawModel = deserializer.deserializeKeepOpen();
                readModernFooter(rawModel, deserializer.getReader());
                ClientModelInfo parsedBundle = YSMClientMapper.buildParsedBundle(rawModel, modelId);
                if (parsedBundle.getInfo().isNeedAuth() != auth) {
                    parsedBundle = parsedBundle.withInfo(parsedBundle.getInfo().withNeedAuth(auth));
                }
                ClientModelInfo finalParsedBundle = parsedBundle;
                YesSteveModel.LOGGER.info("Built modern client model {} in {} ms", modelId, System.currentTimeMillis() - startTime);
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    try {
                        ClientModelManager.registerAll(modelId, finalParsedBundle);
                    } catch (Throwable t) {
                        YesSteveModel.LOGGER.error("Failed to register modern client model {}", modelId, t);
                    }
                });
                return true;
            }
        } catch (Throwable e) {
            YesSteveModel.LOGGER.warn("Failed to load modern client model {}", modelId, e);
            return false;
        }
    }

    private static void readModernFooter(RawYsmModel rawModel, YSMByteBuf reader) {
        rawModel.footer.version = reader.readVarInt();
        rawModel.footer.unkInt1 = reader.readVarInt();
        if (rawModel.footer.unkInt1 != 0) {
            rawModel.footer.rand = reader.readString();
        }
        rawModel.footer.time = reader.readVarLong();
        if (rawModel.footer.unkInt1 != 0) {
            rawModel.footer.extra = reader.readString();
            rawModel.footer.unkInt2 = reader.readVarInt();
        }
    }

    private static boolean isLegacyCache(byte[] fileBytes) {
        return fileBytes.length >= 8
                && ByteInteger.bytes2Int(fileBytes, 0) == EncryptTools.HEAD
                && ByteInteger.bytes2Int(fileBytes, 4) == EncryptTools.VERSION;
    }
}
