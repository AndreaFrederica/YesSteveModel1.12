package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.Tags;
import com.elfmcys.yesstevemodel.capability.AuthModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.StarModelsCapabilityProvider;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.gui.button.*;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeVersion;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlayerModelScreen extends Screen {
    private Map<ResourceLocation, List<ResourceLocation>> models = Maps.newHashMap();
    private List<ResourceLocation> modelOrderList;
    private int maxPage;
    private GuiTextField textField;
    private Category category;
    private int page;
    private int x;
    private int y;

    public PlayerModelScreen() {
        this.category = Category.ALL;
    }

    private void calculateModelList() {
        models = Maps.newHashMap();
        if (this.category == Category.ALL) {
            this.models.putAll(ClientModelManager.MODELS);
        }
        if (this.category == Category.AUTH) {
            if (this.mc != null && this.mc.player != null) {
                CapabilityEvent.getCapability(this.mc.player, AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(cap -> {
                    for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                        if (cap.containModel(modelId) || !ClientModelManager.AUTH_MODELS.contains(modelId.getPath())) {
                            this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                        }
                    }
                });
            }
        }
        if (this.category == Category.STAR) {
            if (this.mc != null && this.mc.player != null) {
                CapabilityEvent.getCapability(this.mc.player, StarModelsCapabilityProvider.STAR_MODELS_CAP).ifPresent(cap -> {
                    for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                        if (cap.containModel(modelId)) {
                            this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                        }
                    }
                });
            }
        }

        if (textField != null) {
            String search = this.textField.getText().toLowerCase(Locale.US);
            models.entrySet().removeIf(next -> !next.getKey().getPath().contains(search));
        }
        this.modelOrderList = Lists.newArrayList(models.keySet());
        this.modelOrderList.sort(ResourceLocation::compareTo);
        this.maxPage = (models.size() - 1) / 10;
    }

    @Override
    public void initGui() {
        this.calculateModelList();

        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;

        String perText = "";
        boolean focus = false;
        if (textField != null) {
            perText = textField.getText();
            focus = textField.isFocused();
        }
        textField = new GuiTextField(0, this.fontRenderer, x + 144, y + 6, 140, 16);
        textField.setText(perText);
        textField.setTextColor(0xF3EFE0);
        textField.setFocused(focus);
        textField.setCursorPositionEnd();

        addButton(new TextureCountButton(x + 5, y + 5));
        addButton(new FlatIconButton(x + 28, y + 5, 79, 20, 32, 16, (b) -> {
            if (this.mc.player != null) {
                EntityPlayerSP player = this.mc.player;
                CapabilityEvent.getCapability(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                    List<ResourceLocation> textures = ClientModelManager.MODELS.get(cap.getModelId());
                    if (textures != null) {
                        this.mc.displayGuiScreen(new PlayerTextureScreen(this, cap.getModelId(), textures));
                    }
                });
            }
        }).setTooltips("gui.yes_steve_model.model.texture"));
        addButton(new StarButton(x + 110, y + 5));

        addButton(new FlatIconButton(x + 328, y + 5, 18, 18, 32, 0, (b) -> {
            if (this.category != Category.ALL) {
                this.category = Category.ALL;
                this.page = 0;
                this.refreshGui();
            }
        }).setTooltips("gui.yes_steve_model.all_models"));
        addButton(new FlatIconButton(x + 308, y + 5, 18, 18, 48, 0, (b) -> {
            if (this.category != Category.AUTH) {
                this.category = Category.AUTH;
                this.page = 0;
                this.refreshGui();
            }
        }).setTooltips("gui.yes_steve_model.auth_models"));
        addButton(new FlatIconButton(x + 288, y + 5, 18, 18, 0, 0, (b) -> {
            if (this.category != Category.STAR) {
                this.category = Category.STAR;
                this.page = 0;
                this.refreshGui();
            }
        }).setTooltips("gui.yes_steve_model.star_models"));
        addButton(new FlatIconButton(x + 397, y + 5, 18, 18, 16, 16, (b) -> {
            this.mc.displayGuiScreen(new ConfigScreen(this));
        }).setTooltips("gui.yes_steve_model.config"));
        addButton(new FlatIconButton(x + 377, y + 5, 18, 18, 0, 16, (b) -> {
            this.mc.displayGuiScreen(new DownloadScreen(this));
        }).setTooltips("gui.yes_steve_model.download"));
        addButton(new FlatIconButton(x + 357, y + 5, 18, 18, 80, 0, (b) -> {
            this.mc.displayGuiScreen(new OpenModelFolderScreen(this));
        }).setTooltips("gui.yes_steve_model.open_model_folder.open"));

        addButton(new FlatColorButton(x + 198, y + 215, 52, 14, I18n.format("gui.yes_steve_model.pre_page"), (b) -> {
            if (this.page > 0) {
                this.page--;
                this.refreshGui();
            }
        }));
        addButton(new FlatColorButton(x + 308, y + 215, 52, 14, I18n.format("gui.yes_steve_model.next_page"), (b) -> {
            if (this.page < this.maxPage) {
                this.page++;
                this.refreshGui();
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
            if (this.mc != null && this.mc.player != null) {
                CapabilityEvent.getCapability(this.mc.player, AuthModelsCapabilityProvider.AUTH_MODELS_CAP).ifPresent(cap -> {
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
    @SuppressWarnings("all")
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.drawGradientRect(x, y, x + 135, y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(x + 138, y, x + 420, y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(x + 351, y + 7, x + 352, y + 21, 0xFF_F3EFE0, 0xFF_F3EFE0);

        textField.drawTextBox();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        RenderUtil.scissor(this.x + 5, this.y + 29, 125, 171);
        GuiInventory.drawEntityOnScreen(x + 67, y + 190, 70, x + 67 - mouseX, y + 180 - 95 - mouseY, player);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        CapabilityEvent.getCapability(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
            String modelName = cap.getModelId().getPath();
            List<String> modelNameSplit = this.fontRenderer.listFormattedStringToWidth(modelName, 125);
            int lineY = y + 205;
            for (String line : modelNameSplit) {
                int nameWidth = this.fontRenderer.getStringWidth(line);
                this.drawString(this.fontRenderer, line, x + (135 - nameWidth) / 2, lineY, 0xF3EFE0);
                lineY += 10;
            }
        });

        if (textField.getText().isEmpty() && !textField.isFocused()) {
            this.drawString(this.fontRenderer, TextFormatting.ITALIC + I18n.format("gui.yes_steve_model.search"), x + 148, y + 10, 0x777777);
        }

        String pageInfo = String.format("%d/%d", page + 1, this.maxPage + 1);
        this.drawString(this.fontRenderer, pageInfo, x + 138 + (282 - this.fontRenderer.getStringWidth(pageInfo)) / 2, y + 223 - this.fontRenderer.FONT_HEIGHT / 2, 0xF3EFE0);

        String debugInfo = String.format("%s-%s", ForgeVersion.mcVersion, Tags.VERSION);
        this.drawString(this.fontRenderer, TextFormatting.DARK_GRAY + debugInfo, x + 2, y + 226, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.buttonList.stream().filter(r -> r instanceof FlatIconButton)
                .forEach(r -> ((FlatIconButton) r).renderToolTip(this, mouseX, mouseY));
        this.buttonList.stream().filter(r -> r instanceof ModelButton)
                .forEach(r -> ((ModelButton) r).renderComponentTooltip(this, mouseX, mouseY));
    }

    @Override
    public void onResize(@Nonnull Minecraft minecraft, int width, int height) {
        String value = this.textField.getText();
        super.onResize(minecraft, width, height);
        this.textField.setText(value);
    }

    @Override
    public void updateScreen() {
        this.textField.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (this.textField.mouseClicked(mouseX, mouseY, button)) {
            this.textField.setFocused(true);
            return;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void keyTyped(char codePoint, int modifiers) throws IOException {
        if (this.textField == null) {
            return;
        }
        String perText = this.textField.getText();
        if (this.textField.textboxKeyTyped(codePoint, modifiers)) {
            if (!Objects.equals(perText, this.textField.getText())) {
                this.page = 0;
                this.refreshGui();
            }
            return;
        }
        super.keyTyped(codePoint, modifiers);
    }

    @Override
    public void mouseScrolled(int mouseX, int mouseY, int delta) {
        if (this.mc == null) {
            return;
        }
        if (delta != 0 && inRange(mouseX, mouseY)) {
            scrollPage(delta);
            return;
        }
        super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean inRange(int mouseX, int mouseY) {
        boolean isInWidthRange = (x + 143) < mouseX && mouseX < (x + 430);
        boolean isInHeightRange = (y + 25) < mouseY && mouseY < (y + 235);
        return isInWidthRange && isInHeightRange;
    }

    private boolean scrollPage(float delta) {
        if (delta > 0 && this.page > 0) {
            this.page--;
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.refreshGui();
        }
        if (delta < 0 && this.page < this.maxPage) {
            this.page++;
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            this.refreshGui();
        }
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private enum Category {
        /**
         * 不同页面类别
         */
        ALL, AUTH, STAR
    }
}
