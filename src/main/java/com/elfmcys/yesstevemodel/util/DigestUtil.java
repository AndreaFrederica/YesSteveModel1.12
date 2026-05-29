package com.elfmcys.yesstevemodel.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class DigestUtil {
    private static final ThreadLocal<MessageDigest> MD5_TL = ThreadLocal.withInitial(() -> createDigest("MD5"));
    private static final ThreadLocal<MessageDigest> SHA256_TL = ThreadLocal.withInitial(() -> createDigest("SHA-256"));

    private DigestUtil() {
    }

    public static MessageDigest md5Digest() {
        MessageDigest md = MD5_TL.get();
        md.reset();
        return md;
    }

    public static MessageDigest sha256Digest() {
        MessageDigest md = SHA256_TL.get();
        md.reset();
        return md;
    }

    public static byte[] md5(byte[] input) {
        MessageDigest md = md5Digest();
        return md.digest(input);
    }

    public static byte[] sha256(byte[] input) {
        MessageDigest md = sha256Digest();
        return md.digest(input);
    }

    public static String md5Hex(byte[] input) {
        return toHex(md5(input));
    }

    public static String sha256Hex(byte[] input) {
        return toHex(sha256(input));
    }

    private static MessageDigest createDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " algorithm not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            String hex = Integer.toHexString(value & 0xFF);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}