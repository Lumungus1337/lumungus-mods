package dev.lumungus.integration.toms;

import java.util.Objects;
import net.minecraft.server.level.ServerLevel;

public record TomsNetworkSegment(ServerLevel level, TomsDryRunReport report) {
    public TomsNetworkSegment {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(report, "report");
    }
}
