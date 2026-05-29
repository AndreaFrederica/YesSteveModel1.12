package rip.ysm.algorithms;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import com.google.common.primitives.Ints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class YsmZstd {
    public static byte[] decompress(byte[] rawData) throws IOException {
        return decompress(rawData, 0, rawData.length);
    }

    public static byte[] decompress(byte[] rawData, int offset, int length) throws IOException {
        byte[] input = offset == 0 && length == rawData.length ? Arrays.copyOf(rawData, rawData.length) : Arrays.copyOfRange(rawData, offset, offset + length);
        washInPlace(input, 0, input.length);

        long size = Zstd.decompressedSize(input);
        if (size > 0 && size <= Integer.MAX_VALUE) {
            return Zstd.decompress(input, Ints.checkedCast(size));
        }

        try (ZstdInputStream in = new ZstdInputStream(new ByteArrayInputStream(input))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(64 * 1024, input.length * 2));
            byte[] buf = new byte[64 * 1024];
            int read;
            while ((read = in.read(buf)) != -1) {
                baos.write(buf, 0, read);
            }
            return baos.toByteArray();
        }
    }

    public static byte[] compress(byte[] rawData) {
        return compress(rawData, 0, rawData.length);
    }

    public static byte[] compress(byte[] rawData, int offset, int length) {
        byte[] input = offset == 0 && length == rawData.length ? rawData : Arrays.copyOfRange(rawData, offset, offset + length);
        return obfuscate(Zstd.compress(input, 3));
    }

    private static void washInPlace(byte[] data, int base, int length) {
        if (data == null || length < 5) {
            throw new IllegalArgumentException("Invalid data length");
        }

        int magic = (data[base] & 0xFF)
                | ((data[base + 1] & 0xFF) << 8)
                | ((data[base + 2] & 0xFF) << 16)
                | ((data[base + 3] & 0xFF) << 24);
        if (magic != 0xFD2FB528) {
            throw new IllegalArgumentException("Not a standard ZSTD Magic Number. May be skippable frame or unknown.");
        }

        byte fhd = data[base + 4];
        data[base + 4] = (byte) (fhd & 0xFB);

        int frameHeaderSize = calculateFrameHeaderSize(fhd);
        int current = base + 4 + frameHeaderSize;
        int end = base + length;

        while (current + 3 <= end) {
            int b0 = data[current] & 0xFF;
            int b1 = data[current + 1] & 0xFF;
            int b2 = data[current + 2] & 0xFF;
            int lastBlock = (b0 >> 7) & 1;
            int blockTypeYsm = (b0 >> 5) & 3;

            int rawSize = ((b0 & 0x1F) << 16) | b1 | (b2 << 8);
            int cSize = rawSize ^ 0xD4E9;
            int blockTypeStd;
            switch (blockTypeYsm) {
                case 0:
                    blockTypeStd = 2;
                    break;
                case 1:
                    blockTypeStd = 1;
                    break;
                case 2:
                    blockTypeStd = 3;
                    break;
                case 3:
                    blockTypeStd = 0;
                    break;
                default:
                    throw new IllegalStateException("Unknown block type");
            }

            int stdHeader = lastBlock | (blockTypeStd << 1) | (cSize << 3);

            data[current] = (byte) (stdHeader & 0xFF);
            data[current + 1] = (byte) ((stdHeader >> 8) & 0xFF);
            data[current + 2] = (byte) ((stdHeader >> 16) & 0xFF);

            int blockDataSize = blockTypeStd == 1 ? 1 : cSize;
            current += 3 + blockDataSize;

            if (lastBlock == 1) {
                break;
            }
        }
    }

    private static byte[] obfuscate(byte[] data) {
        if (data == null || data.length < 5) {
            throw new IllegalArgumentException("Invalid data length");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        int magic = buffer.getInt(0);
        if (magic != 0xFD2FB528) {
            throw new IllegalArgumentException("Not a standard ZSTD frame.");
        }

        byte fhd = data[4];
        int frameHeaderSize = calculateFrameHeaderSize(fhd);
        int current = 4 + frameHeaderSize;

        while (current + 3 <= data.length) {
            int b0 = data[current] & 0xFF;
            int b1 = data[current + 1] & 0xFF;
            int b2 = data[current + 2] & 0xFF;
            int cBlockHeader = b0 | (b1 << 8) | (b2 << 16);

            int lastBlock = cBlockHeader & 1;
            int blockTypeStd = (cBlockHeader >> 1) & 3;
            int cSize = cBlockHeader >> 3;

            int blockDataSize = blockTypeStd == 1 ? 1 : cSize;

            int blockTypeYsm;
            switch (blockTypeStd) {
                case 0:
                    blockTypeYsm = 3;
                    break;
                case 1:
                    blockTypeYsm = 1;
                    break;
                case 2:
                    blockTypeYsm = 0;
                    break;
                case 3:
                    blockTypeYsm = 2;
                    break;
                default:
                    throw new IllegalStateException("Unknown block type");
            }

            int rawSize = cSize ^ 0xD4E9;
            int ysmB0 = (lastBlock << 7) | (blockTypeYsm << 5) | ((rawSize >> 16) & 0x1F);
            int ysmB1 = rawSize & 0xFF;
            int ysmB2 = (rawSize >> 8) & 0xFF;

            data[current] = (byte) ysmB0;
            data[current + 1] = (byte) ysmB1;
            data[current + 2] = (byte) ysmB2;

            current += 3 + blockDataSize;

            if (lastBlock == 1) {
                break;
            }
        }

        return data;
    }

    private static int calculateFrameHeaderSize(byte fhd) {
        int size = 1;
        boolean singleSegment = ((fhd >> 5) & 1) == 1;

        int dictIdSize = 0;
        int dictIdBits = fhd & 3;
        if (dictIdBits == 1) {
            dictIdSize = 1;
        } else if (dictIdBits == 2) {
            dictIdSize = 2;
        } else if (dictIdBits == 3) {
            dictIdSize = 4;
        }

        int fcsSize = 0;
        int fcsBits = (fhd >> 6) & 3;
        if (fcsBits == 0) {
            fcsSize = singleSegment ? 1 : 0;
        } else if (fcsBits == 1) {
            fcsSize = 2;
        } else if (fcsBits == 2) {
            fcsSize = 4;
        } else if (fcsBits == 3) {
            fcsSize = 8;
        }

        int windowDescSize = singleSegment ? 0 : 1;

        return size + windowDescSize + dictIdSize + fcsSize;
    }
}