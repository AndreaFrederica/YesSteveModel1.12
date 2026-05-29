package rip.ysm.algorithms;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public abstract class ChaCha20Base {
    protected static final int[] SIGMA = toIntArray(new byte[]{101, 120, 112, 97, 110, 100, 32, 51, 50, 45, 98, 121, 116, 101, 32, 107});
    public final int[] state = new int[16];
    public int rounds;

    protected final int[] workingState = new int[16];
    protected final byte[] keystreamBuf = new byte[64];

    protected static int rotateLeft(int value, int shift) {
        return (value >>> (-shift)) | (value << shift);
    }

    public static void quarterRound(int[] x, int a, int b, int c, int d) {
        x[a] += x[b];
        x[d] = rotateLeft(x[d] ^ x[a], 16);
        x[c] += x[d];
        x[b] = rotateLeft(x[b] ^ x[c], 12);
        x[a] += x[b];
        x[d] = rotateLeft(x[d] ^ x[a], 8);
        x[c] += x[d];
        x[b] = rotateLeft(x[b] ^ x[c], 7);
    }

    public static void shuffleState(int[] x, int rounds) {
        int halfRounds = rounds / 2;
        for (int index = 0; index < halfRounds; index++) {
            quarterRound(x, 0, 4, 8, 12);
            quarterRound(x, 1, 5, 9, 13);
            quarterRound(x, 2, 6, 10, 14);
            quarterRound(x, 3, 7, 11, 15);
            quarterRound(x, 0, 5, 10, 15);
            quarterRound(x, 1, 6, 11, 12);
            quarterRound(x, 2, 7, 8, 13);
            quarterRound(x, 3, 4, 9, 14);
        }
    }

    protected static int[] toIntArray(byte[] bytes) {
        IntBuffer asIntBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        int[] out = new int[asIntBuffer.remaining()];
        asIntBuffer.get(out);
        return out;
    }

    public byte[] processBlock() {
        processBlock(keystreamBuf);
        return keystreamBuf;
    }

    public void processBlock(byte[] out) {
        int[] ws = workingState;
        System.arraycopy(state, 0, ws, 0, 16);
        shuffleState(ws, this.rounds);
        for (int index = 0; index < 16; index++) {
            ws[index] += state[index];
        }
        for (int index = 0; index < 16; index++) {
            int value = ws[index];
            int pos = index << 2;
            out[pos] = (byte) value;
            out[pos + 1] = (byte) (value >>> 8);
            out[pos + 2] = (byte) (value >>> 16);
            out[pos + 3] = (byte) (value >>> 24);
        }
    }

    public void incrementCounter() {
        state[12]++;
        if (state[12] == 0) {
            state[13]++;
        }
    }
}