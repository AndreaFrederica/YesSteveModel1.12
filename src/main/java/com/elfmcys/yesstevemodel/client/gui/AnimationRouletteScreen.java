package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.input.ExtraAnimationKey;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetPlayAnimation;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class AnimationRouletteScreen extends Screen {
    private int x;
    private int y;
    private int selectId = -1;
    private String[] names;

    public AnimationRouletteScreen() {
    }

    @Override
    public void initGui() {
        this.x = this.width / 2;
        this.y = this.height / 2 - 8;

        if (this.mc != null && this.mc.player != null) {
            CapabilityEvent.getCapability(this.mc.player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                ResourceLocation modelId = cap.getModelId();
                if (ClientModelManager.EXTRA_ANIMATION_NAME.containsKey(ModelIdUtil.getMainId(modelId))) {
                    this.names = ClientModelManager.EXTRA_ANIMATION_NAME.get(ModelIdUtil.getMainId(modelId));
                }
            });
        }
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawRoulette(pMouseX, pMouseY);
        this.drawRouletteText();
    }

    @Override
    public void mouseClicked(int pMouseX, int pMouseY, int pButton) throws IOException {
        if (-1 < this.selectId && this.selectId < 8 && this.mc != null) {
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            NetworkHandler.CHANNEL.sendToServer(new SetPlayAnimation(this.selectId));
            if (this.mc.player != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG) {
                this.mc.player.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.animation_roulette.play", this.selectId));
            }
            this.mc.displayGuiScreen(null);
        }
        super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void drawRouletteText() {
        int count = 8;
        float startDeg = (float) Math.PI / count;
        for (int i = 0; i < count; i++) {
            int r = 65;
            String keyText = TextFormatting.YELLOW + "[ ";
            KeyBinding keyMapping = ExtraAnimationKey.EXTRA_ANIMATION_KEYS.get(i);
            if (keyMapping.getKeyCode() == Keyboard.KEY_NONE) {
                keyText += I18n.format("key.yes_steve_model.extra_animation.none");
            } else {
                keyText += keyMapping.getDisplayName();
            }
            keyText += " ]";
            if (this.names != null && this.names.length > i && StringUtils.isNoneBlank(this.names[i])) {
                this.drawCenteredString(this.fontRenderer, this.names[i], (int) (this.x + r * MathHelper.cos(startDeg)), (int) (this.y + r * MathHelper.sin(startDeg) - (float) this.fontRenderer.FONT_HEIGHT / 2 - 8), 0xF3EFE0);
            } else {
                this.drawCenteredString(this.fontRenderer, String.valueOf(i), (int) (this.x + r * MathHelper.cos(startDeg)), (int) (this.y + r * MathHelper.sin(startDeg) - (float) this.fontRenderer.FONT_HEIGHT / 2 - 8), 0xF3EFE0);
            }
            this.drawCenteredString(this.fontRenderer, keyText, (int) (this.x + r * MathHelper.cos(startDeg)), (int) (this.y + r * MathHelper.sin(startDeg) - (float) this.fontRenderer.FONT_HEIGHT / 2 + 4), 0xF3EFE0);
            startDeg = startDeg + 2 * (float) Math.PI / count;
        }
    }

    private void drawRoulette(int mouseX, int mouseY) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        int count = 8;
        float theta = (float) MathHelper.atan2(mouseY - this.y, mouseX - this.x);
        if (theta < 0) {
            theta = (float) Math.PI * 2 + theta;
        }
        float dx = mouseX - this.x;
        float dy = mouseY - this.y;
        float distance = MathHelper.sqrt(dx * dx + dy * dy);
        boolean isSelected = false;
        for (int i = 0; i < count; i++) {
            float spacingDeg = (float) Math.PI / 90;
            float startDeg = (2 * (float) Math.PI / count) * i + spacingDeg;
            float endDeg = (2 * (float) Math.PI / count) * (i + 1) - spacingDeg;
            if (startDeg < theta && theta < endDeg && 50 < distance && distance < 100) {
                this.drawFan(bufferbuilder, 25, 105, startDeg, endDeg, 0xf0FFB100);
                isSelected = true;
                this.selectId = i;
            } else {
                this.drawFan(bufferbuilder, 25, 105, startDeg, endDeg, 0x90000000);
            }
        }
        if (!isSelected) {
            this.selectId = -1;
        }

        tesselator.draw();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void drawFan(BufferBuilder builder, float rIn, float rOut, float startDeg, float endDeg, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        builder.pos(this.x + rOut * MathHelper.cos(startDeg), this.y + rOut * MathHelper.sin(startDeg), this.zLevel).color(red, green, blue, alpha).endVertex();
        builder.pos(this.x + rIn * MathHelper.cos(startDeg), this.y + rIn * MathHelper.sin(startDeg), this.zLevel).color(red, green, blue, alpha).endVertex();
        builder.pos(this.x + rIn * MathHelper.cos(endDeg), this.y + rIn * MathHelper.sin(endDeg), this.zLevel).color(red, green, blue, alpha).endVertex();
        builder.pos(this.x + rOut * MathHelper.cos(endDeg), this.y + rOut * MathHelper.sin(endDeg), this.zLevel).color(red, green, blue, alpha).endVertex();
    }
}
