package dev.lumungus.integration.toms;

import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.integration.migration.MigrationInventorySnapshot;
import dev.lumungus.integration.migration.ReadOnlyInventorySnapshotter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Collects physical inventory contents adjacent to Tom's inventory-facing network blocks. */
public final class TomsInventorySnapshotCollector {
    private static final Set<String> INVENTORY_FACING_BLOCKS = Set.of(
            "inventory_connector",
            "storage_terminal",
            "crafting_terminal",
            "trim",
            "painted_trim",
            "inventory_cable_connector",
            "inventory_cable_connector_framed"
    );

    private TomsInventorySnapshotCollector() {
    }

    public static MigrationInventorySnapshot capture(
            TomsInventoryWorldView world,
            TomsDryRunReport network,
            String source
    ) {
        if (!network.safeForInventorySnapshot()) {
            throw new IllegalStateException("Tom's network dry run is incomplete or blocked");
        }

        Map<BlockPos, TomsReadOnlyInventoryEndpoint> endpoints = new LinkedHashMap<>();
        for (TomsDryRunBlock block : network.blocks()) {
            if (!INVENTORY_FACING_BLOCKS.contains(block.plan().sourceId().getPath())) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos inventoryPos = block.position().relative(direction);
                world.inventoryAt(inventoryPos, direction.getOpposite())
                        .ifPresent(endpoint -> endpoints.putIfAbsent(endpoint.key(), endpoint));
            }
        }

        long slots = 0;
        List<ResourceAmount> resources = new ArrayList<>();
        for (TomsReadOnlyInventoryEndpoint endpoint : endpoints.values()) {
            slots = Math.addExact(slots, endpoint.slotCount());
            resources.addAll(endpoint.resources());
        }
        return ReadOnlyInventorySnapshotter.capture(source, endpoints.size(), slots, resources);
    }

    public static MigrationInventorySnapshot capture(List<TomsNetworkSegment> segments, String source) {
        Map<DimensionPos, TomsReadOnlyInventoryEndpoint> endpoints = new LinkedHashMap<>();
        for (TomsNetworkSegment segment : segments) {
            TomsDryRunReport report = segment.report().withRemoteConnectionsResolved();
            if (!report.safeForInventorySnapshot()) {
                throw new IllegalStateException("Tom's remote network dry run is incomplete or blocked");
            }
            TomsInventoryWorldView world = new MinecraftTomsInventoryWorldView(segment.level());
            for (TomsDryRunBlock block : report.blocks()) {
                if (!INVENTORY_FACING_BLOCKS.contains(block.plan().sourceId().getPath())) {
                    continue;
                }
                for (Direction direction : Direction.values()) {
                    BlockPos inventoryPos = block.position().relative(direction);
                    world.inventoryAt(inventoryPos, direction.getOpposite()).ifPresent(endpoint -> endpoints.putIfAbsent(
                            new DimensionPos(segment.level().dimension(), endpoint.key()),
                            endpoint
                    ));
                }
            }
        }

        long slots = 0;
        List<ResourceAmount> resources = new ArrayList<>();
        for (TomsReadOnlyInventoryEndpoint endpoint : endpoints.values()) {
            slots = Math.addExact(slots, endpoint.slotCount());
            resources.addAll(endpoint.resources());
        }
        return ReadOnlyInventorySnapshotter.capture(source, endpoints.size(), slots, resources);
    }

    private record DimensionPos(ResourceKey<Level> dimension, BlockPos position) {
        private DimensionPos {
            position = position.immutable();
        }
    }
}
