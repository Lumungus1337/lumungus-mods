package dev.lumungus.storage.menu;

import dev.lumungus.storage.wireless.WirelessModuleHost;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class WirelessModuleMenuProvider implements ExtendedMenuProvider<BlockPos> {
    private final WirelessModuleHost host;
    private final BlockEntity blockEntity;

    public WirelessModuleMenuProvider(WirelessModuleHost host, BlockEntity blockEntity) {
        this.host = host;
        this.blockEntity = blockEntity;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.lumungus_storage.wireless_module");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return blockEntity.getBlockPos();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new WirelessModuleMenu(containerId, inventory, host, blockEntity);
    }
}
