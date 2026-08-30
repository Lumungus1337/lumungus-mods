package dev.lumungus.integration.toms;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerLevel;

public final class TomsRemoteConnectorInspector {
    private TomsRemoteConnectorInspector() {
    }

    public static TomsRemoteConnectorStatus inspect(ServerLevel level, TomsDryRunReport report) {
        if (!report.remoteConnectionsRequireScan()) {
            return TomsRemoteConnectorStatus.NONE_CONFIGURED;
        }
        if (!FabricLoader.getInstance().isModLoaded("toms_storage")) {
            return TomsRemoteConnectorStatus.RUNTIME_UNAVAILABLE;
        }
        try {
            return Toms211RemoteConnectorInspector.inspect(level, report);
        } catch (LinkageError incompatibleRuntime) {
            return TomsRemoteConnectorStatus.RUNTIME_UNAVAILABLE;
        }
    }
}
