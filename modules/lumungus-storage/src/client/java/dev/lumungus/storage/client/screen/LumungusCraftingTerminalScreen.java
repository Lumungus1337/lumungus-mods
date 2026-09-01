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
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public final class LumungusCraftingTerminalScreen extends AbstractContainerScreen<LumungusCraftingMenu> {
    private static final int NETWORK_COLUMNS = 6;
    private static final int NETWORK_ROWS = 4;
    private static final int NETWORK_PAGE_SIZE = NETWORK_COLUMNS * NETWORK_ROWS;
    private static final int NETWORK_SLOT_SIZE = 18;
    private static final int NETWORK_COLUMN_STEP = 22;
    private static final int NETWORK_X = 14;
    private static final int NETWORK_Y = 49;
    private static final int LEFT_PANEL_X = 8;
    private static final int LEFT_PANEL_Y = 18;
    private static final int LEFT_PANEL_WIDTH = 154;
    private static final int LEFT_PANEL_HEIGHT = 122;
    private static final int ACTION_X = 168;
    private static final int ACTION_WIDTH = 23;
    private static final int DEPOSIT_Y = 49;
    private static final int BAY_MOVE_Y = 76;
    private static final int SHULKER_MODE_Y = 103;
    private static final int SORT_X = 126;
    private static final int SORT_Y = 25;
    private static final int SORT_WIDTH = 30;
    private static final int SEARCH_X = 14;
    private static final int SEARCH_Y = 25;
    private static final int SEARCH_WIDTH = 106;
    private static final int PAGE_Y = 122;
    private static final int CRAFT_PANEL_X = 198;
    private static final int CRAFT_PANEL_Y = 18;
    private static final int CRAFT_PANEL_WIDTH = 126;
    private static final int CRAFT_PANEL_HEIGHT = 105;
    private static final int PLAYER_INV_X = 84;
    private static final int PLAYER_INV_Y = 147;
    private static final int PLAYER_INV_WIDTH = 168;
    private static final int PLAYER_INV_HEIGHT = 81;

    private static final int COLOR_FRAME = 0xFF222629;
    private static final int COLOR_SHELL = 0xFFD1D6D2;
    private static final int COLOR_SHELL_LIGHT = 0xFFF1F4F0;
    private static final int COLOR_PANEL = 0xFFE5E9E4;
    private static final int COLOR_PANEL_SHADOW = 0xFF9AA29B;
    private static final int COLOR_SCREEN = 0xFF102017;
    private static final int COLOR_SCREEN_SLOT = 0xFF193224;
    private static final int COLOR_SCREEN_HOVER = 0xFF28513A;
    private static final int COLOR_GREEN = 0xFF8DDB94;
    private static final int COLOR_GREEN_DIM = 0xFF4E8B5A;
    private static final int COLOR_GREEN_DARK = 0xFF1B3B28;
    private static final int COLOR_AMBER = 0xFFFFC857;
    private static final int COLOR_COPPER = 0xFFC97C4C;
    private static final int COLOR_TEXT = 0xFF242726;
    private static final int COLOR_TEXT_DIM = 0xFF5B605D;

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
        inventoryLabelX = PLAYER_INV_X + 2;
        inventoryLabelY = 139;
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
        searchBox.setCanLoseFocus(false);
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
        graphics.fill(left + 5, top + 5, left + imageWidth - 5, top + 14, COLOR_SHELL_LIGHT);

        drawPanel(graphics, left + LEFT_PANEL_X, top + LEFT_PANEL_Y, LEFT_PANEL_WIDTH, LEFT_PANEL_HEIGHT, COLOR_SCREEN);
        graphics.fill(left + SEARCH_X - 1, top + SEARCH_Y - 1, left + SEARCH_X + SEARCH_WIDTH + 1, top + SEARCH_Y + 17, COLOR_GREEN_DARK);
        graphics.outline(left + SEARCH_X - 1, top + SEARCH_Y - 1, SEARCH_WIDTH + 2, 18, COLOR_GREEN_DIM);
        drawPanel(graphics, left + CRAFT_PANEL_X, top + CRAFT_PANEL_Y, CRAFT_PANEL_WIDTH, CRAFT_PANEL_HEIGHT, COLOR_PANEL);
        drawPanel(graphics, left + PLAYER_INV_X, top + PLAYER_INV_Y, PLAYER_INV_WIDTH, PLAYER_INV_HEIGHT, COLOR_PANEL);

        List<ResourceAmount> resources = filteredResources();
        int pageCount = Math.max(1, (resources.size() + NETWORK_PAGE_SIZE - 1) / NETWORK_PAGE_SIZE);
        page = Math.min(page, pageCount - 1);

        drawButton(graphics, SORT_X, SORT_Y, SORT_WIDTH, 18, true, COLOR_GREEN_DIM);
        drawButton(graphics, NETWORK_X, PAGE_Y, 20, 12, page > 0, COLOR_GREEN_DIM);
        drawButton(graphics, NETWORK_X + 112, PAGE_Y, 20, 12, page < pageCount - 1, COLOR_GREEN_DIM);
        drawActionButton(graphics, ACTION_X, DEPOSIT_Y, COLOR_AMBER, isPointInside(mouseX, mouseY, left + ACTION_X, top + DEPOSIT_Y, ACTION_WIDTH, 18));
        drawActionButton(graphics, ACTION_X, BAY_MOVE_Y, COLOR_GREEN_DIM, false);
        drawActionButton(graphics, ACTION_X, SHULKER_MODE_Y, shulkerExtractMode ? COLOR_AMBER : COLOR_GREEN_DIM, false);

        drawNetworkSlots(graphics, left, top, mouseX, mouseY);
        drawCraftingArrow(graphics, left, top);
        drawVanillaSlotBackgrounds(graphics, left, top);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, Component.literal("Lumungus Terminal"), titleLabelX, titleLabelY, COLOR_TEXT, false);
        graphics.text(font, Component.literal("LAGER"), LEFT_PANEL_X + 7, LEFT_PANEL_Y + 27, COLOR_GREEN_DIM, false);
        graphics.centeredText(font, sortMode.label(), SORT_X + SORT_WIDTH / 2, SORT_Y + 5, COLOR_GREEN);
        graphics.centeredText(font, "<", NETWORK_X + 10, PAGE_Y + 2, COLOR_GREEN);
        graphics.centeredText(font, ">", NETWORK_X + 122, PAGE_Y + 2, COLOR_GREEN);
        graphics.centeredText(font, "IN", ACTION_X + ACTION_WIDTH / 2, DEPOSIT_Y + 5, COLOR_AMBER);
        graphics.centeredText(font, "B", ACTION_X + ACTION_WIDTH / 2, BAY_MOVE_Y + 5, COLOR_GREEN);
        graphics.centeredText(
                font,
                shulkerExtractMode ? "S" : "I",
                ACTION_X + ACTION_WIDTH / 2,
                SHULKER_MODE_Y + 5,
                shulkerExtractMode ? COLOR_AMBER : COLOR_GREEN
        );
        graphics.text(
                font,
                Component.translatable("gui.lumungus_storage.crafting_terminal.crafting"),
                CRAFT_PANEL_X + 8,
                7,
                COLOR_TEXT,
                false
        );
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, COLOR_TEXT, false);
        drawPageAndResources(graphics);
        graphics.text(font, compactStatus(), NETWORK_X, 132, COLOR_GREEN, false);
        graphics.text(font, compactTypeStatus(), NETWORK_X + 82, 132, COLOR_GREEN_DIM, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        ResourceAmount hovered = resourceAt(mouseX, mouseY);
        if (hovered != null) {
            List<Component> tooltip = new ArrayList<>(getTooltipFromContainerItem(hovered.stack()));
            tooltip.add(Component.translatable("gui.lumungus_storage.crafting_terminal.amount", hovered.amount()));
            graphics.setComponentTooltipForNextFrame(font, tooltip, mouseX, mouseY);
            return;
        }
        if (isPointInside(mouseX, mouseY, leftPos + ACTION_X, topPos + DEPOSIT_Y, ACTION_WIDTH, 18)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("gui.lumungus_storage.crafting_terminal.deposit"), mouseX, mouseY);
            return;
        }
        if (isPointInside(mouseX, mouseY, leftPos + ACTION_X, topPos + BAY_MOVE_Y, ACTION_WIDTH, 18)) {
            graphics.setTooltipForNextFrame(font, Component.translatable("gui.lumungus_storage.crafting_terminal.move_to_bays"), mouseX, mouseY);
            return;
        }
        if (isPointInside(mouseX, mouseY, leftPos + ACTION_X, topPos + SHULKER_MODE_Y, ACTION_WIDTH, 18)) {
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
        if (isPointInside(mouseX, mouseY, leftPos + NETWORK_X, topPos + PAGE_Y, 20, 12)) {
            page = Math.max(0, page - 1);
            return true;
        }
        if (isPointInside(mouseX, mouseY, leftPos + NETWORK_X + 112, topPos + PAGE_Y, 20, 12)) {
            int pageCount = Math.max(1, (filteredResources().size() + NETWORK_PAGE_SIZE - 1) / NETWORK_PAGE_SIZE);
            page = Math.min(pageCount - 1, page + 1);
            return true;
        }
        if (isPointInside(mouseX, mouseY, leftPos + ACTION_X, topPos + DEPOSIT_Y, ACTION_WIDTH, 18)) {
            sendAction(
                    event.button() == 1
                            ? TerminalActionPayload.Action.DEPOSIT_ONE_CARRIED
                            : TerminalActionPayload.Action.DEPOSIT_CARRIED_STACK,
                    ItemStack.EMPTY
            );
            return true;
        }
        if (isPointInside(mouseX, mouseY, leftPos + ACTION_X, topPos + BAY_MOVE_Y, ACTION_WIDTH, 18)) {
            sendAction(TerminalActionPayload.Action.MOVE_PHYSICAL_TO_DRIVE_BAYS, ItemStack.EMPTY);
            return true;
        }
        if (isPointInside(mouseX, mouseY, leftPos + ACTION_X, topPos + SHULKER_MODE_Y, ACTION_WIDTH, 18)) {
            shulkerExtractMode = !shulkerExtractMode;
            return true;
        }

        ResourceAmount clicked = resourceAt(mouseX, mouseY);
        if (clicked != null) {
            sendAction(actionForResourceClick(event), clicked.stack());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchBox != null && searchBox.isFocused()) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                return super.keyPressed(event);
            }
            searchBox.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchBox != null && searchBox.isFocused()) {
            searchBox.charTyped(event);
            return true;
        }
        return super.charTyped(event);
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
            int pageCount = Math.max(1, (filteredResources().size() + NETWORK_PAGE_SIZE - 1) / NETWORK_PAGE_SIZE);
            if (verticalAmount < 0) {
                page = Math.min(pageCount - 1, page + 1);
            } else if (verticalAmount > 0) {
                page = Math.max(0, page - 1);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private TerminalActionPayload.Action actionForResourceClick(MouseButtonEvent event) {
        if (shulkerExtractMode && event.hasShiftDown()) {
            return TerminalActionPayload.Action.EXTRACT_SHULKER_TO_INVENTORY;
        }
        if (shulkerExtractMode) {
            return TerminalActionPayload.Action.EXTRACT_SHULKER_TO_CURSOR;
        }
        if (event.hasShiftDown()) {
            return TerminalActionPayload.Action.EXTRACT_STACK_TO_INVENTORY;
        }
        if (event.button() == 1) {
            return TerminalActionPayload.Action.EXTRACT_ONE_TO_CURSOR;
        }
        return TerminalActionPayload.Action.EXTRACT_STACK_TO_CURSOR;
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
                .filter(resource -> query.isEmpty() || searchableText(resource).contains(query))
                .sorted(comparator)
                .toList();
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

    private void drawNetworkSlots(GuiGraphicsExtractor graphics, int left, int top, int mouseX, int mouseY) {
        for (int row = 0; row < NETWORK_ROWS; row++) {
            for (int column = 0; column < NETWORK_COLUMNS; column++) {
                int x = left + NETWORK_X + column * NETWORK_COLUMN_STEP;
                int y = top + NETWORK_Y + row * 18;
                boolean hovered = isPointInside(mouseX, mouseY, x, y, 18, 18);
                graphics.fill(x, y, x + 18, y + 18, hovered ? COLOR_SCREEN_HOVER : COLOR_SCREEN_SLOT);
                graphics.outline(x, y, 18, 18, COLOR_GREEN_DIM);
            }
        }
    }

    private void drawPageAndResources(GuiGraphicsExtractor graphics) {
        List<ResourceAmount> resources = filteredResources();
        int pageCount = Math.max(1, (resources.size() + NETWORK_PAGE_SIZE - 1) / NETWORK_PAGE_SIZE);
        page = Math.min(page, pageCount - 1);
        graphics.centeredText(
                font,
                Component.translatable("gui.lumungus_storage.crafting_terminal.page", page + 1, pageCount),
                NETWORK_X + 66,
                PAGE_Y + 2,
                COLOR_GREEN
        );

        if (resources.isEmpty()) {
            graphics.centeredText(
                    font,
                    Component.translatable(searchBox.getValue().isBlank()
                            ? "gui.lumungus_storage.crafting_terminal.empty"
                            : "gui.lumungus_storage.crafting_terminal.no_match"),
                    NETWORK_X + 66,
                    80,
                    COLOR_GREEN_DIM
            );
            return;
        }

        int start = page * NETWORK_PAGE_SIZE;
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
    }

    private void drawPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int fillColor) {
        graphics.fill(x, y, x + width, y + height, COLOR_PANEL_SHADOW);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, COLOR_FRAME);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, fillColor);
    }

    private void drawButton(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            boolean active,
            int accentColor
    ) {
        int left = leftPos + x;
        int top = topPos + y;
        graphics.fill(left, top, left + width, top + height, active ? COLOR_SCREEN : COLOR_FRAME);
        graphics.outline(left, top, width, height, active ? accentColor : 0xFF4A4E4C);
    }

    private void drawActionButton(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int accentColor,
            boolean hovered
    ) {
        int left = leftPos + x;
        int top = topPos + y;
        graphics.fill(left, top, left + ACTION_WIDTH, top + 18, hovered ? 0xFF3C3326 : COLOR_FRAME);
        graphics.outline(left, top, ACTION_WIDTH, 18, accentColor);
        graphics.fill(left + 2, top + 2, left + ACTION_WIDTH - 2, top + 3, 0x553FEF7F);
    }

    private void drawVanillaSlotBackgrounds(GuiGraphicsExtractor graphics, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                drawSlotBackground(graphics, left + 213 + column * 18, top + 39 + row * 18);
            }
        }
        drawSlotBackground(graphics, left + 285, top + 57);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotBackground(graphics, left + 86 + column * 18, top + 153 + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlotBackground(graphics, left + 86 + column * 18, top + 211);
        }
    }

    private static void drawSlotBackground(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, COLOR_FRAME);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, COLOR_SHELL_LIGHT);
    }

    private void drawCraftingArrow(GuiGraphicsExtractor graphics, int left, int top) {
        int x = left + 270;
        int y = top + 65;
        graphics.fill(x, y + 2, x + 12, y + 6, COLOR_TEXT_DIM);
        graphics.fill(x + 8, y, x + 10, y + 8, COLOR_TEXT_DIM);
        graphics.fill(x + 10, y + 1, x + 12, y + 7, COLOR_TEXT_DIM);
        graphics.fill(x + 12, y + 2, x + 14, y + 6, COLOR_TEXT_DIM);
        graphics.fill(
                left + CRAFT_PANEL_X + 7,
                top + CRAFT_PANEL_Y + CRAFT_PANEL_HEIGHT - 13,
                left + CRAFT_PANEL_X + CRAFT_PANEL_WIDTH - 7,
                top + CRAFT_PANEL_Y + CRAFT_PANEL_HEIGHT - 11,
                COLOR_COPPER
        );
        graphics.fill(
                left + CRAFT_PANEL_X + 7,
                top + CRAFT_PANEL_Y + CRAFT_PANEL_HEIGHT - 10,
                left + CRAFT_PANEL_X + CRAFT_PANEL_WIDTH - 7,
                top + CRAFT_PANEL_Y + CRAFT_PANEL_HEIGHT - 7,
                COLOR_FRAME
        );
    }

    private Component compactStatus() {
        return Component.literal(compactAmount(menu.networkStoredAmount()) + "/" + compactAmount(menu.networkTotalCapacity()));
    }

    private Component compactTypeStatus() {
        return Component.literal(menu.networkStoredTypes() + "/" + compactAmount(menu.networkTotalTypeCapacity()) + " T");
    }

    private static String searchableText(ResourceAmount resource) {
        String displayName = resource.stack().getHoverName().getString().toLowerCase(Locale.ROOT);
        String itemId = BuiltInRegistries.ITEM.getKey(resource.stack().getItem()).toString().toLowerCase(Locale.ROOT);
        return displayName + " " + itemId;
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
        if (amount >= 1_000_000_000) {
            return amount / 1_000_000_000 + "B";
        }
        if (amount >= 10_000_000) {
            return amount / 1_000_000 + "M";
        }
        if (amount >= 1_000_000) {
            return String.format(Locale.ROOT, "%.1fM", amount / 1_000_000.0D);
        }
        if (amount >= 1_000) {
            return amount / 1_000 + "K";
        }
        return Long.toString(amount);
    }

    private static String compactAmount(long amount) {
        if (amount >= 10_000_000) {
            return amount / 1_000_000 + "M";
        }
        if (amount >= 1_000_000) {
            return String.format(Locale.ROOT, "%.1fM", amount / 1_000_000.0D);
        }
        if (amount >= 10_000) {
            return amount / 1_000 + "K";
        }
        if (amount >= 1_000) {
            return String.format(Locale.ROOT, "%.1fK", amount / 1_000.0D);
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
