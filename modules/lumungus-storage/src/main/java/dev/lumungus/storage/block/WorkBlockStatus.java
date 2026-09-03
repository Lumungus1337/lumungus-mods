package dev.lumungus.storage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public final class WorkBlockStatus {
    private WorkBlockStatus() {
    }

    public static Component describe(Level level, BlockPos pos, Direction controlSide, boolean linked) {
        if (WorkBlockPower.isPaused(level, pos, controlSide)) {
            return Component.translatable("message.lumungus_storage.work_block.paused");
        }
        return Component.translatable(linked
                ? "message.lumungus_storage.work_block.running"
                : "message.lumungus_storage.work_block.unlinked");
    }
}
