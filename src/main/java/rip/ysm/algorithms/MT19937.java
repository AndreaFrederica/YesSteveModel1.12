package rip.ysm.algorithms;

public class MT19937 {

    private static final int W = 64;
    private static final int N = 312;
    private static final int M = 156;
    private static final int R = 31;
    private static final long A = 0xB5026F5AA96619E9L;
    private static final int U = 29;
    private static final int S = 17;
    private static final int T = 37;
    private static final long D = 0x5555555555555555L;
    private static final long B = 0x71d67fffeda60000L;
    private static final long C = 0xfff7eee000000000L;
    private static final int L = 43;
    private static final long F = 6364136223846793005L;
    private static final long LOWER_MASK = 0x7FFFFFFFL;
    private static final long UPPER_MASK = 0xFFFFFFFF80000000L;

    private final long[] mt;
    private int index;

    public MT19937() {
        this.mt = new long[N];
        this.setSeed(System.currentTimeMillis());
    }

    public MT19937(long seed) {
        this.mt = new long[N];
        this.setSeed(seed);
    }

    private void setSeed(long seed) {
        this.mt[0] = seed;
        this.index = N;
        for (int i = 1; i < N; i++) {
            this.mt[i] = F * (this.mt[i - 1] ^ (this.mt[i - 1] >>> (W - 2))) + i;
        }
    }

    private void twist() {
        for (int i = 0; i < N; i++) {
            long x = (this.mt[i] & UPPER_MASK) | (this.mt[(i + 1) % N] & LOWER_MASK);
            long xa = x >>> 1;
            if ((x & 1L) != 0L) {
                xa ^= A;
            }
            this.mt[i] = this.mt[(i + M) % N] ^ xa;
        }
        this.index = 0;
    }

    public long extract_number() {
        if (this.index >= N) {
            this.twist();
        }
        long y = this.mt[this.index++];

        y ^= (y >>> U) & D;
        y ^= (y << S) & B;
        y ^= (y << T) & C;
        y ^= (y >>> L);

        return y;
    }
}