package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.gui.button.FlatIconButton;
import com.elfmcys.yesstevemodel.client.gui.button.TextureButton;
import com.elfmcys.yesstevemodel.util.Keep;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;

public class PlayerTextureScreen extends Screen {
    private static final float SCALE_MAX = 360f;
    private static final float SCALE_MIN = 18f;
    private static final float PITCH_MAX = 90f;
    private static final float PITCH_MIN = -90f;

    private static final int LEFT_MOUSE_BUTTON = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private final PlayerModelScreen parent;
    private final ResourceLocation modelId;
    private final List<ResourceLocation> textures;
    private final List<String> animations;
    private String animation = "";
    private int maxTexturePage;
    private int texturePage;
    private int maxAnimationPage;
    private int animationPage;
    private int x;
    private int y;

    private float posX = 0;
    private float posY = -60;
    private float scale = 80;
    private float yaw = 165;
    private float pitch = -5;
    private boolean showGround = true;


    public PlayerTextureScreen(PlayerModelScreen parent, ResourceLocation modelId, List<ResourceLocation> textures) {
        super(new StringTextComponent("Player Texture GUI"));
        this.parent = parent;
        this.modelId = modelId;
        this.textures = textures;
        this.textures.sort(ResourceLocation::compareTo);
        this.animations = new ArrayList<>(ClientModelManager.DEFAULT_ANIMATION_FILE.animations().keySet());
        this.animations.sort(String::compareTo);
    }

    @Override
    @Keep
    protected void init() {
        this.buttons.clear();
        this.children.clear();

        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;
        this.maxTexturePage = (textures.size() - 1) / 4;
        this.maxAnimationPage = (animations.size() - 1) / 11;
        if (this.texturePage > this.maxTexturePage) {
            this.texturePage = 0;
        }
        if (this.animationPage > this.maxAnimationPage) {
            this.animationPage = 0;
        }

        addButton(new FlatColorButton(x + 5, y, 80, 18, new TranslationTextComponent("gui.yes_steve_model.model.return"), (b) -> {
            this.getMinecraft().setScreen(parent);
        }));

        addButton(new FlatIconButton(x + 281, y + 2, 16, 16, 64, 16, (b) -> {
            this.animation = "";
        }).setTooltips("gui.yes_steve_model.model.stop"));
        addButton(new FlatIconButton(x + 263, y + 2, 16, 16, 48, 16, (b) -> {
            this.posX = 0;
            this.posY = -60;
            this.scale = 80;
            this.yaw = 165;
            this.pitch = -5;
        }).setTooltips("gui.yes_steve_model.model.reset"));
        addButton(new FlatIconButton(x + 245, y + 2, 16, 16, 64, 0, (b) -> {
            this.showGround = !this.showGround;
        }).setTooltips("gui.yes_steve_model.model.ground"));

        addButton(new FlatColorButton(x + 321, y + 213, 18, 18, new StringTextComponent("<"), (b) -> {
            if (this.texturePage > 0) {
                this.texturePage--;
                this.init();
            }
        }));
        addButton(new FlatColorButton(x + 383, y + 213, 18, 18, new StringTextComponent(">"), (b) -> {
            if (this.texturePage < this.maxTexturePage) {
                this.texturePage++;
                this.init();
            }
        }));
        addButton(new FlatColorButton(x + 11, y + 214, 16, 16, new StringTextComponent("<"), (b) -> {
            if (this.animationPage > 0) {
                this.animationPage--;
                this.init();
            }
        }));
        addButton(new FlatColorButton(x + 63, y + 214, 16, 16, new StringTextComponent(">"), (b) -> {
            if (this.animationPage < this.maxAnimationPage) {
                this.animationPage++;
                this.init();
            }
        }));


        for (int i = 0; i < 11; i++) {
            int animationIndex = i + this.animationPage * 11;
            if (animationIndex >= animations.size()) {
                break;
            }
            String name = animations.get(animationIndex);
            int yStart = y + 27 + 17 * i;
            String key = String.format("gui.yes_steve_model.texture.button.%s", name.replaceAll("\\:", "."));
            String keyDesc = String.format("gui.yes_steve_model.texture.button.%s.desc", name.replaceAll("\\:", "."));
            FlatColorButton sideButton = new FlatColorButton(x + 5, yStart, 80, 16, new TranslationTextComponent(key), b -> this.animation = name);
            sideButton.setTooltips(Lists.newArrayList(new TranslationTextComponent(keyDesc).withStyle(TextFormatting.GOLD),
                    new TranslationTextComponent("gui.yes_steve_model.texture.button.animation_name", name).withStyle(TextFormatting.GRAY)));
            addButton(sideButton);
        }

        for (int i = 0; i < 4; i++) {
            int modelIndex = i + this.texturePage * 4;
            if (modelIndex >= textures.size()) {
                break;
            }
            int xStart = x + 306 + 56 * (i % 2);
            int yStart = y + 5 + 104 * (i / 2);
            addButton(new TextureButton(xStart, yStart, modelId, textures.get(modelIndex)));
        }
    }

