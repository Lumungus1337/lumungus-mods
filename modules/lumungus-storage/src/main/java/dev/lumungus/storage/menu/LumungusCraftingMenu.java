package dev.lumungus.storage.menu;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageSnapshot;
import dev.lumungus.storage.block.entity.CraftingTerminalBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.block.entity.WirelessStorageControllerBlockEntity;
import dev.lumungus.storage.network.TerminalActionPayload;
import dev.lumungus.storage.network.TerminalResourceEntry;
import dev.lumungus.storage.network.TerminalSnapshotPayload;
import dev.lumungus.storage.registry.LumungusStorageMenus;
import dev.lumungus.storage.storage.ShulkerBoxTransfer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractCraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

public final class LumungusCraftingMenu extends AbstractCraftingMenu {
    public static final int IMAGE_WIDTH = 336;
    public static final int IMAGE_HEIGHT = 236;
    public static final int RESULT_SLOT = 0;
    public static final int RECIPE_SLOT_START = 1;
    public static final int RECIPE_SLOT_COUNT = 9;
    public static final int PLAYER_INVENTORY_SLOT_START = 10;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = 36;

    private static final int PLAYER_INVENTORY_SLOT_END = PLAYER_INVENTORY_SLOT_START
            + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int MAX_RECIPE_TRANSFER = 64;
    public static final int MAX_REQUESTED_RESULT_AMOUNT = 4096;

    private final ContainerLevelAccess access;
    private final Player player;
    private final BlockPos terminalPos;
    private boolean placingRecipe;
    private int lastSnapshotFingerprint = Integer.MIN_VALUE;

    private List<ResourceAmount> clientResources = List.of();
    private long clientStoredAmount;
    private long clientTotalCapacity;
    private int clientStoredTypes;
    private int clientTotalTypeCapacity;

    public LumungusCraftingMenu(int containerId, Inventory inventory, BlockPos terminalPos) {
        this(
                containerId,
                inventory,
                ContainerLevelAccess.create(inventory.player.level(), terminalPos),
                terminalPos
        );
    }

    public LumungusCraftingMenu(
            int containerId,
            Inventory inventory,
            ContainerLevelAccess access,
            BlockPos terminalPos
    ) {
        super(LumungusStorageMenus.CRAFTING_TERMINAL, containerId, 3, 3);
        this.access = access;
        this.player = inventory.player;
        this.terminalPos = terminalPos.immutable();

        addResultSlot(player, 286, 58);
        addCraftingGridSlots(214, 40);
        addStandardInventorySlots(inventory, 87, 154);
    }

    public BlockPos terminalPos() {
        return terminalPos;
    }

    public List<ResourceAmount> networkResources() {
        return clientResources;
    }

    public long networkStoredAmount() {
        return clientStoredAmount;
    }

    public long networkTotalCapacity() {
        return clientTotalCapacity;
    }

    public int networkStoredTypes() {
        return clientStoredTypes;
    }

    public int networkTotalTypeCapacity() {
        return clientTotalTypeCapacity;
    }

    public void applySnapshot(TerminalSnapshotPayload payload) {
        if (payload.containerId() != containerId) {
            return;
        }
        clientStoredAmount = payload.storedAmount();
        clientTotalCapacity = payload.totalCapacity();
        clientStoredTypes = payload.storedTypes();
        clientTotalTypeCapacity = payload.totalTypeCapacity();
        clientResources = payload.resources().stream()
                .map(TerminalResourceEntry::asResourceAmount)
                .toList();
    }

    @Override
    public void slotsChanged(Container container) {
        if (!placingRecipe) {
            access.execute((level, pos) -> {
                if (level instanceof ServerLevel serverLevel) {
                    updateCraftingResult(
                            this,
                            serverLevel,
                            player,
                            craftSlots,
                            resultSlots,
                            null
                    );
                }
            });
        }
    }

    @Override
    protected void beginPlacingRecipe() {
        placingRecipe = true;
    }

