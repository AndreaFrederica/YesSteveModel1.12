package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.util.Keep;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TextureCountButton extends FlatColorButton {
    public TextureCountButton(int x, int y) {
        super(x, y, 20, 20, StringTextComponent.EMPTY, (b) -> {
        });
    }

    @Override
    @Keep
    public ITextComponent getMessage() {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            return player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).map(cap -> {
                ResourceLocation modelId = cap.getModelId();
                if (ClientModelManager.MODELS.containsKey(modelId)) {
                    String countText = String.valueOf(ClientModelManager.MODELS.get(modelId).size());
                    return new StringTextComponent(countText);
                }
                return super.getMessage();
            }).orElse(super.getMessage());
        }
        return super.getMessage();
    }
}
