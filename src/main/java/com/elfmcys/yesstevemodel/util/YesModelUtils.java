package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.zip.DataFormatException;

public final class YesModelUtils {
    /**
     * 二进制文件的头部幻数
     * YSGP 的 ASCII 码
     * YSGP 就是 Ying Su Group，映素小组的缩写
     */
    public static final int HEAD = 0x59_53_47_50;

    /**
     * 二进制文件的版本号
     */
    public static final int VERSION = 0x00_00_00_01;
    /**
     * 加密方法
     */
    private static final String ENCRYPTION_METHOD = "AES";

    public static Map<String, byte[]> input(File ysmFile) throws IOException {
        String fileName = removeExtension(ysmFile.getName());
        if (!ResourceLocation.isValidResourceLocation(fileName)) {
            return Collections.emptyMap();
        }
        byte[] data = FileUtils.readFileToByteArray(ysmFile);
        int head = ByteInteger.bytes2Int(data, 0);
        int version = ByteInteger.bytes2Int(data, 4);
        if (head != HEAD) {
            return Collections.emptyMap();
        }
        if (version != VERSION) {
            return Collections.emptyMap();
        }

        byte[] md5 = ByteArrays.copy(data, 8, 16);
        byte[] modelFilesData = ByteArrays.copy(data, 24, data.length - 24);
        if (!Arrays.equals(md5, Md5Utils.md5(modelFilesData))) {
            return Collections.emptyMap();
        }

        Map<String, byte[]> outputs = Maps.newHashMap();
        ByteArrayInputStream tmp = new ByteArrayInputStream(modelFilesData);
        while (tmp.available() > 0) {
            try {
                Pair<String, byte[]> ysmFileData = ysmToFile(tmp);
                outputs.put(ysmFileData.getKey(), ysmFileData.getValue());
            } catch (GeneralSecurityException | DataFormatException e) {
                e.printStackTrace();
            }
        }
        return outputs;
    }

    @Nonnull
    private static Pair<String, byte[]> ysmToFile(ByteArrayInputStream tmp) throws IOException, GeneralSecurityException, DataFormatException {
        String name = readString(tmp);
        int size = readInt(tmp);

        byte[] passwordBytes = new byte[16];
        byte[] ivBytes = new byte[16];
        tmp.read(passwordBytes);
        tmp.read(ivBytes);
        SecretKeySpec key = new SecretKeySpec(passwordBytes, ENCRYPTION_METHOD);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        byte[] fileData = new byte[size];
        tmp.read(fileData);

        ByteArrayOutputStream decryptData = AESUtil.decrypt(key, iv, fileData);
        byte[] rawData = DeflateUtil.decompressBytes(decryptData.toByteArray());

        return Pair.of(name, rawData);
    }

    public static void export(File dir) throws IOException {
        String dirName = dir.getName();
        if (!ResourceLocation.isValidResourceLocation(dirName)) {
            return;
        }
        boolean noMainModelFile = true;
        boolean noArmModelFile = true;
        boolean noTextureFile = true;
        Collection<File> files = FileUtils.listFiles(dir, FileFileFilter.FILE, null);
        for (File file : files) {
            String fileName = file.getName();
            if ("main.json".equals(fileName)) {
                noMainModelFile = false;
            }
            if ("arm.json".equals(fileName)) {
                noArmModelFile = false;
            }
            if (fileName.endsWith(".png")) {
                noTextureFile = false;
            }
        }
        if (noMainModelFile) {
            return;
        }
        if (noArmModelFile) {
            return;
        }
        if (noTextureFile) {
            return;
        }

        byte[] ysmData = filesToYsm(files);
        File outputFile = ServerModelManager.EXPORT.resolve(dirName + ".ysm").toFile();
        FileUtils.writeByteArrayToFile(outputFile, ysmData, false);
    }

    /**
     * 头部幻数
     * 版本号
     * 总 MD5
     * 各个文件
     */
    private static byte[] filesToYsm(Collection<File> files) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(ByteInteger.int2Bytes(HEAD));
        output.write(ByteInteger.int2Bytes(VERSION));

        byte[] filesData = filesToBytes(files);
        byte[] md5 = Md5Utils.md5(filesData);
        output.write(md5);
        output.write(filesData);
        return output.toByteArray();
    }

    private static byte[] filesToBytes(Collection<File> files) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        files.forEach(file -> {
            try {
                output.write(fileToBytes(file));
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        });
        return output.toByteArray();
    }

    /**
     * 名称
     * 长度
     * 密码
     * 主体
     */
    private static byte[] fileToBytes(File file) throws IOException, GeneralSecurityException {
        byte[] rawData = FileUtils.readFileToByteArray(file);
        byte[] compressData = DeflateUtil.compressBytes(rawData);

        SecretKey secretKey = AESUtil.generateKey();
        IvParameterSpec iv = AESUtil.generateIv();
        ByteArrayOutputStream encryptData = AESUtil.encrypt(secretKey, iv, compressData);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeString(output, file.getName());
        output.write(ByteInteger.int2Bytes(encryptData.size()));
        output.write(secretKey.getEncoded());
        output.write(iv.getIV());
        output.write(encryptData.toByteArray());
        return output.toByteArray();
    }

    private static void writeString(ByteArrayOutputStream stream, String string) throws IOException {
        byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
        stream.write(ByteInteger.int2Bytes(stringBytes.length));
        stream.write(stringBytes);
    }

    private static String readString(ByteArrayInputStream stream) throws IOException {
        int size = readInt(stream);
        byte[] stringBytes = new byte[size];
        stream.read(stringBytes);
        return new String(stringBytes);
    }

    private static boolean readBoolean(ByteArrayInputStream stream) throws IOException {
        return readInt(stream) != 0;
    }

    @SuppressWarnings("all")
    private static int readInt(ByteArrayInputStream stream) throws IOException {
        byte[] sizeBytes = new byte[4];
        stream.read(sizeBytes);
        return ByteInteger.bytes2Int(sizeBytes, 0);
    }

    private static String removeExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex != -1) {
            fileName = fileName.substring(0, lastIndex);
        }
        return fileName;
    }
}