    @Override
    @Keep
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        ClientPlayerEntity player = getMinecraft().player;
        if (player == null) {
            return;
        }

        poseStack.translate(0, 0, -1000);
        renderBackground(poseStack);
        fillGradient(poseStack, x, y + 22, x + 90, y + 235, 0xff_222222, 0xff_222222);
        fillGradient(poseStack, x + 93, y, x + 299, y + 235, 0xff_222222, 0xff_222222);
        fillGradient(poseStack, x + 302, y, x + 420, y + 235, 0xff_222222, 0xff_222222);
        poseStack.translate(0, 0, 1000);

        player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
            MainWindow window = Minecraft.getInstance().getWindow();
            double guiScale = window.getGuiScale();
            int scissorX = (int) ((this.x + 93) * guiScale);
            int scissorY = (int) (window.getHeight() - ((this.y + 235) * guiScale));
            int scissorW = (int) (206 * guiScale);
            int scissorH = (int) (235 * guiScale);
            RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
            RenderUtil.renderTextureScreenEntity(this.x + 299 / 2.0F + 40 + posX, this.y + 235 / 2.0F + 80 + posY, scale, pitch, yaw, getMinecraft().player, modelId, cap.getSelectTexture(), showGround, entity -> {
                if (!entity.hasPreviewAnimation(animation)) {
                    entity.setPreviewAnimation(animation);
                }
            });
            RenderSystem.disableScissor();
        });

        String texturePageInfo = String.format("%d/%d", texturePage + 1, this.maxTexturePage + 1);
        font.draw(poseStack, texturePageInfo, x + 302 + (118 - font.width(texturePageInfo)) / 2.0F, y + 223 - font.lineHeight / 2.0F, 0xF3EFE0);

        String animationPageInfo = String.format("%d/%d", animationPage + 1, this.maxAnimationPage + 1);
        font.draw(poseStack, animationPageInfo, x + 5 + (80 - font.width(animationPageInfo)) / 2.0F, y + 218, 0xF3EFE0);

        super.render(poseStack, mouseX, mouseY, partialTick);
        this.buttons.stream().filter(r -> r instanceof FlatColorButton)
                .forEach(r -> ((FlatColorButton) r).renderToolTip(this, poseStack, mouseX, mouseY));
    }

    @Override
    @Keep
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (minecraft == null || !inViewRange(mouseX, mouseY)) {
            return false;
        }
        if (button == LEFT_MOUSE_BUTTON) {
            yaw += (1.5 * dragX);
            changePitchValue((float) dragY);
        }
        if (button == RIGHT_MOUSE_BUTTON) {
            posX += dragX;
            posY += dragY;
        }
        return true;
    }

    @Override
    @Keep
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (minecraft == null) {
            return false;
        }
        if (delta != 0) {
            if (inViewRange(mouseX, mouseY)) {
                changeScaleValue((float) delta * 0.07f);
                return true;
            }
            if (inAnimationRange(mouseX, mouseY)) {
                return scrollAnimationPage(delta);
            }
            if (inTextureRange(mouseX, mouseY)) {
                return scrollTexturePage(delta);
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean scrollTexturePage(double delta) {
        if (delta > 0 && this.texturePage > 0) {
            this.texturePage--;
            getMinecraft().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        if (delta < 0 && this.texturePage < this.maxTexturePage) {
            this.texturePage++;
            getMinecraft().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        return true;
    }

    private boolean scrollAnimationPage(double delta) {
        if (delta > 0 && this.animationPage > 0) {
            this.animationPage--;
            getMinecraft().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        if (delta < 0 && this.animationPage < this.maxAnimationPage) {
            this.animationPage++;
            getMinecraft().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        return true;
    }

    private boolean inViewRange(double mouseX, double mouseY) {
        boolean isInWidthRange = (x + 93) < mouseX && mouseX < (x + 299);
        boolean isInHeightRange = y < mouseY && mouseY < (y + 235);
        return isInWidthRange && isInHeightRange;
    }

    private boolean inAnimationRange(double mouseX, double mouseY) {
        boolean isInWidthRange = x < mouseX && mouseX < (x + 90);
        boolean isInHeightRange = (y + 22) < mouseY && mouseY < (y + 235);
        return isInWidthRange && isInHeightRange;
    }

    private boolean inTextureRange(double mouseX, double mouseY) {
        boolean isInWidthRange = (x + 302) < mouseX && mouseX < (x + 420);
        boolean isInHeightRange = y < mouseY && mouseY < (y + 235);
        return isInWidthRange && isInHeightRange;
    }

    private void changePitchValue(float amount) {
        if (pitch - amount > PITCH_MAX) {
            pitch = 90;
        } else if (pitch - amount < PITCH_MIN) {
            pitch = -90;
        } else {
            pitch = pitch - amount;
        }
    }

    private void changeScaleValue(float amount) {
        float tmp = scale + amount * scale;
        scale = MathHelper.clamp(tmp, SCALE_MIN, SCALE_MAX);
    }

    @Override
    @Keep
    public boolean isPauseScreen() {
        return false;
    }
}
