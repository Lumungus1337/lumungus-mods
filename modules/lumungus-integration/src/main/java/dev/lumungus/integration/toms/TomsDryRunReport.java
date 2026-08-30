package dev.lumungus.integration.toms;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;

public record TomsDryRunReport(
        BlockPos start,
        List<TomsDryRunBlock> blocks,
        boolean unloadedBoundary,
        boolean nodeLimitReached,
        boolean remoteConnectionsRequireScan
) {
    public TomsDryRunReport {
        start = Objects.requireNonNull(start, "start").immutable();
        blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks"));
    }

    public long convertibleCount() {
        return blocks.stream()
                .filter(block -> block.plan().disposition() == TomsMigrationDisposition.CONVERTIBLE)
                .count();
    }

    public long blockingCount() {
        return blocks.size() - convertibleCount();
    }

    public boolean safeForInventorySnapshot() {
        return !blocks.isEmpty()
                && blockingCount() == 0
                && !unloadedBoundary
                && !nodeLimitReached
                && !remoteConnectionsRequireScan;
    }
}
