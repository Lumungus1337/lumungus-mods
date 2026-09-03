package dev.lumungus.storage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class WorkBlockPower {
    private WorkBlockPower() {
    }

    public static boolean isPaused(Level level, BlockPos pos, Direction controlSide) {
        return level.getSignal(pos.relative(controlSide), controlSide) > 0;
    }
}
