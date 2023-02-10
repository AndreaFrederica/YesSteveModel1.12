package com.elfmcys.yesstevemodel.client.texture;

import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResourceManager;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class OuterFileTexture extends Texture {
    private final byte[] data;

    public OuterFileTexture(byte[] data) {
        this.data = data;
    }

    @Override
    @Keep
    public void load(@Nonnull IResourceManager resourceManager) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(this::doLoad);
        } else {
            this.doLoad();
        }
    }

    private void doLoad() {
        try {
            NativeImage imageIn = NativeImage.read(new ByteArrayInputStream(data));
            int width = imageIn.getWidth();
            int height = imageIn.getHeight();
            TextureUtil.prepareImage(this.getId(), 0, width, height);
            imageIn.upload(0, 0, 0, 0, 0, width, height, false, false, false, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
