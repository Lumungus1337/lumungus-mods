package dev.lumungus.integration.toms;

import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.integration.migration.MigrationInventorySnapshot;
import dev.lumungus.integration.migration.ReadOnlyInventorySnapshotter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Direct optional adapter for Tom's Storage 26.2-2.11.x inventory discovery. */
final class Toms211InventorySnapshotCollector {
    private static final Optional<Method> DETECT_TOUCHING_INVENTORIES = detectTouchingInventoriesMethod();

    private Toms211InventorySnapshotCollector() {
    }

    static Optional<MigrationInventorySnapshot> capture(List<TomsNetworkSegment> segments, String source) {
        Map<DimensionPos, TomsReadOnlyInventoryEndpoint> endpoints = new LinkedHashMap<>();
        for (TomsNetworkSegment segment : segments) {
            TomsDryRunReport report = segment.report().withRemoteConnectionsResolved();
            if (!report.safeForInventorySnapshot()) {
                throw new IllegalStateException("Tom's remote network dry run is incomplete or blocked");
            }
            MinecraftTomsInventoryWorldView world = new MinecraftTomsInventoryWorldView(segment.level());
            for (TomsDryRunBlock block : report.blocks()) {
                collectConnectorInventories(segment.level(), world, segment.level().dimension(), block.position(), endpoints);
            }
        }

        if (endpoints.isEmpty()) {
            return Optional.empty();
        }

        long slots = 0;
        List<ResourceAmount> resources = new ArrayList<>();
        for (TomsReadOnlyInventoryEndpoint endpoint : endpoints.values()) {
            slots = Math.addExact(slots, endpoint.slotCount());
            resources.addAll(endpoint.resources());
        }
        return Optional.of(ReadOnlyInventorySnapshotter.capture(source, endpoints.size(), slots, resources));
    }

    private static void collectConnectorInventories(
            ServerLevel level,
            MinecraftTomsInventoryWorldView world,
            ResourceKey<Level> dimension,
            BlockPos connectorPos,
            Map<DimensionPos, TomsReadOnlyInventoryEndpoint> endpoints
    ) {
        BlockEntity blockEntity = level.getBlockEntity(connectorPos);
        if (!(blockEntity instanceof InventoryConnectorBlockEntity connector)) {
            return;
        }
        refreshTouchingInventories(connector);
        for (BlockPos inventoryPos : connector.getConnectedBlocks()) {
            collectInventory(world, dimension, inventoryPos, endpoints);
        }
    }

    private static void collectInventory(
            MinecraftTomsInventoryWorldView world,
            ResourceKey<Level> dimension,
            BlockPos inventoryPos,
            Map<DimensionPos, TomsReadOnlyInventoryEndpoint> endpoints
    ) {
        for (Direction direction : Direction.values()) {
            Optional<TomsReadOnlyInventoryEndpoint> endpoint = world.inventoryAt(inventoryPos, direction);
            if (endpoint.isPresent()) {
                TomsReadOnlyInventoryEndpoint value = endpoint.orElseThrow();
                endpoints.putIfAbsent(new DimensionPos(dimension, value.key()), value);
                return;
            }
        }
    }

    private static void refreshTouchingInventories(InventoryConnectorBlockEntity connector) {
        DETECT_TOUCHING_INVENTORIES.ifPresent(method -> {
            try {
                method.invoke(connector);
            } catch (ReflectiveOperationException ignored) {
                // Fall back to Tom's already-populated connector cache.
            }
        });
    }

    private static Optional<Method> detectTouchingInventoriesMethod() {
        try {
            Method method = InventoryConnectorBlockEntity.class.getDeclaredMethod("detectTouchingInventories");
            method.setAccessible(true);
            return Optional.of(method);
        } catch (ReflectiveOperationException | SecurityException unavailable) {
            return Optional.empty();
        }
    }

    private record DimensionPos(ResourceKey<Level> dimension, BlockPos position) {
        private DimensionPos {
            position = position.immutable();
        }
    }
}
