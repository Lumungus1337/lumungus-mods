package dev.lumungus.integration.toms;

import java.util.List;
import java.util.Objects;

public record TomsRemoteScanResult(
        TomsRemoteConnectorStatus status,
        List<TomsNetworkSegment> segments
) {
    public TomsRemoteScanResult {
        Objects.requireNonNull(status, "status");
        segments = List.copyOf(Objects.requireNonNull(segments, "segments"));
    }

    public boolean resolved() {
        return status == TomsRemoteConnectorStatus.NONE_CONFIGURED
                || status == TomsRemoteConnectorStatus.CONFIGURED_RESOLVED;
    }

    public int blockCount() {
        return segments.stream().mapToInt(segment -> segment.report().blocks().size()).sum();
    }

    public long convertibleCount() {
        return segments.stream().mapToLong(segment -> segment.report().convertibleCount()).sum();
    }

    public long blockingCount() {
        return segments.stream().mapToLong(segment -> segment.report().blockingCount()).sum();
    }

    public boolean safeForInventorySnapshot() {
        return resolved() && !segments.isEmpty() && segments.stream()
                .map(TomsNetworkSegment::report)
                .map(TomsDryRunReport::withRemoteConnectionsResolved)
                .allMatch(TomsDryRunReport::safeForInventorySnapshot);
    }
}
