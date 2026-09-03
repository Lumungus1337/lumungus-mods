package dev.lumungus.storage.client.screen;

import dev.lumungus.storage.menu.WirelessModuleMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class WirelessModuleScreen extends AbstractContainerScreen<WirelessModuleMenu> {
    private static final int FRAME = 0xFF222629;
    private static final int SHELL = 0xFFC9CCC8;
    private static final int LIGHT = 0xFFE9ECE8;
    private static final int SCREEN = 0xFF102017;
    private static final int GREEN = 0xFF8DDB94;
    private static final int TEXT = 0xFF242726;

    public WirelessModuleScreen(WirelessModuleMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WirelessModuleMenu.IMAGE_WIDTH, WirelessModuleMenu.IMAGE_HEIGHT);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = 72;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int left = leftPos;
        int top = topPos;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, FRAME);
        graphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, SHELL);
        graphics.fill(left + 5, top + 5, left + imageWidth - 5, top + 17, LIGHT);
        graphics.fill(left + 8, top + 24, left + 168, top + 62, FRAME);
        graphics.fill(left + 10, top + 26, left + 166, top + 60, SCREEN);
        graphics.fill(left + 79, top + 34, left + 97, top + 52, 0xFF193224);
        graphics.outline(left + 79, top + 34, 18, 18, 0xFF4E8B5A);
        drawInventorySlots(graphics, left, top);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT, false);
        graphics.text(font, Component.translatable("gui.lumungus_storage.wireless_module.slot"), 12, 22, GREEN, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }

    private static void drawInventorySlots(GuiGraphicsExtractor graphics, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, left + 7 + column * 18, top + 83 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, left + 7 + column * 18, top + 141);
        }
    }

    private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, FRAME);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, LIGHT);
    }
}
