package dev.lumungus.storage.wireless;

import dev.lumungus.storage.registry.LumungusStorageItems;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class WirelessModuleContainer implements Container {
    private final WirelessModuleHost host;
    private final BlockEntity blockEntity;

    public WirelessModuleContainer(WirelessModuleHost host, BlockEntity blockEntity) {
        this.host = host;
        this.blockEntity = blockEntity;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return host.wirelessModule().isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? host.wirelessModule() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return index == 0 && count > 0 ? host.removeWirelessModule() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return index == 0 ? host.removeWirelessModule() : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index != 0) {
            return;
        }
        host.removeWirelessModule();
        if (!stack.isEmpty()) {
            host.installWirelessModule(stack);
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        blockEntity.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(blockEntity, player);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == 0
                && stack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)
                && WirelessModuleBinding.isPrimedModule(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void clearContent() {
        host.removeWirelessModule();
    }
}
