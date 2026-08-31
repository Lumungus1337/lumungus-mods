package dev.lumungus.storage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public final class WorkBlockStatus {
    private WorkBlockStatus() {
    }

    public static Component describe(Level level, BlockPos pos, boolean linked) {
        if (WorkBlockPower.isPaused(level, pos)) {
            return Component.translatable("message.lumungus_storage.work_block.paused");
        }
        return Component.translatable(linked
                ? "message.lumungus_storage.work_block.running"
                : "message.lumungus_storage.work_block.unlinked");
    }
}
