package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.FlatColorButton;
import com.elfmcys.yesstevemodel.client.gui.button.ModelInfoButton;
import com.elfmcys.yesstevemodel.client.upload.UploadManager;
import com.elfmcys.yesstevemodel.model.format.Type;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.HandleFile;
import com.elfmcys.yesstevemodel.network.message.RefreshModelManage;
import com.elfmcys.yesstevemodel.network.message.RequestServerModelInfo;
import com.elfmcys.yesstevemodel.network.message.UploadFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class ModelManageScreen extends Screen {
    private static final int MAX_COUNT = 13;
    private final List<RequestServerModelInfo.Info> customModels;
    private final List<RequestServerModelInfo.Info> authModels;
    private volatile String uploadError = null;
    private GuiTextField textField;
    private static boolean isCustomModels = true;
    private int index = -1;
    private Action action = Action.EMPTY;
    private static int page = 0;
    private int modelsCount = 0;
    private int x;
    private int y;

    public ModelManageScreen(List<RequestServerModelInfo.Info> customModels, List<RequestServerModelInfo.Info> authModels) {
        this.customModels = customModels;
        this.authModels = authModels;
    }

    private void calculateList() {
        this.modelsCount = getModels().size();
        if ((this.modelsCount - 1) / MAX_COUNT < page) {
            page = 0;
        }
    }

    @Override
    public void initGui() {
        this.calculateList();
        this.x = (width - 420) / 2;
        this.y = (height - 235) / 2;
        this.addTopButtons();
        this.addPageButtons();
        this.addModelInfoButtons();
        if (index >= 0) {
            addActionButtons();
        }
        if (this.action != Action.EMPTY) {
            addExtraButtons();
        }
    }

    private void addExtraButtons() {
        if (this.action != Action.UPLOAD || StringUtils.isNoneBlank(UploadManager.FILE_PATH)) {
            addButton(new FlatColorButton(x + 270, y + 235 - 23, 70, 18, I18n.format("gui.yes_steve_model.model_manage.confirm"), (b) -> {
                boolean canConfirm = false;
                if (index >= 0 && index < getModels().size()) {
                    RequestServerModelInfo.Info info = getModels().get(index);
                    UploadFile.Dir dir = isCustomModels ? UploadFile.Dir.CUSTOM : UploadFile.Dir.AUTH;
                    if (this.action == Action.DELETE) {
                        NetworkHandler.CHANNEL.sendToServer(new HandleFile(info.getFileName(), dir, "delete", ""));
                        canConfirm = true;
                    }
                    if (this.action == Action.MOVE) {
                        NetworkHandler.CHANNEL.sendToServer(new HandleFile(info.getFileName(), dir, "move", ""));
                        canConfirm = true;
                    }
                    if (this.action == Action.RENAME && StringUtils.isNotBlank(this.textField.getText())) {
                        String value = this.textField.getText();
                        String fileName = info.getFileName();
                        if (info.getType() == Type.FOLDER && !value.equals(fileName)) {
                            NetworkHandler.CHANNEL.sendToServer(new HandleFile(info.getFileName(), dir, "rename", value));
                            canConfirm = true;
                        }
                        if (info.getType() != Type.FOLDER && !value.equals(fileName.substring(0, fileName.length() - 4))) {
                            value = value + fileName.substring(fileName.length() - 4);
                            NetworkHandler.CHANNEL.sendToServer(new HandleFile(info.getFileName(), dir, "rename", value));
                            canConfirm = true;
                        }
                    }
                }
                if (this.action == Action.UPLOAD && StringUtils.isNoneBlank(UploadManager.FILE_PATH) && UploadManager.STATUE == UploadManager.Statue.FULFILL) {
                    try {
                        uploadFile(UploadManager.FILE_PATH, isCustomModels);
                        canConfirm = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (canConfirm) {
                    this.action = Action.EMPTY;
                    NetworkHandler.CHANNEL.sendToServer(new RefreshModelManage());
                }
            }));
            addButton(new FlatColorButton(x + 345, y + 235 - 23, 70, 18, I18n.format("gui.yes_steve_model.model_manage.cancel"), (b) -> {
                this.action = Action.EMPTY;
                this.refreshGui();
            }));
        }
        if (this.action == Action.RENAME) {
            textField = new GuiTextField(0, this.fontRenderer, x + 270, y + 51, 145, 14);
            textField.setTextColor(0xF3EFE0);
            textField.setMaxStringLength(24);
            textField.setCursorPositionEnd();
        }
    }

    private void addActionButtons() {
        addButton(new FlatColorButton(x + 5, y + 235 - 23, 80, 18, I18n.format("gui.yes_steve_model.model_manage.delete"), (b) -> {
            this.action = Action.DELETE;
            this.refreshGui();
        }));
        addButton(new FlatColorButton(x + 90, y + 235 - 23, 80, 18, I18n.format("gui.yes_steve_model.model_manage.move"), (b) -> {
            this.action = Action.MOVE;
            this.refreshGui();
        }));
        addButton(new FlatColorButton(x + 175, y + 235 - 23, 80, 18, I18n.format("gui.yes_steve_model.model_manage.rename"), (b) -> {
            this.action = Action.RENAME;
            this.refreshGui();
        }));
    }

    private void addModelInfoButtons() {
        int modelsY = y + 51;
        int count = page * MAX_COUNT;
        for (int i = count; i < count + MAX_COUNT; i++) {
            if (i >= this.getModels().size()) {
                return;
            }
            RequestServerModelInfo.Info info = this.getModels().get(i);
            final int finalIndex = i;
            ModelInfoButton modelInfoButton = new ModelInfoButton(x + 5, modelsY, 12, info, (b) -> {
                this.index = finalIndex;
                this.action = Action.EMPTY;
                this.refreshGui();
            });
            if (this.index == i) {
                modelInfoButton.setSelect(true);
            }
            addButton(modelInfoButton);
            modelsY += 12;
        }
    }

    private void addPageButtons() {
        addButton(new FlatColorButton(x + 5, y + 28, 80, 18, "<", (b) -> {
            if (page > 0) {
                page--;
                this.refreshGui();
            }
        }));
        addButton(new FlatColorButton(x + 260 - 85, y + 28, 80, 18, ">", (b) -> {
            if ((page + 1) * MAX_COUNT < this.modelsCount) {
                page++;
                this.refreshGui();
            }
        }));
    }

    private void addTopButtons() {
        FlatColorButton customButton = new FlatColorButton(x + 5, y + 5, 80, 18, I18n.format("gui.yes_steve_model.model_manage.custom"), (b) -> {
            if (!isCustomModels) {
                isCustomModels = true;
                this.index = -1;
                page = 0;
                this.action = Action.EMPTY;
                this.refreshGui();
            }
        });
        customButton.setSelect(isCustomModels);
        addButton(customButton);

        FlatColorButton authButton = new FlatColorButton(x + 90, y + 5, 80, 18, I18n.format("gui.yes_steve_model.model_manage.auth"), (b) -> {
            if (isCustomModels) {
                isCustomModels = false;
                this.index = -1;
                page = 0;
                this.action = Action.EMPTY;
                this.refreshGui();
            }
        });
        authButton.setSelect(!isCustomModels);
        addButton(authButton);

        addButton(new FlatColorButton(x + 175, y + 5, 80, 18, I18n.format("gui.yes_steve_model.model_manage.upload"), (b) -> {
            if (UploadManager.STATUE == UploadManager.Statue.FULFILL) {
                UploadManager.FILE_PATH = "";
            }
            this.action = Action.UPLOAD;
            this.refreshGui();
            if (UploadManager.STATUE == UploadManager.Statue.FULFILL) {
                new Thread(this::getUploadFilePath).start();
            }
        }));
    }

    private void getUploadFilePath() {
        FileDialog dialog = new FileDialog((Frame) null, I18n.format("gui.yes_steve_model.model_manage.open_file"), FileDialog.LOAD);
        dialog.setMultipleMode(false);
        dialog.setVisible(true);

        String directory = dialog.getDirectory();
        String filename = dialog.getFile();
        if (StringUtils.isBlank(directory) || StringUtils.isBlank(filename)) {
            return;
        }
        File file = new File(directory, filename);
        if (file.isFile()) {
            this.uploadError = null;
            if (!file.getName().endsWith("zip") && !file.getName().endsWith("ysm")) {
                this.uploadError = I18n.format("gui.yes_steve_model.model_manage.error.format_incorrect");
                return;
            }
            if (FileUtils.sizeOf(file) > 32000) {
                this.uploadError = I18n.format("gui.yes_steve_model.model_manage.error.too_large");
                return;
            }
            UploadManager.FILE_PATH = file.getAbsolutePath();
            Minecraft.getMinecraft().addScheduledTask(this::refreshGui);
        }
    }

    private void uploadFile(String filePath, boolean isCustom) throws IOException {
        File file = Paths.get(filePath).toFile();
        if (file.isFile()) {
            String name = file.getName();
            byte[] bytes = FileUtils.readFileToByteArray(file);
            UploadFile.Dir dir = isCustom ? UploadFile.Dir.CUSTOM : UploadFile.Dir.AUTH;
            NetworkHandler.CHANNEL.sendToServer(new UploadFile(name, bytes, dir));
            UploadManager.STATUE = UploadManager.Statue.PROCESSING;
        }
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTick) {
        this.drawDefaultBackground();
        this.drawGradientRect(x, y, x + 260, y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(x + 265, y, x + 420, y + 235, 0xff_222222, 0xff_222222);
        this.drawGradientRect(x + 270, y + 5, x + 415, y + 23, 0xff_434242, 0xff_434242);

        this.drawCenteredString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.action_info"), x + 342, y + 11, 0xFFFFFF);
        this.drawString(this.fontRenderer, String.format("%d/%d", page + 1, (this.modelsCount - 1) / MAX_COUNT + 1), x + 120, y + 33, 0xFFFFFF);

        if (this.action == Action.UPLOAD) {
            String folder = isCustomModels ? I18n.format("gui.yes_steve_model.model_manage.custom") : I18n.format("gui.yes_steve_model.model_manage.auth");
            String actionName = I18n.format("gui.yes_steve_model.model_manage." + this.action.name().toLowerCase(Locale.US));
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.selected", TextFormatting.RESET + folder), x + 272, y + 29, 0xFFFFFF);
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.action", TextFormatting.RESET + actionName), x + 272, y + 39, 0xFFFFFF);
            String uploadStatue = I18n.format("gui.yes_steve_model.model_manage.upload.statue." + UploadManager.STATUE.name().toLowerCase(Locale.US));
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.upload.statue", TextFormatting.RESET + uploadStatue), x + 272, y + 49, 0xFFFFFF);
            String fileUpload = I18n.format("gui.yes_steve_model.model_manage.file.empty");
            if (StringUtils.isNoneBlank(UploadManager.FILE_PATH)) {
                fileUpload = UploadManager.FILE_PATH;
            }
            int yOffset = this.drawWordWrap(I18n.format("gui.yes_steve_model.model_manage.file", TextFormatting.RESET + fileUpload), x + 272, y + 59, 145, 0xFFFFFF);
            if (this.uploadError != null) {
                this.drawWordWrap(uploadError, x + 272, y + 60 + yOffset, 145, 0xFFFFFF);
            }
        }

        if (index >= 0 && index < getModels().size() && this.action != Action.UPLOAD) {
            RequestServerModelInfo.Info info = getModels().get(this.index);
            this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.selected", TextFormatting.RESET + info.getFileName()), x + 272, y + 29, 0xFFFFFF);
            if (this.action != Action.EMPTY) {
                String actionName = I18n.format("gui.yes_steve_model.model_manage." + this.action.name().toLowerCase(Locale.US));
                this.drawString(this.fontRenderer, I18n.format("gui.yes_steve_model.model_manage.action", TextFormatting.RESET + actionName), x + 272, y + 39, 0xFFFFFF);
            }
            if (this.action == Action.RENAME && textField != null) {
                textField.drawTextBox();
            }
        }
        super.drawScreen(pMouseX, pMouseY, pPartialTick);
    }

    private List<RequestServerModelInfo.Info> getModels() {
        if (isCustomModels) {
            return customModels;
        }
        return authModels;
    }

    @Override
    public void keyTyped(char codePoint, int modifiers) throws IOException {
        if (this.textField != null && this.textField.textboxKeyTyped(codePoint, modifiers)) return;
        super.keyTyped(codePoint, modifiers);
    }

    @Override
    public void onResize(@Nonnull Minecraft minecraft, int width, int height) {
        super.onResize(minecraft, width, height);
        if (textField != null) {
            String value = this.textField.getText();
            super.onResize(minecraft, width, height);
            this.textField.setText(value);
        }
    }

    @Override
    public void updateScreen() {
        if (textField != null) {
            this.textField.updateCursorCounter();
        }
    }

    public enum Action {
        DELETE, MOVE, RENAME, UPLOAD, EMPTY
    }
}
