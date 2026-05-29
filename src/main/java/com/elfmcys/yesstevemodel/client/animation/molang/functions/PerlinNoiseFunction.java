package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;

public class PerlinNoiseFunction implements Function {

    private static final int[] PERM = new int[512];

    static {
        int[] p = {
            151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,
            140,36,103,30,69,142,8,99,37,240,21,10,23,190,6,148,
            247,120,234,75,0,26,197,62,94,252,219,203,117,35,11,32,
            57,177,33,88,237,149,56,87,174,20,125,136,171,168,68,175,
            74,165,71,134,139,48,27,166,77,146,158,231,83,111,229,122,
            60,211,133,230,220,105,92,41,55,46,245,40,244,102,143,54,
            65,25,63,161,1,216,80,73,209,76,132,187,208,89,18,169,
            200,196,135,130,116,188,159,86,164,100,109,198,173,186,3,64,
            52,217,226,250,124,123,5,202,38,147,118,126,255,82,85,212,
            207,206,59,227,47,16,58,17,182,189,28,42,223,183,170,213,
            119,248,152,2,44,154,163,70,221,153,101,155,167,43,172,9,
            129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,104,
            218,246,97,228,251,34,242,193,238,210,144,12,191,179,162,241,
            81,51,145,235,249,14,239,107,49,192,214,31,181,199,106,157,
            184,84,204,176,115,121,50,45,127,4,150,254,138,236,205,93,
            222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180
        };
        for (int i = 0; i < 256; i++) {
            PERM[i] = p[i];
            PERM[256 + i] = p[i];
        }
    }

    @Override
    public Object evaluate(ExecutionContext<?> context, ArgumentCollection arguments) {
        int seed = arguments.getAsInt(context, 0);
        float x = arguments.getAsFloat(context, 1);
        float y = arguments.size() > 2 ? arguments.getAsFloat(context, 2) : 0.0f;
        float z = arguments.size() > 3 ? arguments.getAsFloat(context, 3) : 0.0f;
        return noise3D(x * 0.01f, y * 0.01f, z * 0.01f, seed);
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 2 && size <= 4;
    }

    private static double noise3D(float x, float y, float z, int seed) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int zi = (int) Math.floor(z) & 255;
        float xf = x - (float) Math.floor(x);
        float yf = y - (float) Math.floor(y);
        float zf = z - (float) Math.floor(z);
        float u = fade(xf);
        float v = fade(yf);
        float w = fade(zf);
        int s = seed & 255;
        int a = PERM[xi + s] + yi;
        int aa = PERM[a + s] + zi;
        int ab = PERM[a + 1 + s] + zi;
        int b = PERM[xi + 1 + s] + yi;
        int ba = PERM[b + s] + zi;
        int bb = PERM[b + 1 + s] + zi;
        return lerp(w, lerp(v,
                lerp(u, grad(PERM[aa], xf, yf, zf), grad(PERM[ba], xf - 1, yf, zf)),
                lerp(u, grad(PERM[ab], xf, yf - 1, zf), grad(PERM[bb], xf - 1, yf - 1, zf))),
                lerp(v,
                lerp(u, grad(PERM[aa + 1], xf, yf, zf - 1), grad(PERM[ba + 1], xf - 1, yf, zf - 1)),
                lerp(u, grad(PERM[ab + 1], xf, yf - 1, zf - 1), grad(PERM[bb + 1], xf - 1, yf - 1, zf - 1))));
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(float t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, float x, float y, float z) {
        int h = hash & 15;
        float u = h < 8 ? x : y;
        float v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
