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
                .filter(block -> block.plan().disposition().convertible())
                .count();
    }

    public long readOnlySupportedCount() {
        return blocks.stream()
                .filter(block -> block.plan().disposition() == TomsMigrationDisposition.READ_ONLY_SUPPORTED)
                .count();
    }

    public long blockingCount() {
        return blocks.stream()
                .filter(block -> !block.plan().disposition().safeForInventorySnapshot())
                .count();
    }

    public boolean safeForInventorySnapshot() {
        return !blocks.isEmpty()
                && blockingCount() == 0
                && !unloadedBoundary
                && !nodeLimitReached
                && !remoteConnectionsRequireScan;
    }

    public TomsDryRunReport withRemoteConnectionsResolved() {
        return new TomsDryRunReport(start, blocks, unloadedBoundary, nodeLimitReached, false);
    }
}
