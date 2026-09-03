package dev.lumungus.storage.menu;

import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.registry.LumungusStorageMenus;
import dev.lumungus.storage.wireless.WirelessModuleContainer;
import dev.lumungus.storage.wireless.WirelessModuleHost;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class WirelessModuleMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 176;
    public static final int IMAGE_HEIGHT = 166;
    public static final int MODULE_SLOT = 0;
    public static final int PLAYER_INVENTORY_SLOT_START = 1;
    private static final int PLAYER_INVENTORY_SLOT_END = PLAYER_INVENTORY_SLOT_START + 36;

    private final Container moduleContainer;

    public WirelessModuleMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, findContainer(inventory, pos));
    }

    public WirelessModuleMenu(int containerId, Inventory inventory, WirelessModuleHost host, BlockEntity blockEntity) {
        this(containerId, inventory, new WirelessModuleContainer(host, blockEntity));
    }

    private WirelessModuleMenu(int containerId, Inventory inventory, Container moduleContainer) {
        super(LumungusStorageMenus.WIRELESS_MODULE, containerId);
        checkContainerSize(moduleContainer, 1);
        this.moduleContainer = moduleContainer;
        addSlot(new ModuleSlot(moduleContainer, 0, 80, 35));
        addStandardInventorySlots(inventory, 8, 84);
    }

    private static Container findContainer(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof WirelessModuleHost host
                ? new WirelessModuleContainer(host, blockEntity)
                : new SimpleContainer(1);
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
        if (slotIndex == MODULE_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_SLOT_START, PLAYER_INVENTORY_SLOT_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, MODULE_SLOT, MODULE_SLOT + 1, false)) {
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
        return moduleContainer.stillValid(player);
    }

    private static final class ModuleSlot extends Slot {
        private ModuleSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)
                    && container.canPlaceItem(getContainerSlot(), stack);
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
