package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.gui.button.Button;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.io.IOException;

public class Screen extends GuiScreen {
    @Override
    protected void actionPerformed(@Nonnull GuiButton guiButton) throws IOException {
        if (guiButton instanceof Button button) {
            button.onPress();
            return;
        }
        super.actionPerformed(guiButton);
    }

    protected static final int LEFT_MOUSE_BUTTON = 0;
    protected static final int RIGHT_MOUSE_BUTTON = 1;
    private int lastMouseX;
    private int laseMouseY;

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == LEFT_MOUSE_BUTTON || button == RIGHT_MOUSE_BUTTON) {
            this.lastMouseX = mouseX;
            this.laseMouseY = mouseY;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        this.mouseDragged(mouseX, mouseY, button, mouseX - this.lastMouseX, mouseY - this.laseMouseY);
        this.lastMouseX = mouseX;
        this.laseMouseY = mouseY;
    }

    protected void mouseDragged(int mouseX, int mouseY, int button, int deltaX, int deltaY) {
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            this.mouseScrolled(
                    Mouse.getEventX() * this.width / this.mc.displayWidth,
                    this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1,
                    dWheel > 0 ? 1 : -1
            );
        }
    }

    protected void mouseScrolled(int mouseX, int mouseY, int delta) {
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.canGuiClose(keyCode)) super.keyTyped(typedChar, Keyboard.KEY_ESCAPE);
        else if (keyCode != Keyboard.KEY_ESCAPE) super.keyTyped(typedChar, keyCode); // 保留兼容
    }

    protected boolean canGuiClose(int keyCode) {
        return keyCode == Keyboard.KEY_ESCAPE;
    }

    /**
     * 清除 {@link #buttonList} 并发起 Forge 事件，供 Gui 自己调用。其实就是把 {@link #initGui()} 包装了一下。
     */
    protected void refreshGui() {
        if (!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
            this.buttonList.clear();
            this.initGui();
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
    }

    @Override
    public void drawCenteredString(@Nonnull FontRenderer font, @Nonnull String text, int x, int y, int color) {
        super.drawCenteredString(font, text, x, y, color);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void drawString(@Nonnull FontRenderer font, @Nonnull String text, int x, int y, int color) {
        super.drawString(font, text, x, y, color);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * {@link KeyBinding#isActiveAndMatches(int)}，但是不检测 {@link KeyConflictContext#isActive()}。
     */
    public static boolean isKeyActiveIgnoreConflict(KeyBinding keyBinding, int keyCode) {
        return keyCode != 0 && keyCode == keyBinding.getKeyCode() && keyBinding.getKeyModifier().equals(KeyModifier.getActiveModifier());
    }
}
