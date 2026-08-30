package dev.lumungus.integration.toms;

import com.tom.storagemod.block.entity.InventoryCableConnectorBlockEntity;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Direct optional adapter for Tom's Storage 26.2-2.11.x. */
final class Toms211RemoteConnectorInspector {
    private Toms211RemoteConnectorInspector() {
    }

    static TomsRemoteConnectorStatus inspect(ServerLevel level, TomsDryRunReport report) {
        Set<BlockPos> localBlocks = new HashSet<>();
        report.blocks().forEach(block -> localBlocks.add(block.position()));

        boolean configured = false;
        boolean activeRemoteTarget = false;
        for (TomsDryRunBlock block : report.blocks()) {
            String path = block.plan().sourceId().getPath();
            if (!path.equals("inventory_cable_connector")
                    && !path.equals("inventory_cable_connector_framed")) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(block.position());
            if (!(blockEntity instanceof InventoryCableConnectorBlockEntity connector)) {
                return TomsRemoteConnectorStatus.CONFIGURED_NOT_READY;
            }
            if (connector.getChannel() == null) {
                continue;
            }
            configured = true;
            if (connector.getBeaconLevel() < 0) {
                return TomsRemoteConnectorStatus.CONFIGURED_NOT_READY;
            }
            for (var linked : connector.getConnectedConnectors()) {
                if (!(linked instanceof BlockEntity linkedBlockEntity)
                        || !(linkedBlockEntity.getLevel() instanceof ServerLevel linkedLevel)) {
                    return TomsRemoteConnectorStatus.CONFIGURED_NOT_READY;
                }
                if (linkedLevel != level || !localBlocks.contains(linkedBlockEntity.getBlockPos())) {
                    activeRemoteTarget = true;
                }
            }
        }

        if (!configured) {
            return TomsRemoteConnectorStatus.NONE_CONFIGURED;
        }
        return activeRemoteTarget
                ? TomsRemoteConnectorStatus.CONFIGURED_ACTIVE
                : TomsRemoteConnectorStatus.CONFIGURED_NOT_READY;
    }
}
