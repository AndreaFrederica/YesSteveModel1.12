package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.AuthModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.gui.button.*;
import com.elfmcys.yesstevemodel.util.Keep;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlayerModelScreen extends Screen {
    private Map<ResourceLocation, List<ResourceLocation>> models = Maps.newHashMap();
    private List<ResourceLocation> modelOrderList;
    private int maxPage;
    private TextFieldWidget textField;
    private Category category;
    private int page;
    private int x;
    private int y;

    public PlayerModelScreen() {
        super(new StringTextComponent("YSM PlayerEntity Model GUI"));
        this.category = Category.ALL;
    }

    private void calculateModelList() {
        models = Maps.newHashMap();
        if (this.category == Category.ALL) {
            this.models.putAll(ClientModelManager.MODELS);
        }
        if (this.category == Category.AUTH) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(cap -> {
                    for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                        if (cap.containModel(modelId) || !ClientModelManager.AUTH_MODELS.contains(modelId.getPath())) {
                            this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                        }
                    }
                });
            }
        }
        if (this.category == Category.STAR) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap -> {
                    for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                        if (cap.containModel(modelId)) {
                            this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                        }
                    }
                });
            }
        }

        if (textField != null) {
            String search = this.textField.getValue().toLowerCase(Locale.US);
            models.entrySet().removeIf(next -> !next.getKey().getPath().contains(search));
        }
        this.modelOrderList = Lists.newArrayList(models.keySet());
        this.modelOrderList.sort(ResourceLocation::compareTo);
        this.maxPage = (models.size() - 1) / 10;
    }

    @Override
    @Keep
    protected void init() {
        this.buttons.clear();
        this.children.clear();
        this.calculateModelList();

        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;

        String perText = "";
        boolean focus = false;
        if (textField != null) {
            perText = textField.getValue();
            focus = textField.isFocused();
        }
        textField = new TextFieldWidget(getMinecraft().font, x + 144, y + 6, 140, 16, new StringTextComponent("YSM Search Box"));
        textField.setValue(perText);
        textField.setTextColor(0xF3EFE0);
        textField.setFocus(focus);
        textField.moveCursorToEnd();
        this.addWidget(this.textField);

        addButton(new TextureCountButton(x + 5, y + 5));
        addButton(new FlatIconButton(x + 28, y + 5, 79, 20, 32, 16, (b) -> {
            if (Minecraft.getInstance().player != null) {
                ClientPlayerEntity player = Minecraft.getInstance().player;
                player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                    List<ResourceLocation> textures = ClientModelManager.MODELS.get(cap.getModelId());
                    if (textures != null) {
                        Minecraft.getInstance().setScreen(new PlayerTextureScreen(this, cap.getModelId(), textures));
                    }
                });
            }
        }).setTooltips("gui.yes_steve_model.model.texture"));
        addButton(new StarButton(x + 110, y + 5));

        addButton(new FlatIconButton(x + 328, y + 5, 18, 18, 32, 0, (b) -> {
            if (this.category != Category.ALL) {
                this.category = Category.ALL;
                this.page = 0;
                this.init();
            }
        }).setTooltips("gui.yes_steve_model.all_models"));
        addButton(new FlatIconButton(x + 308, y + 5, 18, 18, 48, 0, (b) -> {
            if (this.category != Category.AUTH) {
                this.category = Category.AUTH;
                this.page = 0;
                this.init();
            }
        }).setTooltips("gui.yes_steve_model.auth_models"));
        addButton(new FlatIconButton(x + 288, y + 5, 18, 18, 0, 0, (b) -> {
            if (this.category != Category.STAR) {
                this.category = Category.STAR;
                this.page = 0;
                this.init();
            }
        }).setTooltips("gui.yes_steve_model.star_models"));
        addButton(new FlatIconButton(x + 397, y + 5, 18, 18, 16, 16, (b) -> {
            this.getMinecraft().setScreen(new ConfigScreen(this));
        }).setTooltips("gui.yes_steve_model.config"));
        addButton(new FlatIconButton(x + 377, y + 5, 18, 18, 0, 16, (b) -> {
            this.getMinecraft().setScreen(new DownloadScreen(this));
        }).setTooltips("gui.yes_steve_model.download"));
        addButton(new FlatIconButton(x + 357, y + 5, 18, 18, 80, 0, (b) -> {
            this.getMinecraft().setScreen(new OpenModelFolderScreen(this));
        }).setTooltips("gui.yes_steve_model.open_model_folder.open"));

        addButton(new FlatColorButton(x + 198, y + 215, 52, 14, new TranslationTextComponent("gui.yes_steve_model.pre_page"), (b) -> {
            if (this.page > 0) {
                this.page--;
                this.init();
            }
        }));
        addButton(new FlatColorButton(x + 308, y + 215, 52, 14, new TranslationTextComponent("gui.yes_steve_model.next_page"), (b) -> {
            if (this.page < this.maxPage) {
                this.page++;
                this.init();
            }
        }));

        if (this.page > this.maxPage) {
            this.page = 0;
        }

        for (int i = 0; i < 10; i++) {
            int modelIndex = i + this.page * 10;
            if (modelIndex >= models.size()) {
                break;
            }
            ResourceLocation id = modelOrderList.get(modelIndex);
            int xStart = x + 143 + 55 * (i % 5);
            int yStart = y + 28 + 93 * (i / 5);
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(cap -> {
                    if (ClientModelManager.AUTH_MODELS.contains(id.getPath()) && !cap.containModel(id)) {
                        addButton(new ModelButton(xStart, yStart, true, Pair.of(id, models.get(id)), ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getMainId(id))));
                    } else {
                        addButton(new ModelButton(xStart, yStart, false, Pair.of(id, models.get(id)), ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getMainId(id))));
                    }
                });
            }
        }
    }

    @Override
    @Keep
    @SuppressWarnings("all")
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);

        fillGradient(poseStack, x, y, x + 135, y + 235, 0xff_222222, 0xff_222222);
        fillGradient(poseStack, x + 138, y, x + 420, y + 235, 0xff_222222, 0xff_222222);
        fillGradient(poseStack, x + 351, y + 7, x + 352, y + 21, 0xFF_F3EFE0, 0xFF_F3EFE0);

        textField.render(poseStack, mouseX, mouseY, partialTicks);
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            MainWindow window = Minecraft.getInstance().getWindow();
            double scale = window.getGuiScale();
            int scissorX = (int) ((this.x + 5) * scale);
            int scissorY = (int) (window.getHeight() - ((this.y + 200) * scale));
            int scissorW = (int) (125 * scale);
            int scissorH = (int) (171 * scale);
            RenderSystem.enableScissor(scissorX, scissorY, scissorW, scissorH);
            InventoryScreen.renderEntityInInventory(x + 67, y + 190, 70, x + 67 - mouseX, y + 180 - 95 - mouseY, player);
            RenderSystem.disableScissor();

            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                String modelName = cap.getModelId().getPath();
                List<IReorderingProcessor> modelNameSplit = font.split(new StringTextComponent(modelName), 125);
                int lineY = y + 205;
                for (IReorderingProcessor line : modelNameSplit) {
                    int nameWidth = font.width(line);
                    font.draw(poseStack, line, x + (135 - nameWidth) / 2.0F, lineY, 0xF3EFE0);
                    lineY += 10;
                }
            });
        }

        if (textField.getValue().isEmpty() && !textField.isFocused()) {
            font.draw(poseStack, new TranslationTextComponent("gui.yes_steve_model.search").withStyle(TextFormatting.ITALIC), x + 148, y + 10, 0x777777);
        }

        String pageInfo = String.format("%d/%d", page + 1, this.maxPage + 1);
        font.draw(poseStack, pageInfo, x + 138 + (282 - font.width(pageInfo)) / 2.0F, y + 223 - font.lineHeight / 2, 0xF3EFE0);

        String debugInfo = String.format("%s-%s", SharedConstants.getCurrentVersion().getName(), ModList.get().getModFileById(YesSteveModel.MOD_ID).getMods().get(0).getVersion().toString());
        font.draw(poseStack, debugInfo, x + 2, y + 226, TextFormatting.DARK_GRAY.getColor());

        // FIXME: 2023/6/21 奇妙的修复了 bug，应该不影响渲染？
        InventoryScreen.renderEntityInInventory(0, 0, 0, 0, 0, player);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.buttons.stream().filter(r -> r instanceof FlatIconButton)
                .forEach(r -> ((FlatIconButton) r).renderToolTip(this, poseStack, mouseX, mouseY));
        this.buttons.stream().filter(r -> r instanceof ModelButton)
                .forEach(r -> ((ModelButton) r).renderComponentTooltip(this, poseStack, mouseX, mouseY));
    }

    @Override
    @Keep
    public void resize(Minecraft minecraft, int width, int height) {
        String value = this.textField.getValue();
        super.resize(minecraft, width, height);
        this.textField.setValue(value);
    }

    @Override
    @Keep
    public void tick() {
        this.textField.tick();
    }

    @Override
    @Keep
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.textField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.textField);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @Keep
    public boolean charTyped(char codePoint, int modifiers) {
        if (textField == null) {
            return false;
        }
        String perText = this.textField.getValue();
        if (this.textField.charTyped(codePoint, modifiers)) {
            if (!Objects.equals(perText, this.textField.getValue())) {
                this.page = 0;
                this.init();
            }
            return true;
        }
        return false;
    }

    @Override
    @Keep
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean hasKeyCode = InputMappings.getKey(keyCode, scanCode).getNumericKeyValue().isPresent();
        String preText = this.textField.getValue();
        if (hasKeyCode) {
            return true;
        }
        if (this.textField.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(preText, this.textField.getValue())) {
                this.page = 0;
                this.init();
            }
            return true;
        } else {
            return this.textField.isFocused() && this.textField.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    @Keep
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.textField.setValue(text);
        } else {
            this.textField.insertText(text);
        }
    }

    @Override
    @Keep
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (minecraft == null) {
            return false;
        }
        if (delta != 0 && inRange(mouseX, mouseY)) {
            return scrollPage(delta);
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean inRange(double mouseX, double mouseY) {
        boolean isInWidthRange = (x + 143) < mouseX && mouseX < (x + 430);
        boolean isInHeightRange = (y + 25) < mouseY && mouseY < (y + 235);
        return isInWidthRange && isInHeightRange;
    }

    private boolean scrollPage(double delta) {
        if (delta > 0 && this.page > 0) {
            this.page--;
            getMinecraft().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        if (delta < 0 && this.page < this.maxPage) {
            this.page++;
            getMinecraft().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.init();
        }
        return true;
    }

    @Override
    @Keep
    public boolean isPauseScreen() {
        return false;
    }

    private enum Category {
        /**
         * 不同页面类别
         */
        ALL, AUTH, STAR
    }
}
