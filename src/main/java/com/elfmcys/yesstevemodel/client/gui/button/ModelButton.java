package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SetModelAndTexture;
import com.elfmcys.yesstevemodel.util.Keep;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ModelButton extends Button {
    private final static ResourceLocation ICON = new ResourceLocation(YesSteveModel.MOD_ID, "texture/icon.png");
    private final Pair<ResourceLocation, List<ResourceLocation>> modelInfo;
    private final boolean needAuth;
    private final int color;
    private final List<ITextComponent> tooltips;

    public ModelButton(int pX, int pY, boolean needAuth, Pair<ResourceLocation, List<ResourceLocation>> modelInfo, List<ITextComponent> tooltips) {
        super(pX, pY, 52, 90, new StringTextComponent(modelInfo.getLeft().getPath()), (b) -> {
        });
        this.modelInfo = modelInfo;
        this.needAuth = needAuth;
        this.color = needAuth ? 0x7F_000000 : 0xFF_434242;
        this.tooltips = tooltips;
    }

    @Override
    @Keep
    public void onPress() {
        if (needAuth) {
            return;
        }
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap ->
                    cap.setModelAndTexture(modelInfo.getLeft(), modelInfo.getRight().get(0)));
        }
        NetworkHandler.CHANNEL.sendToServer(new SetModelAndTexture(modelInfo.getLeft(), modelInfo.getRight().get(0)));
    }

    @Override
    @Keep
    public void renderButton(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer font = minecraft.font;

        fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, this.color, this.color);
        MainWindow window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int scissorX = (int) (this.x * scale);
        int scissorY = (int) (window.getHeight() - ((this.y + this.height - 20) * scale));
        int scissorW = (int) (this.width * scale);
        int scissorH = (int) ((this.height - 20) * scale);
        RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
        RenderUtil.renderEntityInInventory(this.x + this.width / 2, this.y + this.height / 2 + 20, 30, minecraft.player, modelInfo.getLeft(), modelInfo.getRight().get(0));
        RenderSystem.disableScissor();

        ITextComponent message = this.getMessage();
        List<IReorderingProcessor> split = font.split(message, 45);
        if (split.size() > 1) {
            drawCenteredString(poseStack, font, split.get(0), this.x + this.width / 2, this.y + this.height - 19, 0xF3EFE0);
            drawCenteredString(poseStack, font, split.get(1), this.x + this.width / 2, this.y + this.height - 10, 0xF3EFE0);
        } else {
            drawCenteredString(poseStack, font, this.getMessage(), this.x + this.width / 2, this.y + this.height - 15, 0xF3EFE0);
        }
        if (!this.needAuth && this.isHovered()) {
            fillGradient(poseStack, this.x, this.y + 1, this.x + 1, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x + this.width - 1, this.y + 1, this.x + this.width, this.y + this.height - 1, 0xff_F3EFE0, 0xff_F3EFE0);
            fillGradient(poseStack, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height, 0xff_F3EFE0, 0xff_F3EFE0);
        }

        if (minecraft.player != null) {
            minecraft.player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap -> {
                if (cap.containModel(modelInfo.getLeft())) {
                    minecraft.getTextureManager().bind(ICON);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.enableDepthTest();
                    blit(poseStack, this.x + this.width - 14, this.y, 16, 16, 16, 0, 16, 16, 256, 256);
                }
            });
        }

        if (needAuth) {
            fillGradient(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 0x9f_222222, 0x9f_222222);
        }
    }

    public void renderComponentTooltip(Screen screen, MatrixStack pPoseStack, int pMouseX, int pMouseY) {
        if (this.isHovered && tooltips != null) {
            screen.renderComponentTooltip(pPoseStack, tooltips, pMouseX, pMouseY);
        }
    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        return !this.needAuth && super.clicked(pMouseX, pMouseY);
    }

    private static void drawCenteredString(MatrixStack poseStack, FontRenderer pFont, IReorderingProcessor processor, int pX, int pY, int color) {
        pFont.drawShadow(poseStack, processor, (float) (pX - pFont.width(processor) / 2), (float) pY, color);
    }
}
