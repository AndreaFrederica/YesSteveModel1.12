package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.Tags;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.gui.button.*;
import com.elfmcys.yesstevemodel.client.input.PlayerModelScreenKey;
import com.elfmcys.yesstevemodel.config.Config;
import com.elfmcys.yesstevemodel.config.GeneralConfig;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.RenderUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PlayerModelScreen extends Screen {
    protected final EntityPlayer player;
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
        this.player = Minecraft.getMinecraft().player;
    }

    public PlayerModelScreen(EntityPlayer player) {
        this.category = Category.ALL;
        this.player = player;
    }

    private void calculateModelList() {
        this.models = Maps.newHashMap();
        if (this.category == Category.ALL) {
            this.models.putAll(ClientModelManager.MODELS);
        }
        if (this.category == Category.AUTH) {
            CapabilityEvent.getAuthModelsCap(this.player).ifPresent(cap -> {
                for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                    if (cap.containModel(modelId) || !ClientModelManager.AUTH_MODELS.contains(modelId.getPath())) {
                        this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                    }
                }
            });
        }
        if (this.category == Category.STAR) {
            CapabilityEvent.getStarModelsCap(this.player).ifPresent(cap -> {
                for (ResourceLocation modelId : ClientModelManager.MODELS.keySet()) {
                    if (cap.containModel(modelId)) {
                        this.models.put(modelId, ClientModelManager.MODELS.get(modelId));
                    }
                }
            });
        }

        if (this.textField != null) {
            String search = this.textField.getText().toLowerCase(Locale.US);
            this.models.entrySet().removeIf(next -> !next.getKey().getPath().contains(search));
        }
        this.modelOrderList = Lists.newArrayList(this.models.keySet());
        this.modelOrderList.sort(ResourceLocation::compareTo);
        this.maxPage = (this.models.size() - 1) / 10;
    }

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    public void initGui() {
        this.calculateModelList();

        this.x = (this.width - 420) / 2;
        this.y = (this.height - 235) / 2;

        String perText = "";
        boolean focus = false;
        if (this.textField != null) {
            perText = this.textField.getText();
            focus = this.textField.isFocused();
        }
        this.textField = new GuiTextField(0, this.fontRenderer, this.x + 144, this.y + 6, 140, 16);
        this.textField.setText(perText);
        this.textField.setTextColor(0xF3EFE0);
        this.textField.setFocused(focus);
        this.textField.setCursorPositionEnd();

        this.addButton(new TextureCountButton(this.x + 5, this.y + 5));
        this.addButton(new FlatIconButton(this.x + 28, this.y + 5, 79, 20, 32, 16, (b) -> {
            CapabilityEvent.getModelInfoCap(this.player).ifPresent(cap -> {
                List<ResourceLocation> textures = ClientModelManager.MODELS.get(cap.getModelId());
                if (textures != null) {
                    this.mc.displayGuiScreen(new PlayerTextureScreen(this, cap.getModelId(), textures));
                }
            });
        }).setTooltips("gui.yes_steve_model.model.texture"));
        this.addButton(new StarButton(this.x + 110, this.y + 5));

        this.addButton(new ConfigCheckBox(this.x + 5, this.y - 22, "show_model_id_first", this.fontRenderer,
                GeneralConfig.SHOW_MODEL_ID_FIRST, value -> GeneralConfig.SHOW_MODEL_ID_FIRST = value) {
            @Override
            public void onPress() {
                super.onPress();
                Config.save();
            }
        });

        this.addButton(new FlatIconButton(this.x + 328, this.y + 5, 18, 18, 32, 0, (b) -> {
            if (this.category != Category.ALL) {
                this.category = Category.ALL;
                this.page = 0;
                this.refreshGui();
            }
        }).setTooltips("gui.yes_steve_model.all_models"));
        this.addButton(new FlatIconButton(this.x + 308, this.y + 5, 18, 18, 48, 0, (b) -> {
            if (this.category != Category.AUTH) {
                this.category = Category.AUTH;
                this.page = 0;
                this.refreshGui();
            }
        }).setTooltips("gui.yes_steve_model.auth_models"));
        this.addButton(new FlatIconButton(this.x + 288, this.y + 5, 18, 18, 0, 0, (b) -> {
            if (this.category != Category.STAR) {
                this.category = Category.STAR;
                this.page = 0;
                this.refreshGui();
            }
        }).setTooltips("gui.yes_steve_model.star_models"));
        this.addButton(new FlatIconButton(this.x + 397, this.y + 5, 18, 18, 16, 16, (b) -> {
            this.mc.displayGuiScreen(new ConfigScreen(this));
        }).setTooltips("gui.yes_steve_model.config"));
        this.addButton(new FlatIconButton(this.x + 377, this.y + 5, 18, 18, 0, 16, (b) -> {
            this.mc.displayGuiScreen(new DownloadScreen(this));
        }).setTooltips("gui.yes_steve_model.download"));
        this.addButton(new FlatIconButton(this.x + 357, this.y + 5, 18, 18, 80, 0, (b) -> {
            this.mc.displayGuiScreen(new OpenModelFolderScreen(this));
        }).setTooltips("gui.yes_steve_model.open_model_folder.open"));

        this.addButton(new FlatColorButton(this.x + 198, this.y + 215, 52, 14, I18n.format("gui.yes_steve_model.pre_page"), (b) -> {
            if (this.page > 0) {
                this.page--;
                this.refreshGui();
            }
        }));
        this.addButton(new FlatColorButton(this.x + 308, this.y + 215, 52, 14, I18n.format("gui.yes_steve_model.next_page"), (b) -> {
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
            if (modelIndex >= this.models.size()) {
                break;
            }
            ResourceLocation id = this.modelOrderList.get(modelIndex);
            int xStart = this.x + 143 + 55 * (i % 5);
            int yStart = this.y + 28 + 93 * (i / 5);
            CapabilityEvent.getAuthModelsCap(this.player).ifPresent(cap -> {
                if (ClientModelManager.AUTH_MODELS.contains(id.getPath()) && !cap.containModel(id)) {
                    this.addButton(new ModelButton(xStart, yStart, true, Pair.of(id, this.models.get(id)), ClientModelManager.METADATA.get(ModelIdUtil.getInfoId(id)), this.player));
                } else {
                    this.addButton(new ModelButton(xStart, yStart, false, Pair.of(id, this.models.get(id)), ClientModelManager.METADATA.get(ModelIdUtil.getInfoId(id)), this.player));
                }
            });
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        this.drawGradientRect(this.x, this.y, this.x + 135, this.y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(this.x + 138, this.y, this.x + 420, this.y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(this.x + 351, this.y + 7, this.x + 352, this.y + 21, 0xFF_F3EFE0, 0xFF_F3EFE0);

        this.textField.drawTextBox();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        RenderUtil.scissor(this.x + 5, this.y + 29, 125, 171);
        GuiInventory.drawEntityOnScreen(this.x + 67, this.y + 190, 70, this.x + 67 - mouseX, this.y + 180 - 95 - mouseY, this.player);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        CapabilityEvent.getModelInfoCap(this.player).ifPresent(cap -> {
            String modelName = cap.getModelId().getPath();
            final ExtraInfo extraInfo = ClientModelManager.EXTRA_INFO.get(ModelIdUtil.getInfoId(cap.getModelId()));
            if (extraInfo.getName() != null && !extraInfo.getName().isEmpty()) {
                modelName = extraInfo.getName();
            }
            List<String> modelNameSplit = this.fontRenderer.listFormattedStringToWidth(modelName, 125);
            int lineY = this.y + 205;
            for (String line : modelNameSplit) {
                int nameWidth = this.fontRenderer.getStringWidth(line);
                this.drawString(this.fontRenderer, line, this.x + (135 - nameWidth) / 2, lineY, 0xF3EFE0);
                lineY += 10;
            }
        });

        if (this.textField.getText().isEmpty() && !this.textField.isFocused()) {
            this.drawString(this.fontRenderer, TextFormatting.ITALIC + I18n.format("gui.yes_steve_model.search"), this.x + 148, this.y + 10, 0x777777);
        }

        String pageInfo = String.format("%d/%d", this.page + 1, this.maxPage + 1);
        this.drawString(this.fontRenderer, pageInfo, this.x + 138 + (282 - this.fontRenderer.getStringWidth(pageInfo)) / 2, this.y + 223 - this.fontRenderer.FONT_HEIGHT / 2, 0xF3EFE0);

        String debugInfo = String.format("%s-%s", Loader.MC_VERSION, Tags.VERSION);
        this.drawString(this.fontRenderer, TextFormatting.DARK_GRAY + debugInfo, this.x + 2, this.y + 226, 0xFFFFFFFF);

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
        this.textField.mouseClicked(mouseX, mouseY, button);
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
        if (delta != 0 && this.inRange(mouseX, mouseY)) {
            this.scrollPage(delta);
            return;
        }
        super.mouseScrolled(mouseX, mouseY, delta);
    }

    private boolean inRange(int mouseX, int mouseY) {
        boolean isInWidthRange = (this.x + 143) < mouseX && mouseX < (this.x + 430);
        boolean isInHeightRange = (this.y + 25) < mouseY && mouseY < (this.y + 235);
        return isInWidthRange && isInHeightRange;
    }

    private void scrollPage(float delta) {
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
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected boolean canGuiClose(int keyCode) {
        return super.canGuiClose(keyCode) || isKeyActiveIgnoreConflict(PlayerModelScreenKey.PLAYER_MODEL_KEY, keyCode);
    }

    /**
     * 不同页面类别
     */
    private enum Category {
        ALL, AUTH, STAR
    }
}
