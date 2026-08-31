package dev.lumungus.storage.menu;

import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.registry.LumungusStorageMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class DriveBayMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 166;
    public static final int CELL_SLOT_START = 0;
    public static final int CELL_SLOT_COUNT = DriveBayBlockEntity.CELL_SLOTS;
    public static final int PLAYER_INVENTORY_SLOT_START = CELL_SLOT_START + CELL_SLOT_COUNT;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = 36;

    private static final int PLAYER_INVENTORY_SLOT_END = PLAYER_INVENTORY_SLOT_START
            + PLAYER_INVENTORY_SLOT_COUNT;

    private final ContainerLevelAccess access;
    private final Container cellContainer;

    public DriveBayMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(
                containerId,
                inventory,
                inventory.player.level().getBlockEntity(pos) instanceof DriveBayBlockEntity driveBay
                        ? driveBay.cellContainer()
                        : new SimpleContainer(CELL_SLOT_COUNT),
                ContainerLevelAccess.create(inventory.player.level(), pos)
        );
    }

    public DriveBayMenu(
            int containerId,
            Inventory inventory,
            DriveBayBlockEntity driveBay,
            BlockPos pos
    ) {
        this(containerId, inventory, driveBay.cellContainer(), ContainerLevelAccess.create(inventory.player.level(), pos));
    }

    private DriveBayMenu(
            int containerId,
            Inventory inventory,
            Container cellContainer,
            ContainerLevelAccess access
    ) {
        super(LumungusStorageMenus.DRIVE_BAY, containerId);
        checkContainerSize(cellContainer, CELL_SLOT_COUNT);
        this.cellContainer = cellContainer;
        this.access = access;

        for (int index = 0; index < CELL_SLOT_COUNT; index++) {
            addSlot(new CellSlot(cellContainer, index, 17 + index * 18, 35));
        }
        addStandardInventorySlots(inventory, 8, 84);
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
        if (slotIndex < PLAYER_INVENTORY_SLOT_START) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_SLOT_START, PLAYER_INVENTORY_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(LumungusStorageItems.STORAGE_CELL_16K)) {
            if (!moveItemStackTo(stack, CELL_SLOT_START, CELL_SLOT_START + CELL_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return cellContainer.stillValid(player)
                && stillValid(access, player, LumungusStorageBlocks.DRIVE_BAY);
    }

    private static final class CellSlot extends Slot {
        private CellSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(LumungusStorageItems.STORAGE_CELL_16K);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }
}
