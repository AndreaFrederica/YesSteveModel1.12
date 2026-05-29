package com.elfmcys.yesstevemodel.client.texture;

import rip.ysm.compat.oculus.ShadersTextureType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OuterFileTexture extends AbstractTexture {
    private final byte[] data;
    private Map<ShadersTextureType, OuterFileTexture> suffixTextures = Collections.emptyMap();

    public OuterFileTexture(byte[] data) {
        this.data = data;
    }

    @Override
    public void loadTexture(@Nonnull IResourceManager resourceManager) {
        this.deleteGlTexture();

        BufferedImage bufferedimage = null;
        try (ByteArrayInputStream is = new ByteArrayInputStream(this.data)) {
            bufferedimage = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bufferedimage != null) {
            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedimage, false, false);
        }
    }

    public void setSuffixTextures(Map<ShadersTextureType, OuterFileTexture> suffixTextures) {
        this.suffixTextures = Collections.unmodifiableMap(new LinkedHashMap<>(suffixTextures));
    }

    public Map<ShadersTextureType, ? extends AbstractTexture> getSuffixTextures() {
        return this.suffixTextures;
    }
}
