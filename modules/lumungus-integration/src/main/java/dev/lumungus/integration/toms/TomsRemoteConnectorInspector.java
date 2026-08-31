package dev.lumungus.integration.toms;

import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;

public final class TomsRemoteConnectorInspector {
    private TomsRemoteConnectorInspector() {
    }

    public static TomsRemoteConnectorStatus inspect(ServerLevel level, TomsDryRunReport report) {
        return resolve(level, report, 100_000).status();
    }

    public static TomsRemoteScanResult resolve(ServerLevel level, TomsDryRunReport report, int maxNodes) {
        if (maxNodes <= 0) {
            throw new IllegalArgumentException("Node limit must be positive");
        }
        if (!report.remoteConnectionsRequireScan()) {
            return new TomsRemoteScanResult(
                    TomsRemoteConnectorStatus.NONE_CONFIGURED,
                    List.of(new TomsNetworkSegment(level, report))
            );
        }
        if (!FabricLoader.getInstance().isModLoaded("toms_storage")) {
            return new TomsRemoteScanResult(
                    TomsRemoteConnectorStatus.RUNTIME_UNAVAILABLE,
                    List.of(new TomsNetworkSegment(level, report))
            );
        }
        try {
            return Toms211RemoteConnectorInspector.resolve(level, report, maxNodes);
        } catch (LinkageError incompatibleRuntime) {
            return new TomsRemoteScanResult(
                    TomsRemoteConnectorStatus.RUNTIME_UNAVAILABLE,
                    List.of(new TomsNetworkSegment(level, report))
            );
        }
    }
}