    @Override
    protected void finishPlacingRecipe(
            ServerLevel serverLevel,
            RecipeHolder<CraftingRecipe> recipe
    ) {
        placingRecipe = false;
        updateCraftingResult(this, serverLevel, player, craftSlots, resultSlots, recipe);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        StorageControllerBlockEntity controller = linkedController();
        StorageControllerBlockEntity.NetworkState networkState = controller == null
                ? new StorageControllerBlockEntity.NetworkState(
                        List.of(),
                        new StorageSnapshot(0, 0),
                        new StorageCapacity(0, 0)
                )
                : controller.networkState();
        List<ResourceAmount> resources = networkState.resources();
        StorageSnapshot snapshot = networkState.snapshot();
        StorageCapacity capacity = networkState.capacity();
        int fingerprint = Objects.hash(resources, snapshot, capacity);
        if (fingerprint == lastSnapshotFingerprint) {
            return;
        }

        lastSnapshotFingerprint = fingerprint;
        List<TerminalResourceEntry> syncedResources = resources.stream()
                .limit(TerminalSnapshotPayload.MAX_SYNCED_TYPES)
                .map(TerminalResourceEntry::new)
                .toList();
        ServerPlayNetworking.send(serverPlayer, new TerminalSnapshotPayload(
                containerId,
                snapshot.storedTotalAmount(),
                capacity.maxTotalAmount(),
                snapshot.storedDistinctTypes(),
                capacity.maxDistinctTypes(),
                syncedResources
        ));
    }

    public void handleTerminalAction(TerminalActionPayload payload) {
        StorageControllerBlockEntity controller = linkedController();
        if (controller == null || !stillValid(player)) {
            return;
        }

        switch (payload.action()) {
            case EXTRACT_STACK_TO_CURSOR -> extractToCursor(controller, payload.template(), false);
            case EXTRACT_ONE_TO_CURSOR -> extractToCursor(controller, payload.template(), true);
            case EXTRACT_STACK_TO_INVENTORY -> extractToInventory(controller, payload.template());
            case EXTRACT_SHULKER_TO_CURSOR -> extractShulkerToCursor(controller, payload.template());
            case EXTRACT_SHULKER_TO_INVENTORY -> extractShulkerToInventory(controller, payload.template());
            case DEPOSIT_CARRIED_STACK -> depositCarried(controller, false);
            case DEPOSIT_ONE_CARRIED -> depositCarried(controller, true);
            case MOVE_PHYSICAL_TO_DRIVE_BAYS -> movePhysicalInventoriesToDriveBays(controller);
        }
        broadcastChanges();
    }

    private int requestedCraftResultAmount = 1;

    public int requestedCraftResultAmount() {
        return requestedCraftResultAmount;
    }

    public void setRequestedCraftResultAmount(int amount) {
        requestedCraftResultAmount = Math.clamp(amount, 1, MAX_REQUESTED_RESULT_AMOUNT);
    }

    public void placeRecipeFromNetwork(Identifier recipeId, boolean maxTransfer) {
        placeRecipeFromNetwork(recipeId, maxTransfer ? MAX_RECIPE_TRANSFER : 1);
    }

