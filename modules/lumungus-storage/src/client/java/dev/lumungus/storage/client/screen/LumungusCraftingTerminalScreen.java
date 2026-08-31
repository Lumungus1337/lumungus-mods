package dev.lumungus.storage.client.screen;

import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.network.TerminalActionPayload;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class LumungusCraftingTerminalScreen extends AbstractContainerScreen<LumungusCraftingMenu> {
    private static final int NETWORK_COLUMNS = 7;
    private static final int NETWORK_ROWS = 4;
    private static final int NETWORK_PAGE_SIZE = NETWORK_COLUMNS * NETWORK_ROWS;
    private static final int NETWORK_SLOT_SIZE = 18;
    private static final int NETWORK_COLUMN_STEP = 22;
    private static final int NETWORK_X = 8;
    private static final int NETWORK_Y = 44;
    private static final int DEPOSIT_X = 174;
    private static final int DEPOSIT_Y = 44;
    private static final int BAY_MOVE_X = 174;
    private static final int BAY_MOVE_Y = 68;
    private static final int BAY_MOVE_WIDTH = 20;
    private static final int SHULKER_MODE_Y = 92;
    private static final int SHULKER_MODE_WIDTH = 28;
    private static final int SORT_X = 124;
    private static final int SORT_Y = 18;
    private static final int SORT_WIDTH = 44;
    private static final int SEARCH_X = 8;
    private static final int SEARCH_Y = 18;
    private static final int SEARCH_WIDTH = 112;
    private static final int PAGE_Y = 116;

    private static final int COLOR_FRAME = 0xFF222629;
    private static final int COLOR_SHELL = 0xFFC9CCC8;
    private static final int COLOR_SHELL_LIGHT = 0xFFE9ECE8;
    private static final int COLOR_SCREEN = 0xFF102017;
    private static final int COLOR_SCREEN_SLOT = 0xFF193224;
    private static final int COLOR_SCREEN_HOVER = 0xFF28513A;
    private static final int COLOR_GREEN = 0xFF8DDB94;
    private static final int COLOR_GREEN_DIM = 0xFF4E8B5A;
    private static final int COLOR_AMBER = 0xFFFFC857;
    private static final int COLOR_TEXT = 0xFF242726;

    private EditBox searchBox;
    private SortMode sortMode = SortMode.NAME;
    private boolean shulkerExtractMode;
    private int page;

    public LumungusCraftingTerminalScreen(
            LumungusCraftingMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, LumungusCraftingMenu.IMAGE_WIDTH, LumungusCraftingMenu.IMAGE_HEIGHT);
        titleLabelX = 8;
        titleLabelY = 5;
        inventoryLabelX = 73;
        inventoryLabelY = 141;
    }

    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(
                font,
                leftPos + SEARCH_X,
                topPos + SEARCH_Y,
                SEARCH_WIDTH,
                18,
                Component.translatable("gui.lumungus_storage.crafting_terminal.search")
        );
        searchBox.setMaxLength(64);
        searchBox.setBordered(false);
        searchBox.setTextColor(COLOR_GREEN);
        searchBox.setTextColorUneditable(COLOR_GREEN_DIM);
        searchBox.setHint(Component.translatable("gui.lumungus_storage.crafting_terminal.search"));
        searchBox.setResponder(value -> page = 0);
        addRenderableWidget(searchBox);
        setInitialFocus(searchBox);
        searchBox.setFocused(true);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int left = leftPos;
        int top = topPos;

        graphics.fill(left, top, left + imageWidth, top + imageHeight, COLOR_FRAME);
        graphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, COLOR_SHELL);
        graphics.fill(left + 5, top + 5, left + imageWidth - 5, top + 15, COLOR_SHELL_LIGHT);

        graphics.fill(left + 5, top + 16, left + 171, top + 139, COLOR_FRAME);
        graphics.fill(left + 7, top + 18, left + 169, top + 137, COLOR_SCREEN);
        graphics.outline(left + SEARCH_X - 1, top + SEARCH_Y - 1, SEARCH_WIDTH + 2, 20, COLOR_GREEN_DIM);
        graphics.outline(left + 196, top + 17, 113, 92, COLOR_FRAME);
        graphics.fill(left + 197, top + 18, left + 308, top + 108, COLOR_SHELL_LIGHT);

        List<ResourceAmount> resources = filteredResources();
        int pageCount = Math.max(1, (resources.size() + NETWORK_PAGE_SIZE - 1) / NETWORK_PAGE_SIZE);
        page = Math.min(page, pageCount - 1);
        drawButton(graphics, SORT_X, SORT_Y, SORT_WIDTH, 18, true);
        drawButton(graphics, 8, PAGE_Y, 20, 14, page > 0);
        drawButton(graphics, 149, PAGE_Y, 20, 14, page < pageCount - 1);

        for (int row = 0; row < NETWORK_ROWS; row++) {
            for (int column = 0; column < NETWORK_COLUMNS; column++) {
                int x = left + NETWORK_X + column * NETWORK_COLUMN_STEP;
                int y = top + NETWORK_Y + row * 18;
                boolean hovered = isPointInside(mouseX, mouseY, x, y, 18, 18);
                graphics.fill(
                        x,
                        y,
                        x + 18,
                        y + 18,
                        hovered ? COLOR_SCREEN_HOVER : COLOR_SCREEN_SLOT
                );
                graphics.outline(x, y, 18, 18, COLOR_GREEN_DIM);
            }
        }

        int depositLeft = left + DEPOSIT_X;
        int depositTop = top + DEPOSIT_Y;
        boolean depositHovered = isPointInside(mouseX, mouseY, depositLeft, depositTop, 18, 18);
        graphics.fill(
                depositLeft,
                depositTop,
                depositLeft + 18,
                depositTop + 18,
                depositHovered ? 0xFF664F20 : COLOR_FRAME
        );
        graphics.outline(depositLeft, depositTop, 18, 18, COLOR_AMBER);
        drawButton(
                graphics,
                BAY_MOVE_X,
                BAY_MOVE_Y,
                BAY_MOVE_WIDTH,
                18,
                true
        );
        drawButton(
                graphics,
                BAY_MOVE_X,
                SHULKER_MODE_Y,
                SHULKER_MODE_WIDTH,
                18,
                true
        );

        drawVanillaSlotBackgrounds(graphics, left, top);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, COLOR_TEXT, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_TEXT, false);
        graphics.text(
                font,
                Component.translatable("gui.lumungus_storage.crafting_terminal.crafting"),
                199,
                6,
                COLOR_TEXT,
                false
        );
        graphics.centeredText(font, sortMode.label(), SORT_X + SORT_WIDTH / 2, SORT_Y + 5, COLOR_GREEN);
        graphics.text(font, Component.translatable("gui.lumungus_storage.crafting_terminal.search_label"), SEARCH_X, 31, COLOR_GREEN_DIM, false);
        graphics.centeredText(font, "<", 18, PAGE_Y + 3, COLOR_GREEN);
        graphics.centeredText(font, ">", 159, PAGE_Y + 3, COLOR_GREEN);
        graphics.centeredText(font, "IN", DEPOSIT_X + 9, DEPOSIT_Y + 5, COLOR_AMBER);
        graphics.centeredText(font, "BAY", BAY_MOVE_X + BAY_MOVE_WIDTH / 2, BAY_MOVE_Y + 5, COLOR_GREEN);
        graphics.centeredText(
                font,
                shulkerExtractMode ? "BOX" : "ITM",
                BAY_MOVE_X + SHULKER_MODE_WIDTH / 2,
                SHULKER_MODE_Y + 5,
                shulkerExtractMode ? COLOR_AMBER : COLOR_GREEN
        );

        List<ResourceAmount> resources = filteredResources();
        int pageCount = Math.max(1, (resources.size() + NETWORK_PAGE_SIZE - 1) / NETWORK_PAGE_SIZE);
        page = Math.min(page, pageCount - 1);
        graphics.centeredText(
                font,
                Component.translatable(
                        "gui.lumungus_storage.crafting_terminal.page",
                        page + 1,
                        pageCount
                ),
                88,
                PAGE_Y + 3,
                COLOR_GREEN
        );

        int start = page * NETWORK_PAGE_SIZE;
        if (resources.isEmpty()) {
            graphics.centeredText(
                    font,
                    Component.translatable(searchBox.getValue().isBlank()
                            ? "gui.lumungus_storage.crafting_terminal.empty"
                            : "gui.lumungus_storage.crafting_terminal.no_match"),
                    88,
                    80,
                    COLOR_GREEN_DIM
            );
        }
        for (int visibleIndex = 0; visibleIndex < NETWORK_PAGE_SIZE; visibleIndex++) {
            int resourceIndex = start + visibleIndex;
            if (resourceIndex >= resources.size()) {
                break;
            }
            ResourceAmount resource = resources.get(resourceIndex);
            int x = NETWORK_X + visibleIndex % NETWORK_COLUMNS * NETWORK_COLUMN_STEP + 1;
            int y = NETWORK_Y + visibleIndex / NETWORK_COLUMNS * 18 + 1;
            graphics.item(resource.stack(), x, y);
            graphics.itemDecorations(font, resource.stack(), x, y, formatAmount(resource.amount()));
        }

        graphics.text(
                font,
                Component.translatable(
                        "gui.lumungus_storage.crafting_terminal.status",
                        menu.networkStoredAmount(),
                        menu.networkTotalCapacity(),
                        menu.networkStoredTypes(),
                        menu.networkTotalTypeCapacity()
                ),
                8,
                130,
                COLOR_GREEN,
                false
        );
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        ResourceAmount hovered = resourceAt(mouseX, mouseY);
        if (hovered != null) {
            List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(hovered.stack()));
            tooltip.add(Component.translatable(
                    "gui.lumungus_storage.crafting_terminal.amount",
                    hovered.amount()
            ));
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            return;
        }
        if (isPointInside(
                mouseX,
                mouseY,
                leftPos + DEPOSIT_X,
                topPos + DEPOSIT_Y,
                18,
                18
        )) {
            graphics.setTooltipForNextFrame(
                    font,
                    Component.translatable("gui.lumungus_storage.crafting_terminal.deposit"),
                    mouseX,
                    mouseY
            );
            return;
        }
        if (isPointInside(
                mouseX,
                mouseY,
                leftPos + BAY_MOVE_X,
                topPos + BAY_MOVE_Y,
                BAY_MOVE_WIDTH,
                18
        )) {
            graphics.setTooltipForNextFrame(
                    font,
                    Component.translatable("gui.lumungus_storage.crafting_terminal.move_to_bays"),
                    mouseX,
                    mouseY
            );
            return;
        }
        if (isPointInside(
                mouseX,
                mouseY,
                leftPos + BAY_MOVE_X,
                topPos + SHULKER_MODE_Y,
                SHULKER_MODE_WIDTH,
                18
        )) {
            graphics.setTooltipForNextFrame(
                    font,
                    Component.translatable(shulkerExtractMode
                            ? "gui.lumungus_storage.crafting_terminal.extract_mode_shulker"
                            : "gui.lumungus_storage.crafting_terminal.extract_mode_items"),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (searchBox != null && searchBox.isMouseOver(mouseX, mouseY)) {
            return super.mouseClicked(event, doubleClick);
        }

        if (isPointInside(mouseX, mouseY, leftPos + SORT_X, topPos + SORT_Y, SORT_WIDTH, 18)) {
            sortMode = sortMode.next();
            page = 0;
            return true;
        }
        if (isPointInside(mouseX, mouseY, leftPos + 8, topPos + PAGE_Y, 20, 14)) {
            page = Math.max(0, page - 1);
            return true;
        }
        if (isPointInside(mouseX, mouseY, leftPos + 149, topPos + PAGE_Y, 20, 14)) {
            int pageCount = Math.max(1, (filteredResources().size() + NETWORK_PAGE_SIZE - 1)
                    / NETWORK_PAGE_SIZE);
            page = Math.min(pageCount - 1, page + 1);
            return true;
        }
        if (isPointInside(
                mouseX,
                mouseY,
                leftPos + DEPOSIT_X,
                topPos + DEPOSIT_Y,
                18,
                18
        )) {
            sendAction(
                    event.button() == 1
                            ? TerminalActionPayload.Action.DEPOSIT_ONE_CARRIED
                            : TerminalActionPayload.Action.DEPOSIT_CARRIED_STACK,
                    ItemStack.EMPTY
            );
            return true;
        }
        if (isPointInside(
                mouseX,
                mouseY,
                leftPos + BAY_MOVE_X,
                topPos + BAY_MOVE_Y,
                BAY_MOVE_WIDTH,
                18
        )) {
            sendAction(TerminalActionPayload.Action.MOVE_PHYSICAL_TO_DRIVE_BAYS, ItemStack.EMPTY);
            return true;
        }
        if (isPointInside(
                mouseX,
                mouseY,
                leftPos + BAY_MOVE_X,
                topPos + SHULKER_MODE_Y,
                SHULKER_MODE_WIDTH,
                18
        )) {
            shulkerExtractMode = !shulkerExtractMode;
            return true;
        }

        ResourceAmount clicked = resourceAt(mouseX, mouseY);
        if (clicked != null) {
            TerminalActionPayload.Action action;
            if (shulkerExtractMode && event.hasShiftDown()) {
                action = TerminalActionPayload.Action.EXTRACT_SHULKER_TO_INVENTORY;
            } else if (shulkerExtractMode) {
                action = TerminalActionPayload.Action.EXTRACT_SHULKER_TO_CURSOR;
            } else if (event.hasShiftDown()) {
                action = TerminalActionPayload.Action.EXTRACT_STACK_TO_INVENTORY;
            } else if (event.button() == 1) {
                action = TerminalActionPayload.Action.EXTRACT_ONE_TO_CURSOR;
            } else {
                action = TerminalActionPayload.Action.EXTRACT_STACK_TO_CURSOR;
            }
            sendAction(action, clicked.stack());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (isPointInside(
                mouseX,
                mouseY,
                leftPos + NETWORK_X,
                topPos + NETWORK_Y,
                NETWORK_COLUMNS * NETWORK_COLUMN_STEP,
                NETWORK_ROWS * 18
        )) {
            int pageCount = Math.max(1, (filteredResources().size() + NETWORK_PAGE_SIZE - 1)
                    / NETWORK_PAGE_SIZE);
            if (verticalAmount < 0) {
                page = Math.min(pageCount - 1, page + 1);
            } else if (verticalAmount > 0) {
                page = Math.max(0, page - 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void sendAction(TerminalActionPayload.Action action, ItemStack template) {
        if (ClientPlayNetworking.canSend(TerminalActionPayload.TYPE)) {
            ClientPlayNetworking.send(new TerminalActionPayload(menu.containerId, action, template));
        }
    }

    private List<ResourceAmount> filteredResources() {
        String query = searchBox == null ? "" : searchBox.getValue().strip().toLowerCase(Locale.ROOT);
        Comparator<ResourceAmount> comparator = sortMode == SortMode.NAME
                ? Comparator.comparing(resource -> resource.stack().getHoverName().getString().toLowerCase(Locale.ROOT))
                : Comparator.comparingLong(ResourceAmount::amount).reversed()
                .thenComparing(resource -> resource.stack().getHoverName().getString());
        return menu.networkResources().stream()
                .filter(resource -> query.isEmpty()
                        || searchableText(resource).contains(query))
                .sorted(comparator)
                .toList();
    }

    private static String searchableText(ResourceAmount resource) {
        String displayName = resource.stack().getHoverName().getString().toLowerCase(Locale.ROOT);
        String itemId = BuiltInRegistries.ITEM.getKey(resource.stack().getItem()).toString().toLowerCase(Locale.ROOT);
        return displayName + " " + itemId;
    }

    private ResourceAmount resourceAt(double mouseX, double mouseY) {
        int relativeX = (int) mouseX - leftPos - NETWORK_X;
        int relativeY = (int) mouseY - topPos - NETWORK_Y;
        if (relativeX < 0
                || relativeY < 0
                || relativeX >= NETWORK_COLUMNS * NETWORK_COLUMN_STEP
                || relativeY >= NETWORK_ROWS * 18) {
            return null;
        }
        int column = relativeX / NETWORK_COLUMN_STEP;
        if (relativeX % NETWORK_COLUMN_STEP >= NETWORK_SLOT_SIZE) {
            return null;
        }
        int row = relativeY / 18;
        int resourceIndex = page * NETWORK_PAGE_SIZE + row * NETWORK_COLUMNS + column;
        List<ResourceAmount> resources = filteredResources();
        return resourceIndex < resources.size() ? resources.get(resourceIndex) : null;
    }

    private void drawButton(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            boolean active
    ) {
        int left = leftPos + x;
        int top = topPos + y;
        graphics.fill(left, top, left + width, top + height, active ? COLOR_SCREEN : COLOR_FRAME);
        graphics.outline(left, top, width, height, active ? COLOR_GREEN_DIM : 0xFF4A4E4C);
    }

    private void drawVanillaSlotBackgrounds(GuiGraphicsExtractor graphics, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                drawSlotBackground(graphics, left + 198 + column * 18, top + 26 + row * 18);
            }
        }
        drawSlotBackground(graphics, left + 268, top + 44);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotBackground(graphics, left + 72 + column * 18, top + 153 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlotBackground(graphics, left + 72 + column * 18, top + 211);
        }
    }

    private static void drawSlotBackground(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, COLOR_FRAME);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_SHELL_LIGHT);
    }

    private static boolean isPointInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }

    private static String formatAmount(long amount) {
        if (amount >= 1_000_000) {
            return amount / 1_000_000 + "M";
        }
        if (amount >= 1_000) {
            return amount / 1_000 + "K";
        }
        return Long.toString(amount);
    }

    private enum SortMode {
        NAME(Component.literal("A-Z")),
        AMOUNT(Component.literal("#"));

        private final Component label;

        SortMode(Component label) {
            this.label = label;
        }

        public Component label() {
            return label;
        }

        public SortMode next() {
            return this == NAME ? AMOUNT : NAME;
        }
    }
}
