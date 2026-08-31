package dev.lumungus.storage.client.screen;

import dev.lumungus.storage.menu.DriveBayMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class DriveBayScreen extends AbstractContainerScreen<DriveBayMenu> {
    private static final int COLOR_FRAME = 0xFF222629;
    private static final int COLOR_SHELL = 0xFFC9CCC8;
    private static final int COLOR_SHELL_LIGHT = 0xFFE9ECE8;
    private static final int COLOR_SCREEN = 0xFF102017;
    private static final int COLOR_SLOT = 0xFF193224;
    private static final int COLOR_GREEN_DIM = 0xFF4E8B5A;
    private static final int COLOR_GREEN = 0xFF8DDB94;
    private static final int COLOR_TEXT = 0xFF242726;

    public DriveBayScreen(DriveBayMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, DriveBayMenu.IMAGE_WIDTH, DriveBayMenu.IMAGE_HEIGHT);
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

        graphics.fill(left, top, left + imageWidth, top + imageHeight, COLOR_FRAME);
        graphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, COLOR_SHELL);
        graphics.fill(left + 5, top + 5, left + imageWidth - 5, top + 17, COLOR_SHELL_LIGHT);
        graphics.fill(left + 8, top + 24, left + 168, top + 62, COLOR_FRAME);
        graphics.fill(left + 10, top + 26, left + 166, top + 60, COLOR_SCREEN);

        for (int index = 0; index < DriveBayMenu.CELL_SLOT_COUNT; index++) {
            int x = left + 16 + index * 18;
            int y = top + 34;
            graphics.fill(x, y, x + 18, y + 18, COLOR_SLOT);
            graphics.outline(x, y, 18, 18, COLOR_GREEN_DIM);
        }

        drawInventorySlots(graphics, left, top);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, COLOR_TEXT, false);
        graphics.text(font, Component.translatable("gui.lumungus_storage.drive_bay.cells"), 12, 22, COLOR_GREEN, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_TEXT, false);
    }

    private void drawInventorySlots(GuiGraphicsExtractor graphics, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotBackground(graphics, left + 7 + column * 18, top + 83 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlotBackground(graphics, left + 7 + column * 18, top + 141);
        }
    }

    private static void drawSlotBackground(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, COLOR_FRAME);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_SHELL_LIGHT);
    }
}
