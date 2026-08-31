package dev.lumungus.storage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class WorkBlockPower {
    private WorkBlockPower() {
    }

    public static boolean isPaused(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos);
    }
}
