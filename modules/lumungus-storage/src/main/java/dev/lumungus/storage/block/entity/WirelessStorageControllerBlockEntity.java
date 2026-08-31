package dev.lumungus.storage.block.entity;

import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WirelessStorageControllerBlockEntity extends BlockEntity {
    public WirelessStorageControllerBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.WIRELESS_STORAGE_CONTROLLER, pos, state);
    }
}
