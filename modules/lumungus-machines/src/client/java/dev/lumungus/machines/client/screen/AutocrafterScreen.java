package dev.lumungus.machines.client.screen;

import dev.lumungus.machines.menu.AutocrafterMenu;
import dev.lumungus.machines.network.AutocrafterActionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public final class AutocrafterScreen extends AbstractContainerScreen<AutocrafterMenu> {
    private static final int COLOR_FRAME = 0xFF222629;
    private static final int COLOR_SHELL = 0xFFD1D6D2;
    private static final int COLOR_SHELL_LIGHT = 0xFFF1F4F0;
    private static final int COLOR_SCREEN = 0xFF102017;
    private static final int COLOR_SLOT = 0xFF193224;
    private static final int COLOR_GREEN = 0xFF8DDB94;
    private static final int COLOR_GREEN_DIM = 0xFF4E8B5A;
    private static final int COLOR_GREEN_DARK = 0xFF1B3B28;
    private static final int COLOR_AMBER = 0xFFFFC857;
    private static final int COLOR_COPPER = 0xFFC97C4C;
    private static final int COLOR_TEXT = 0xFF242726;

    private static final int AMOUNT_X = 149;
    private static final int AMOUNT_Y = 38;
    private static final int CONTROL_WIDTH = 61;
    private static final int CONTROL_HEIGHT = 18;
    private static final int APPLY_Y = 59;
    private static final int TOGGLE_Y = 80;

    private EditBox amountBox;
    private long lastSyncedTargetAmount;
    private boolean amountEdited;
    private boolean applyingServerValue;
    private static String lastAmountValueForTests = "";

    public AutocrafterScreen(AutocrafterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, AutocrafterMenu.IMAGE_WIDTH, AutocrafterMenu.IMAGE_HEIGHT);
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 34;
        inventoryLabelY = 109;
    }

    @Override
    protected void init() {
        super.init();
        amountBox = new EditBox(
                font,
                leftPos + AMOUNT_X + 3,
                topPos + AMOUNT_Y + 4,
                CONTROL_WIDTH - 6,
                12,
                Component.translatable("gui.lumungus_machines.autocrafter.amount")
        );
        amountBox.setMaxLength(7);
        amountBox.setBordered(false);
        amountBox.setTextColor(COLOR_GREEN);
        amountBox.setTextColorUneditable(COLOR_GREEN_DIM);
        amountBox.setValue(Long.toString(Math.max(1, menu.targetAmount())));
        lastSyncedTargetAmount = menu.targetAmount();
        amountBox.setResponder(value -> {
            lastAmountValueForTests = value;
            if (!applyingServerValue) {
                amountEdited = true;
            }
        });
        addRenderableWidget(amountBox);
        setInitialFocus(amountBox);
        amountBox.setFocused(true);
        lastAmountValueForTests = amountBox.getValue();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        long syncedTargetAmount = menu.targetAmount();
        if (syncedTargetAmount == lastSyncedTargetAmount) {
            return;
        }

        lastSyncedTargetAmount = syncedTargetAmount;
        if (!amountEdited) {
            applyingServerValue = true;
            amountBox.setValue(Long.toString(Math.max(1, syncedTargetAmount)));
            applyingServerValue = false;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int left = leftPos;
        int top = topPos;
        graphics.fill(left, top, left + imageWidth, top + imageHeight, COLOR_FRAME);
        graphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, COLOR_SHELL);
        graphics.fill(left + 5, top + 5, left + imageWidth - 5, top + 17, COLOR_SHELL_LIGHT);

        graphics.fill(left + 12, top + 23, left + 137, top + 96, COLOR_SCREEN);
        graphics.outline(left + 12, top + 23, 125, 73, COLOR_GREEN_DIM);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                drawSlot(graphics, left + 23 + column * 18, top + 34 + row * 18);
            }
        }
        drawSlot(graphics, left + 111, top + 52);
        graphics.fill(left + 86, top + 60, left + 101, top + 63, COLOR_COPPER);
        graphics.fill(left + 97, top + 57, left + 101, top + 66, COLOR_COPPER);

        drawControl(graphics, left, top, AMOUNT_Y, COLOR_GREEN_DARK);
        drawControl(graphics, left, top, APPLY_Y, COLOR_COPPER);
        drawControl(graphics, left, top, TOGGLE_Y, menu.paused() ? COLOR_GREEN_DIM : COLOR_AMBER);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawInventorySlot(graphics, left + 33 + column * 18, top + 120 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawInventorySlot(graphics, left + 33 + column * 18, top + 178);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, COLOR_TEXT, false);
        graphics.text(font, Component.translatable("gui.lumungus_machines.autocrafter.recipe"), 17, 25, COLOR_GREEN_DIM, false);
        graphics.centeredText(font, Component.translatable("gui.lumungus_machines.autocrafter.apply"), AMOUNT_X + CONTROL_WIDTH / 2, APPLY_Y + 5, COLOR_TEXT);
        graphics.centeredText(
                font,
                Component.translatable(menu.paused()
                        ? "gui.lumungus_machines.autocrafter.start"
                        : "gui.lumungus_machines.autocrafter.pause"),
                AMOUNT_X + CONTROL_WIDTH / 2,
                TOGGLE_Y + 5,
                COLOR_TEXT
        );
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_TEXT, false);
        Component progress = Component.translatable(
                "gui.lumungus_machines.autocrafter.progress",
                menu.completedAmount(),
                menu.targetAmount()
        );
        graphics.text(font, progress, 13, 97, COLOR_TEXT, false);
        graphics.text(
                font,
                Component.translatable(menu.state().translationKey() + ".short"),
                149,
                25,
                menu.paused() ? COLOR_GREEN_DIM : COLOR_COPPER,
                false
        );
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (inside(event.x(), event.y(), APPLY_Y)) {
            applyAmount();
            return true;
        }
        if (inside(event.x(), event.y(), TOGGLE_Y)) {
            send(AutocrafterActionPayload.Action.TOGGLE_PAUSED, 0);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (amountBox != null && amountBox.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                return super.keyPressed(event);
            }
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                applyAmount();
                return true;
            }
            amountBox.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (amountBox != null && amountBox.isFocused()) {
            amountBox.charTyped(event);
            return true;
        }
        return super.charTyped(event);
    }

    private void applyAmount() {
        try {
            long amount = Long.parseLong(amountBox.getValue());
            if (amount > 0 && amount <= 9_999_999L) {
                send(AutocrafterActionPayload.Action.APPLY_AMOUNT, amount);
                lastSyncedTargetAmount = amount;
                amountEdited = false;
            }
        } catch (NumberFormatException ignored) {
            amountBox.setValue(Long.toString(Math.max(1, menu.targetAmount())));
        }
    }

    public static String lastAmountValueForTests() {
        return lastAmountValueForTests;
    }

    private void send(AutocrafterActionPayload.Action action, long amount) {
        if (ClientPlayNetworking.canSend(AutocrafterActionPayload.TYPE)) {
            ClientPlayNetworking.send(new AutocrafterActionPayload(menu.containerId, action, amount));
        }
    }

    private boolean inside(double mouseX, double mouseY, int y) {
        return mouseX >= leftPos + AMOUNT_X
                && mouseX < leftPos + AMOUNT_X + CONTROL_WIDTH
                && mouseY >= topPos + y
                && mouseY < topPos + y + CONTROL_HEIGHT;
    }

    private static void drawSlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, COLOR_FRAME);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_SLOT);
        graphics.outline(x, y, 18, 18, COLOR_GREEN_DIM);
    }

    private static void drawInventorySlot(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, COLOR_FRAME);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_SHELL_LIGHT);
    }

    private static void drawControl(GuiGraphicsExtractor graphics, int left, int top, int y, int fill) {
        graphics.fill(left + AMOUNT_X, top + y, left + AMOUNT_X + CONTROL_WIDTH, top + y + CONTROL_HEIGHT, COLOR_FRAME);
        graphics.fill(left + AMOUNT_X + 1, top + y + 1, left + AMOUNT_X + CONTROL_WIDTH - 1, top + y + CONTROL_HEIGHT - 1, fill);
    }
}
