package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetPlayAnimation;
import com.elfmcys.yesstevemodel.util.ControllerUtils;
import com.elfmcys.yesstevemodel.util.Keep;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.opengl.GL11;

public class AnimationRouletteScreen extends Screen {
    private int x;
    private int y;
    private int selectId = -1;

    public AnimationRouletteScreen() {
        super(new StringTextComponent("Animation Roulette GUI"));
    }

    @Override
    @Keep
    protected void init() {
        this.x = width / 2;
        this.y = height / 2 - 8;
    }

    @Override
    @Keep
    public void render(MatrixStack poseStack, int pMouseX, int pMouseY, float pPartialTick) {
        drawRoulette(poseStack, pMouseX, pMouseY);
        drawRouletteText(poseStack);
    }

    @Override
    @Keep
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (-1 < selectId && selectId < 8 && minecraft != null) {
            minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            NetworkHandler.CHANNEL.sendToServer(new SetPlayAnimation(selectId));
            ControllerUtils.markCapControllerReload();
            if (minecraft.player != null && GeneralConfig.PRINT_ANIMATION_ROULETTE_MSG.get()) {
                minecraft.player.sendMessage(new TranslationTextComponent("message.yes_steve_model.model.animation_roulette.play", selectId), Util.NIL_UUID);
            }
            minecraft.setScreen(null);
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    @Keep
    public boolean isPauseScreen() {
        return false;
    }

    private void drawRouletteText(MatrixStack poseStack) {
        int count = 8;
        float startDeg = (float) Math.PI / count;
        for (int i = 0; i < count; i++) {
            int r = 65;
            drawCenteredString(poseStack, font, String.valueOf(i), (int) (x + r * MathHelper.cos(startDeg)), (int) (y + r * MathHelper.sin(startDeg) - font.lineHeight / 2), 0xF3EFE0);
            startDeg = startDeg + 2 * (float) Math.PI / count;
        }
    }

    private void drawRoulette(MatrixStack pMatrixStack, int mouseX, int mouseY) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Tessellator tesselator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        Matrix4f pMatrix = pMatrixStack.last().pose();

        int count = 8;
        float theta = (float) MathHelper.atan2(mouseY - y, mouseX - x);
        if (theta < 0) {
            theta = (float) Math.PI * 2 + theta;
        }
        float distance = MathHelper.sqrt(MathHelper.square(mouseY - y) + MathHelper.square(mouseX - x));
        boolean isSelected = false;
        for (int i = 0; i < count; i++) {
            float spacingDeg = (float) Math.PI / 90;
            float startDeg = (2 * (float) Math.PI / count) * i + spacingDeg;
            float endDeg = (2 * (float) Math.PI / count) * (i + 1) - spacingDeg;
            if (startDeg < theta && theta < endDeg && 50 < distance && distance < 100) {
                drawFan(bufferbuilder, pMatrix, 25, 105, startDeg, endDeg, 0xf0FFB100);
                isSelected = true;
                this.selectId = i;
            } else {
                drawFan(bufferbuilder, pMatrix, 25, 105, startDeg, endDeg, 0x90000000);
            }
        }
        if (!isSelected) {
            this.selectId = -1;
        }

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private void drawFan(BufferBuilder builder, Matrix4f matrix4f, float rIn, float rOut, float startDeg, float endDeg, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        builder.vertex(matrix4f, x + rOut * MathHelper.cos(startDeg), y + rOut * MathHelper.sin(startDeg), this.getBlitOffset()).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix4f, x + rIn * MathHelper.cos(startDeg), y + rIn * MathHelper.sin(startDeg), this.getBlitOffset()).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix4f, x + rIn * MathHelper.cos(endDeg), y + rIn * MathHelper.sin(endDeg), this.getBlitOffset()).color(red, green, blue, alpha).endVertex();
        builder.vertex(matrix4f, x + rOut * MathHelper.cos(endDeg), y + rOut * MathHelper.sin(endDeg), this.getBlitOffset()).color(red, green, blue, alpha).endVertex();
    }
}