    public void placeRecipeFromNetwork(Identifier recipeId, int requestedResultAmount) {
        if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)) {
            return;
        }
        StorageControllerBlockEntity controller = linkedController();
        if (controller == null) {
            return;
        }

        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, recipeId);
        Optional<RecipeHolder<?>> recipeHolder = serverPlayer.level().getServer()
                .getRecipeManager()
                .byKey(key);
        if (recipeHolder.isEmpty() || !(recipeHolder.get().value() instanceof CraftingRecipe recipe)) {
            return;
        }

        List<ItemStack> previousGrid = snapshotCraftGrid();
        List<PoolEntry> pool = createAvailablePool(controller);
        for (ItemStack stack : previousGrid) {
            if (!stack.isEmpty()) {
                addToPool(pool, stack, stack.getCount());
            }
        }
        RecipePlacement singlePlacement = planRecipe(recipe, pool, 1);
        int resultPerCraft = singlePlacement == null
                ? recursiveResultCount(recipe, (ServerLevel) serverPlayer.level())
                : recipe.assemble(craftingInput(singlePlacement)).getCount();
        int clampedResultAmount = Math.clamp(requestedResultAmount, 1, MAX_REQUESTED_RESULT_AMOUNT);
        int requestedCrafts = Math.min(
                MAX_RECIPE_TRANSFER,
                Math.max(1, (clampedResultAmount + Math.max(1, resultPerCraft) - 1) / Math.max(1, resultPerCraft))
        );

        List<RecursiveCraftingPlanner.AvailableResource> recursiveResources = pool.stream()
                .map(resource -> new RecursiveCraftingPlanner.AvailableResource(
                        resource.stack(),
                        resource.amount()
                ))
                .toList();
        RecipePlacement placement = null;
        RecursiveCraftingPlanner.Plan recursivePlan = null;
        for (int crafts = requestedCrafts; crafts >= 1; crafts--) {
            placement = planRecipe(recipe, pool, crafts);
            if (placement != null) {
                break;
            }
            if (player.level() instanceof ServerLevel serverLevel) {
                recursivePlan = RecursiveCraftingPlanner.plan(
                        serverLevel,
                        recipe,
                        recursiveResources,
                        crafts
                ).orElse(null);
                if (recursivePlan != null) {
                    break;
                }
            }
        }

        if (placement == null && recursivePlan == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.missing_ingredients"
            ));
            return;
        }

        if (recursivePlan != null) {
            executeRecursivePlan(controller, recursivePlan, previousGrid);
            return;
        }

        returnCraftGridItems(controller);
        List<AcquiredSlot> acquiredSlots = new ArrayList<>();
        for (PlannedSlot plannedSlot : placement.slots()) {
            int remaining = removeFromPlayerInventory(plannedSlot.stack(), plannedSlot.stack().getCount());
            int fromPlayer = plannedSlot.stack().getCount() - remaining;
            ItemStack fromNetwork = ItemStack.EMPTY;
            if (remaining > 0) {
                fromNetwork = controller.extract(plannedSlot.stack(), remaining, TransferMode.EXECUTE);
            }
            if (fromNetwork.getCount() != remaining) {
                acquiredSlots.add(new AcquiredSlot(plannedSlot.stack(), fromPlayer, fromNetwork.getCount()));
                rollbackAcquiredIngredients(controller, acquiredSlots);
                player.sendSystemMessage(Component.translatable(
                        "message.lumungus_storage.crafting_terminal.missing_ingredients"
                ));
                restoreCraftGrid(controller, previousGrid);
                slotsChanged(craftSlots);
                broadcastChanges();
                return;
            }
            acquiredSlots.add(new AcquiredSlot(plannedSlot.stack(), fromPlayer, fromNetwork.getCount()));
        }
        for (PlannedSlot plannedSlot : placement.slots()) {
            craftSlots.setItem(plannedSlot.slot(), plannedSlot.stack());
        }
        slotsChanged(craftSlots);
        broadcastChanges();
    }

    private CraftingInput craftingInput(RecipePlacement placement) {
        List<ItemStack> grid = new ArrayList<>(RECIPE_SLOT_COUNT);
        for (int index = 0; index < RECIPE_SLOT_COUNT; index++) {
            grid.add(ItemStack.EMPTY);
        }
        for (PlannedSlot slot : placement.slots()) {
            grid.set(slot.slot(), slot.stack());
        }
        return CraftingInput.of(3, 3, grid);
    }

    private int recursiveResultCount(CraftingRecipe recipe, ServerLevel level) {
        List<RecursiveCraftingPlanner.AvailableResource> samples = recipeIngredients(recipe).stream()
                .map(ingredient -> ingredient.ingredient().items()
                        .findFirst()
                        .map(item -> new RecursiveCraftingPlanner.AvailableResource(new ItemStack(item), 1))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        return RecursiveCraftingPlanner.plan(level, recipe, samples, 1)
                .map(plan -> recipe.assemble(RecursiveCraftingPlanner.input(plan.finalSlots())).getCount())
                .orElse(1);
    }

    private void executeRecursivePlan(
            StorageControllerBlockEntity controller,
            RecursiveCraftingPlanner.Plan plan,
            List<ItemStack> previousGrid
    ) {
        returnCraftGridItems(controller);
        List<PoolEntry> crafted = new ArrayList<>();
        List<AcquiredSlot> acquired = new ArrayList<>();

        for (RecursiveCraftingPlanner.CraftStep step : plan.steps()) {
            if (!acquirePlannedStacks(controller, step.inputs(), crafted, acquired)) {
                rollbackRecursiveTransfer(controller, acquired, crafted, previousGrid);
                return;
            }
            CraftingInput input = RecursiveCraftingPlanner.input(step.inputs());
            ItemStack output = step.recipe().assemble(input);
            output.onCraftedBySystem(player.level());
            addToPool(crafted, output, output.getCount());
            step.recipe().getRemainingItems(input).stream()
                    .filter(stack -> !stack.isEmpty())
                    .forEach(stack -> addToPool(crafted, stack, stack.getCount()));
        }

        if (!acquirePlannedStacks(controller, plan.finalSlots(), crafted, acquired)) {
            rollbackRecursiveTransfer(controller, acquired, crafted, previousGrid);
            return;
        }
        for (RecursiveCraftingPlanner.SelectedSlot slot : plan.finalSlots()) {
            craftSlots.setItem(slot.slot(), slot.stack());
        }
        returnCraftedSurplus(controller, crafted);
        slotsChanged(craftSlots);
        broadcastChanges();
    }

    private boolean acquirePlannedStacks(
            StorageControllerBlockEntity controller,
            List<RecursiveCraftingPlanner.SelectedSlot> slots,
            List<PoolEntry> crafted,
            List<AcquiredSlot> acquired
    ) {
        for (RecursiveCraftingPlanner.SelectedSlot slot : slots) {
            ItemStack template = slot.stack();
            int remaining = removeFromPool(crafted, template, template.getCount());
            if (remaining == 0) {
                continue;
            }
            int afterPlayer = removeFromPlayerInventory(template, remaining);
            int fromPlayer = remaining - afterPlayer;
            ItemStack fromNetwork = afterPlayer == 0
                    ? ItemStack.EMPTY
                    : controller.extract(template, afterPlayer, TransferMode.EXECUTE);
            acquired.add(new AcquiredSlot(template, fromPlayer, fromNetwork.getCount()));
            if (fromNetwork.getCount() != afterPlayer) {
                return false;
            }
        }
        return true;
    }

    private void rollbackRecursiveTransfer(
            StorageControllerBlockEntity controller,
            List<AcquiredSlot> acquired,
            List<PoolEntry> crafted,
            List<ItemStack> previousGrid
    ) {
        rollbackAcquiredIngredients(controller, acquired);
        crafted.clear();
        restoreCraftGrid(controller, previousGrid);
        player.sendSystemMessage(Component.translatable(
                "message.lumungus_storage.crafting_terminal.missing_ingredients"
        ));
        slotsChanged(craftSlots);
        broadcastChanges();
    }

    private void returnCraftedSurplus(StorageControllerBlockEntity controller, List<PoolEntry> crafted) {
        for (PoolEntry entry : crafted) {
            if (entry.amount() <= 0) {
                continue;
            }
            long remaining = entry.amount();
            while (remaining > 0) {
                int batch = (int) Math.min(remaining, entry.stack().getMaxStackSize());
                ItemStack remainder = controller.insert(
                        entry.stack().copyWithCount(batch),
                        TransferMode.EXECUTE
                );
                player.getInventory().placeItemBackInInventory(remainder);
                remaining -= batch;
            }
        }
        crafted.clear();
    }

    private static int removeFromPool(List<PoolEntry> pool, ItemStack template, int amount) {
        int remaining = amount;
        for (int index = 0; index < pool.size() && remaining > 0; index++) {
            PoolEntry entry = pool.get(index);
            if (!ItemStack.isSameItemSameComponents(entry.stack(), template)) {
                continue;
            }
            int removed = (int) Math.min(remaining, entry.amount());
            pool.set(index, new PoolEntry(entry.stack(), entry.amount() - removed));
            remaining -= removed;
        }
        return remaining;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide()) {
            StorageControllerBlockEntity controller = linkedController();
            returnCraftGridItems(controller);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof StorageControllerBlockEntity) {
                return true;
            }
            boolean validAnchor = level.getBlockEntity(pos) instanceof CraftingTerminalBlockEntity
                    || level.getBlockEntity(pos) instanceof WirelessStorageControllerBlockEntity;
            double centerX = pos.getX() + 0.5D;
            double centerY = pos.getY() + 0.5D;
            double centerZ = pos.getZ() + 0.5D;
            return validAnchor && player.distanceToSqr(centerX, centerY, centerZ) <= 64.0D;
        }, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (slotIndex == RESULT_SLOT) {
            stack.getItem().onCraftedBy(stack, player);
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_SLOT_START, PLAYER_INVENTORY_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
            ItemStack resultRemainder = stack.copy();
            slot.onQuickCraft(stack, original);
            slot.onTake(player, stack);
            ItemStack unhandledRemainder = placeOrReturnResultRemainder(slot, resultRemainder);
            if (!unhandledRemainder.isEmpty()) {
                player.drop(unhandledRemainder, false);
            }
            broadcastChanges();
            return original;
        } else {
            StorageControllerBlockEntity controller = linkedController();
            if (controller != null) {
                ItemStack remainder = controller.insert(stack, TransferMode.EXECUTE);
                if (remainder.getCount() == stack.getCount()) {
                    return ItemStack.EMPTY;
                }
                slot.setByPlayer(remainder);
            } else if (!moveItemStackTo(
                    stack,
                    PLAYER_INVENTORY_SLOT_START,
                    PLAYER_INVENTORY_SLOT_END,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        broadcastChanges();
        return original;
    }

    static ItemStack placeOrReturnResultRemainder(Slot slot, ItemStack remainder) {
        if (!remainder.isEmpty() && !slot.hasItem()) {
            slot.setByPlayer(remainder);
            return ItemStack.EMPTY;
        }
        return remainder.copy();
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public Slot getResultSlot() {
        return slots.get(RESULT_SLOT);
    }

    @Override
    public List<Slot> getInputGridSlots() {
        return slots.subList(RECIPE_SLOT_START, RECIPE_SLOT_START + RECIPE_SLOT_COUNT);
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return RecipeBookType.CRAFTING;
    }

    @Override
    protected Player owner() {
        return player;
    }

    private StorageControllerBlockEntity linkedController() {
        Optional<StorageControllerBlockEntity> linked = access.evaluate((level, pos) -> {
            if (level.getBlockEntity(pos) instanceof CraftingTerminalBlockEntity terminal) {
                return Optional.ofNullable(terminal.linkedController());
            }
            if (level.getBlockEntity(pos) instanceof WirelessStorageControllerBlockEntity wireless) {
                return Optional.ofNullable(wireless.linkedController());
            }
            if (level.getBlockEntity(pos) instanceof StorageControllerBlockEntity controller) {
                return Optional.of(controller);
            }
            return Optional.<StorageControllerBlockEntity>empty();
        }, Optional.<StorageControllerBlockEntity>empty());
        return linked.orElse(null);
    }

    private void extractToCursor(
            StorageControllerBlockEntity controller,
            ItemStack template,
            boolean singleItem
    ) {
        if (template.isEmpty() || controller.count(template) <= 0) {
            return;
        }

        ItemStack carried = getCarried().copy();
        if (!carried.isEmpty() && !ItemStack.isSameItemSameComponents(carried, template)) {
            return;
        }
        int freeSpace = carried.isEmpty()
                ? template.getMaxStackSize()
                : carried.getMaxStackSize() - carried.getCount();
        int requested = singleItem ? Math.min(1, freeSpace) : freeSpace;
        if (requested <= 0) {
            return;
        }

        ItemStack extracted = controller.extract(template, requested, TransferMode.EXECUTE);
        if (extracted.isEmpty()) {
            return;
        }
        if (carried.isEmpty()) {
            setCarried(extracted);
        } else {
            carried.grow(extracted.getCount());
            setCarried(carried);
        }
    }

    private void extractToInventory(StorageControllerBlockEntity controller, ItemStack template) {
        if (template.isEmpty()) {
            return;
        }
        int freeSpace = inventorySpaceFor(template);
        if (freeSpace <= 0) {
            return;
        }
        ItemStack extracted = controller.extract(
                template,
                Math.min(freeSpace, template.getMaxStackSize()),
                TransferMode.EXECUTE
        );
        player.getInventory().placeItemBackInInventory(extracted);
    }

    private void extractShulkerToCursor(StorageControllerBlockEntity controller, ItemStack template) {
        if (!getCarried().isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.shulker_cursor_blocked"
            ));
            return;
        }

        ItemStack filledShulker = createFilledShulker(controller, template);
        if (!filledShulker.isEmpty()) {
            setCarried(filledShulker);
        }
    }

    private void extractShulkerToInventory(StorageControllerBlockEntity controller, ItemStack template) {
        ItemStack preview = new ItemStack(net.minecraft.world.item.Items.SHULKER_BOX);
        if (inventorySpaceFor(preview) <= 0) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.shulker_inventory_full"
            ));
            return;
        }

        ItemStack filledShulker = createFilledShulker(controller, template);
        if (!filledShulker.isEmpty()) {
            player.getInventory().placeItemBackInInventory(filledShulker);
        }
    }

    private ItemStack createFilledShulker(StorageControllerBlockEntity controller, ItemStack template) {
        if (template.isEmpty() || ShulkerBoxTransfer.isShulkerBox(template)) {
            return ItemStack.EMPTY;
        }

        ItemStack emptyShulker = findEmptyShulker(controller);
        if (emptyShulker.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.no_empty_shulker"
            ));
            return ItemStack.EMPTY;
        }

        long available = controller.count(template);
        int requested = (int) Math.min(available, ShulkerBoxTransfer.maxPackedAmount(template));
        if (requested <= 0) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.no_items_for_shulker"
            ));
            return ItemStack.EMPTY;
        }

        ItemStack reservedShulker = controller.extract(emptyShulker, 1, TransferMode.EXECUTE);
        if (reservedShulker.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.no_empty_shulker"
            ));
            return ItemStack.EMPTY;
        }

        List<ItemStack> contents = extractMany(controller, template, requested);
        if (contents.isEmpty()) {
            controller.insert(reservedShulker, TransferMode.EXECUTE);
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.no_items_for_shulker"
            ));
            return ItemStack.EMPTY;
        }

        return ShulkerBoxTransfer.filledShulker(reservedShulker, contents);
    }

    private ItemStack findEmptyShulker(StorageControllerBlockEntity controller) {
        return controller.storedResources().stream()
                .map(ResourceAmount::stack)
                .filter(ShulkerBoxTransfer::isEmptyShulkerBox)
                .findFirst()
                .orElse(ItemStack.EMPTY);
    }

    private List<ItemStack> extractMany(
            StorageControllerBlockEntity controller,
            ItemStack template,
            int amount
    ) {
        List<ItemStack> extractedStacks = new ArrayList<>();
        int remaining = amount;
        while (remaining > 0) {
            ItemStack extracted = controller.extract(
                    template,
                    Math.min(remaining, template.getMaxStackSize()),
                    TransferMode.EXECUTE
            );
            if (extracted.isEmpty()) {
                break;
            }
            extractedStacks.add(extracted);
            remaining -= extracted.getCount();
        }
        return List.copyOf(extractedStacks);
    }

    private void depositCarried(StorageControllerBlockEntity controller, boolean singleItem) {
        ItemStack carried = getCarried().copy();
        if (carried.isEmpty()) {
            return;
        }
        int requested = singleItem ? 1 : carried.getCount();
        ItemStack transfer = carried.copyWithCount(requested);
        ItemStack remainder = controller.insert(transfer, TransferMode.EXECUTE);
        int inserted = requested - remainder.getCount();
        if (inserted <= 0) {
            return;
        }
        carried.shrink(inserted);
        setCarried(carried.isEmpty() ? ItemStack.EMPTY : carried);
    }

    private void movePhysicalInventoriesToDriveBays(StorageControllerBlockEntity controller) {
        StorageControllerBlockEntity.BayMoveResult result = controller.movePhysicalInventoriesIntoDriveBays();
        player.sendSystemMessage(Component.translatable(
                result.movedItems() > 0
                        ? "message.lumungus_storage.crafting_terminal.bay_move_done"
                        : "message.lumungus_storage.crafting_terminal.bay_move_empty",
                result.movedItems(),
                result.physicalInventories(),
                result.driveBays()
        ));
        if (result.paused()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.bay_move_paused",
                    result.remainingItems()
            ));
        }
    }

    private int inventorySpaceFor(ItemStack template) {
        int freeSpace = 0;
        Inventory inventory = player.getInventory();
        int storageSlots = Math.min(PLAYER_INVENTORY_SLOT_COUNT, inventory.getContainerSize());
        for (int index = 0; index < storageSlots; index++) {
            ItemStack stack = inventory.getItem(index);
            if (stack.isEmpty()) {
                freeSpace += template.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(stack, template)) {
                freeSpace += Math.max(0, stack.getMaxStackSize() - stack.getCount());
            }
            if (freeSpace >= template.getMaxStackSize()) {
                return template.getMaxStackSize();
            }
        }
        return freeSpace;
    }

    private void returnCraftGridItems(StorageControllerBlockEntity controller) {
        for (int index = 0; index < craftSlots.getContainerSize(); index++) {
            ItemStack stack = craftSlots.removeItemNoUpdate(index);
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack remainder = controller == null
                    ? stack
                    : controller.insert(stack, TransferMode.EXECUTE);
            player.getInventory().placeItemBackInInventory(remainder);
        }
        resultSlots.setItem(0, ItemStack.EMPTY);
    }

    private List<ItemStack> snapshotCraftGrid() {
        List<ItemStack> snapshot = new ArrayList<>(RECIPE_SLOT_COUNT);
        for (int index = 0; index < RECIPE_SLOT_COUNT; index++) {
            snapshot.add(craftSlots.getItem(index).copy());
        }
        return snapshot;
    }

    private void restoreCraftGrid(
            StorageControllerBlockEntity controller,
            List<ItemStack> previousGrid
    ) {
        for (int index = 0; index < previousGrid.size(); index++) {
            ItemStack expected = previousGrid.get(index);
            if (expected.isEmpty()) {
                continue;
            }

            int remaining = removeFromPlayerInventory(expected, expected.getCount());
            ItemStack restored = expected.copyWithCount(expected.getCount() - remaining);
            if (remaining > 0) {
                ItemStack fromNetwork = controller.extract(expected, remaining, TransferMode.EXECUTE);
                if (!fromNetwork.isEmpty()) {
                    if (restored.isEmpty()) {
                        restored = fromNetwork.copy();
                    } else {
                        restored.grow(fromNetwork.getCount());
                    }
                    remaining -= fromNetwork.getCount();
                }
            }
            if (remaining > 0) {
                player.drop(expected.copyWithCount(remaining), false);
            }
            craftSlots.setItem(index, restored);
        }
    }

    private List<PoolEntry> createAvailablePool(StorageControllerBlockEntity controller) {
        List<PoolEntry> pool = new ArrayList<>();
        Inventory inventory = player.getInventory();
        int storageSlots = Math.min(PLAYER_INVENTORY_SLOT_COUNT, inventory.getContainerSize());
        for (int index = 0; index < storageSlots; index++) {
            ItemStack stack = inventory.getItem(index);
            if (!stack.isEmpty()) {
                addToPool(pool, stack, stack.getCount());
            }
        }
        for (ResourceAmount resource : controller.storedResources()) {
            addToPool(pool, resource.stack(), resource.amount());
        }
        return pool;
    }

    private static void addToPool(List<PoolEntry> pool, ItemStack stack, long amount) {
        for (int index = 0; index < pool.size(); index++) {
            PoolEntry entry = pool.get(index);
            if (ItemStack.isSameItemSameComponents(entry.stack(), stack)) {
                pool.set(index, new PoolEntry(stack, entry.amount() + amount));
                return;
            }
        }
        pool.add(new PoolEntry(stack, amount));
    }

    private RecipePlacement planRecipe(CraftingRecipe recipe, List<PoolEntry> available, int crafts) {
        List<PlannedIngredient> ingredients = recipeIngredients(recipe);
        List<RecipeIngredientPlanner.IngredientSlot> plannerIngredients = ingredients.stream()
                .map(ingredient -> new RecipeIngredientPlanner.IngredientSlot(
                        ingredient.slot(),
                        ingredient.ingredient()
                ))
                .toList();
        List<RecipeIngredientPlanner.AvailableResource> plannerResources = available.stream()
                .map(resource -> new RecipeIngredientPlanner.AvailableResource(
                        resource.stack(),
                        resource.amount()
                ))
                .toList();
        Optional<List<RecipeIngredientPlanner.PlannedSlot>> planned = RecipeIngredientPlanner.plan(
                plannerIngredients,
                plannerResources,
                crafts
        );
        if (planned.isEmpty()) {
            return null;
        }

        List<ItemStack> grid = new ArrayList<>(RECIPE_SLOT_COUNT);
        for (int index = 0; index < RECIPE_SLOT_COUNT; index++) {
            grid.add(ItemStack.EMPTY);
        }
        for (RecipeIngredientPlanner.PlannedSlot plannedSlot : planned.get()) {
            grid.set(plannedSlot.slot(), plannedSlot.stack());
        }
        if (!recipe.matches(CraftingInput.of(3, 3, grid), player.level())) {
            return null;
        }
        return new RecipePlacement(planned.get().stream()
                .map(plannedSlot -> new PlannedSlot(plannedSlot.slot(), plannedSlot.stack()))
                .toList());
    }

    private void rollbackAcquiredIngredients(
            StorageControllerBlockEntity controller,
            List<AcquiredSlot> acquiredSlots
    ) {
        for (AcquiredSlot acquired : acquiredSlots) {
            if (acquired.fromPlayer() > 0) {
                player.getInventory().placeItemBackInInventory(
                        acquired.stack().copyWithCount(acquired.fromPlayer())
                );
            }
            if (acquired.fromNetwork() > 0) {
                ItemStack stack = acquired.stack().copyWithCount(acquired.fromNetwork());
                ItemStack remainder = controller.insert(stack, TransferMode.EXECUTE);
                player.getInventory().placeItemBackInInventory(remainder);
            }
        }
    }

    private static List<PlannedIngredient> recipeIngredients(CraftingRecipe recipe) {
        List<PlannedIngredient> ingredients = new ArrayList<>();
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            List<Optional<Ingredient>> shapedIngredients = shapedRecipe.getIngredients();
            for (int row = 0; row < shapedRecipe.getHeight(); row++) {
                for (int column = 0; column < shapedRecipe.getWidth(); column++) {
                    int recipeIndex = row * shapedRecipe.getWidth() + column;
                    Optional<Ingredient> ingredient = shapedIngredients.get(recipeIndex);
                    if (ingredient.isPresent()) {
                        ingredients.add(new PlannedIngredient(row * 3 + column, ingredient.get()));
                    }
                }
            }
            return ingredients;
        }

        List<Ingredient> shapelessIngredients = recipe.placementInfo().ingredients();
        for (int index = 0; index < shapelessIngredients.size() && index < RECIPE_SLOT_COUNT; index++) {
            ingredients.add(new PlannedIngredient(index, shapelessIngredients.get(index)));
        }
        return ingredients;
    }

    private int removeFromPlayerInventory(ItemStack template, int amount) {
        int remaining = amount;
        Inventory inventory = player.getInventory();
        int storageSlots = Math.min(PLAYER_INVENTORY_SLOT_COUNT, inventory.getContainerSize());
        for (int index = 0; index < storageSlots && remaining > 0; index++) {
            ItemStack stack = inventory.getItem(index);
            if (!ItemStack.isSameItemSameComponents(stack, template)) {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (stack.isEmpty()) {
                inventory.setItem(index, ItemStack.EMPTY);
            }
        }
        return remaining;
    }

    private static void updateCraftingResult(
            LumungusCraftingMenu menu,
            ServerLevel serverLevel,
            Player player,
            CraftingContainer craftSlots,
            ResultContainer resultSlots,
            RecipeHolder<CraftingRecipe> knownRecipe
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        CraftingInput input = craftSlots.asCraftInput();
        ItemStack result = ItemStack.EMPTY;
        Optional<RecipeHolder<CraftingRecipe>> match = serverLevel.getServer()
                .getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, serverLevel, knownRecipe);
        if (match.isPresent()) {
            RecipeHolder<CraftingRecipe> holder = match.get();
            CraftingRecipe recipe = holder.value();
            if (resultSlots.setRecipeUsed(serverPlayer, holder)) {
                ItemStack assembled = recipe.assemble(input);
                if (assembled.isItemEnabled(serverLevel.enabledFeatures())) {
                    result = assembled;
                }
            }
        }

        resultSlots.setItem(0, result);
        menu.setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId,
                menu.incrementStateId(),
                0,
                result
        ));
    }

    private record PoolEntry(ItemStack stack, long amount) {
        private PoolEntry {
            stack = stack.copyWithCount(1);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    private record PlannedIngredient(int slot, Ingredient ingredient) {
    }

    private record PlannedSlot(int slot, ItemStack stack) {
        private PlannedSlot {
            stack = stack.copy();
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    private record RecipePlacement(List<PlannedSlot> slots) {
    }

    private record AcquiredSlot(ItemStack stack, int fromPlayer, int fromNetwork) {
        private AcquiredSlot {
            stack = stack.copyWithCount(1);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }
}
