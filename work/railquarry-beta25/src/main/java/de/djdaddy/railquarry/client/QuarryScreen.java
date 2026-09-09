package de.djdaddy.railquarry.client;

import de.djdaddy.railquarry.menu.QuarryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public final class QuarryScreen extends AbstractContainerScreen<QuarryMenu> {
    private EditBox heightBox;
    private int syncedHeight;
    private Button minus;
    private Button plus;
    private Button apply;

    public QuarryScreen(QuarryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 288, 222);
        inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        syncedHeight = menu.miningHeight();
        heightBox = new EditBox(font, leftPos + 207, topPos + 42, 33, 18,
                Component.translatable("gui.railquarry.height"));
        heightBox.setMaxLength(2);
        heightBox.setValue(Integer.toString(syncedHeight));
        addRenderableWidget(heightBox);
        minus = addRenderableWidget(Button.builder(Component.literal("-"), b -> send(parsedHeight() - 1))
                .bounds(leftPos + 184, topPos + 42, 20, 18).build());
        plus = addRenderableWidget(Button.builder(Component.literal("+"), b -> send(parsedHeight() + 1))
                .bounds(leftPos + 243, topPos + 42, 20, 18).build());
        apply = addRenderableWidget(Button.builder(Component.translatable("gui.railquarry.apply"), b -> send(parsedHeight()))
                .bounds(leftPos + 184, topPos + 68, 96, 18).build());
        updateButtons();
    }

    private int parsedHeight() {
        try { return Integer.parseInt(heightBox.getValue()); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private void updateButtons() {
        int value = parsedHeight();
        minus.active = value > 1 && value <= 64;
        plus.active = value >= 1 && value < 64;
        apply.active = value >= 1 && value <= 64;
        heightBox.setTextColor(apply.active ? 0xFFE2EEE8 : 0xFFFF7777);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (syncedHeight != menu.miningHeight()) {
            syncedHeight = menu.miningHeight();
            heightBox.setValue(Integer.toString(syncedHeight));
        }
        updateButtons();
    }

    private void send(int value) {
        if (value < 1 || value > 64) return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, value);
        heightBox.setValue(Integer.toString(value));
        updateButtons();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF24282A);
        graphics.outline(leftPos, topPos, imageWidth, imageHeight, 0xFFBA8059);
        for (var slot : menu.slots) {
            int x = leftPos + slot.x - 1;
            int y = topPos + slot.y - 1;
            graphics.fill(x, y, x + 18, y + 18, 0xFF15191A);
            graphics.outline(x, y, 18, 18, 0xFF5E6A65);
        }
        graphics.fill(leftPos + 175, topPos + 8, leftPos + 176, topPos + 214, 0xFFBA8059);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, 8, 6, 0xFFE2EEE8, false);
        graphics.text(font, playerInventoryTitle, 8, inventoryLabelY, 0xFFB4C6BE, false);
        graphics.text(font, Component.translatable("gui.railquarry.height"), 184, 18, 0xFFB4C6BE, false);
        graphics.text(font, Component.literal("1-64"), 184, 29, 0xFFB4C6BE, false);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (heightBox.isFocused() && event.key() != GLFW.GLFW_KEY_ESCAPE) {
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) send(parsedHeight());
            else heightBox.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (heightBox.isFocused()) {
            heightBox.charTyped(event);
            return true;
        }
        return super.charTyped(event);
    }
}
