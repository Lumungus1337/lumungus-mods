package dev.lumungus.storage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

final class WirelessStatusText {
    private WirelessStatusText() {
    }

    static Component position(BlockPos pos) {
        return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
    }
}
