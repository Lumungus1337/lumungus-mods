package dev.lumungus.integration.toms;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface TomsInventoryWorldView extends TomsBlockWorldView {
    Optional<TomsReadOnlyInventoryEndpoint> inventoryAt(BlockPos pos, Direction side);
}
