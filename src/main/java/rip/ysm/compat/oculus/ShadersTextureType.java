package rip.ysm.compat.oculus;

import net.minecraft.util.ResourceLocation;

public enum ShadersTextureType {
    NORMAL("_n"),
    SPECULAR("_s");

    public static final ShadersTextureType[] VALUES = values();

    private final String suffix;

    ShadersTextureType(String suffix) {
        this.suffix = suffix;
    }

    public ResourceLocation appendSuffix(ResourceLocation resourceLocation) {
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + this.suffix);
    }
}