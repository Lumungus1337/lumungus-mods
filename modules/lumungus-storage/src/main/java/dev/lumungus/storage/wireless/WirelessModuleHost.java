package dev.lumungus.storage.wireless;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface WirelessModuleHost {
    ItemStack wirelessModule();

    boolean installWirelessModule(ItemStack module);

    ItemStack removeWirelessModule();

    default void dropWirelessModule(Level level, BlockPos pos) {
        ItemStack removed = removeWirelessModule();
        if (!removed.isEmpty()) {
            Block.popResource(level, pos, removed);
        }
    }
}
