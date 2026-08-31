package dev.lumungus.integration.toms;

import com.tom.storagemod.block.entity.InventoryCableConnectorBlockEntity;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Direct optional adapter for Tom's Storage 26.2-2.11.x. */
final class Toms211RemoteConnectorInspector {
    private Toms211RemoteConnectorInspector() {
    }

    static TomsRemoteScanResult resolve(ServerLevel level, TomsDryRunReport initialReport, int maxNodes) {
        List<TomsNetworkSegment> segments = new ArrayList<>();
        Queue<TomsNetworkSegment> pending = new ArrayDeque<>();
        Set<DimensionPos> discoveredBlocks = new HashSet<>();
        pending.add(new TomsNetworkSegment(level, initialReport));
        boolean configured = false;
        boolean remoteSegmentFound = false;
        int scannedNodes = 0;
        int scheduledNodes = initialReport.blocks().size();
        while (!pending.isEmpty()) {
            TomsNetworkSegment segment = pending.remove();
            scheduledNodes -= segment.report().blocks().size();
            if (containsDiscoveredBlock(segment, discoveredBlocks)) {
                continue;
            }
            segments.add(segment);
            scannedNodes = Math.addExact(scannedNodes, segment.report().blocks().size());
            segment.report().blocks().forEach(block -> discoveredBlocks.add(
                    new DimensionPos(segment.level().dimension(), block.position())
            ));

            for (TomsDryRunBlock block : segment.report().blocks()) {
                if (!isCableConnector(block)) {
                    continue;
                }
                BlockEntity blockEntity = segment.level().getBlockEntity(block.position());
                if (!(blockEntity instanceof InventoryCableConnectorBlockEntity connector)) {
                    return result(TomsRemoteConnectorStatus.CONFIGURED_NOT_READY, segments);
                }
                if (connector.getChannel() == null) {
                    continue;
                }
                configured = true;
                if (connector.getBeaconLevel() < 0 || connector.getConnectedConnectors().isEmpty()) {
                    return result(TomsRemoteConnectorStatus.CONFIGURED_NOT_READY, segments);
                }
                for (var linked : connector.getConnectedConnectors()) {
                    if (!(linked instanceof BlockEntity linkedBlockEntity)
                            || !(linkedBlockEntity.getLevel() instanceof ServerLevel linkedLevel)
                            || !linkedLevel.isLoaded(linkedBlockEntity.getBlockPos())) {
                        return result(TomsRemoteConnectorStatus.CONFIGURED_NOT_READY, segments);
                    }
                    DimensionPos linkedPos = new DimensionPos(
                            linkedLevel.dimension(), linkedBlockEntity.getBlockPos()
                    );
                    if (discoveredBlocks.contains(linkedPos)) {
                        continue;
                    }
                    int remainingNodes = maxNodes - scannedNodes - scheduledNodes;
                    if (remainingNodes <= 0) {
                        return result(TomsRemoteConnectorStatus.CONFIGURED_NOT_READY, segments);
                    }
                    TomsDryRunReport linkedReport = TomsReadOnlyNetworkScanner.scan(
                            new MinecraftTomsInventoryWorldView(linkedLevel),
                            linkedBlockEntity.getBlockPos(),
                            remainingNodes
                    );
                    pending.add(new TomsNetworkSegment(linkedLevel, linkedReport));
                    scheduledNodes = Math.addExact(scheduledNodes, linkedReport.blocks().size());
                    remoteSegmentFound = true;
                }
            }
        }

        if (!configured) {
            return result(TomsRemoteConnectorStatus.NONE_CONFIGURED, segments);
        }
        return result(
                remoteSegmentFound
                        ? TomsRemoteConnectorStatus.CONFIGURED_RESOLVED
                        : TomsRemoteConnectorStatus.CONFIGURED_NOT_READY,
                segments
        );
    }

    private static boolean containsDiscoveredBlock(
            TomsNetworkSegment segment,
            Set<DimensionPos> discoveredBlocks
    ) {
        return segment.report().blocks().stream().anyMatch(block -> discoveredBlocks.contains(
                new DimensionPos(segment.level().dimension(), block.position())
        ));
    }

    private static boolean isCableConnector(TomsDryRunBlock block) {
        String path = block.plan().sourceId().getPath();
        return path.equals("inventory_cable_connector")
                || path.equals("inventory_cable_connector_framed");
    }

    private static TomsRemoteScanResult result(
            TomsRemoteConnectorStatus status,
            List<TomsNetworkSegment> segments
    ) {
        return new TomsRemoteScanResult(status, segments);
    }

    private record DimensionPos(ResourceKey<Level> dimension, BlockPos position) {
        private DimensionPos {
            position = position.immutable();
        }
    }
}
