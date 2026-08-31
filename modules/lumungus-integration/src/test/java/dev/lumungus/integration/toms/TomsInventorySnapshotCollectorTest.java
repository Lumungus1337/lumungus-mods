package dev.lumungus.integration.toms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.integration.migration.MigrationInventorySnapshot;
import dev.lumungus.integration.test.MinecraftTestBootstrap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class TomsInventorySnapshotCollectorTest {
    @BeforeAll
    static void initializeMinecraft() {
        MinecraftTestBootstrap.initialize();
    }

    @Test
    void deduplicatesAnInventoryTouchedByMultipleTrims() {
        FakeInventoryWorld world = new FakeInventoryWorld();
        world.block(new BlockPos(0, 0, 0), "trim");
        world.block(new BlockPos(1, 0, 0), "trim");
        TomsReadOnlyInventoryEndpoint shared = new TomsReadOnlyInventoryEndpoint(
                new BlockPos(0, 0, 1),
                54,
                java.util.List.of(new ResourceAmount(MinecraftTestBootstrap.stack(Items.COBBLESTONE), 7_000_000))
        );
        world.inventory(new BlockPos(0, 0, 1), shared);
        world.inventory(new BlockPos(1, 0, 1), shared);

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, new BlockPos(0, 0, 0), 100);
        MigrationInventorySnapshot snapshot = TomsInventorySnapshotCollector.capture(world, report, "Tom's dry run");

        assertEquals(1, snapshot.endpointCount());
        assertEquals(54, snapshot.slotCount());
        assertEquals(7_000_000, snapshot.totalAmount());
    }

    @Test
    void snapshotsInventoriesTouchedByBasicInventoryHoppers() {
        FakeInventoryWorld world = new FakeInventoryWorld();
        world.block(new BlockPos(0, 0, 0), "inventory_connector");
        world.block(new BlockPos(1, 0, 0), "basic_inventory_hopper");
        TomsReadOnlyInventoryEndpoint hopperEndpoint = new TomsReadOnlyInventoryEndpoint(
                new BlockPos(1, 0, 1),
                27,
                java.util.List.of(new ResourceAmount(MinecraftTestBootstrap.stack(Items.BARREL), 120))
        );
        world.inventory(new BlockPos(1, 0, 1), hopperEndpoint);

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, new BlockPos(0, 0, 0), 100);
        MigrationInventorySnapshot snapshot = TomsInventorySnapshotCollector.capture(world, report, "Tom's dry run");

        assertEquals(1, snapshot.endpointCount());
        assertEquals(27, snapshot.slotCount());
        assertEquals(120, snapshot.totalAmount());
    }

    @Test
    void refusesToSnapshotAnIncompleteNetworkScan() {
        FakeInventoryWorld world = new FakeInventoryWorld();
        world.block(new BlockPos(0, 0, 0), "inventory_cable");
        world.block(new BlockPos(1, 0, 0), "inventory_cable");
        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, new BlockPos(0, 0, 0), 1);

        assertThrows(
                IllegalStateException.class,
                () -> TomsInventorySnapshotCollector.capture(world, report, "Incomplete")
        );
    }

    private static final class FakeInventoryWorld implements TomsInventoryWorldView {
        private static final Identifier AIR = Identifier.parse("minecraft:air");
        private final Map<BlockPos, Identifier> blocks = new HashMap<>();
        private final Map<BlockPos, TomsReadOnlyInventoryEndpoint> inventories = new HashMap<>();

        void block(BlockPos pos, String path) {
            blocks.put(pos.immutable(), Identifier.fromNamespaceAndPath("toms_storage", path));
        }

        void inventory(BlockPos pos, TomsReadOnlyInventoryEndpoint endpoint) {
            inventories.put(pos.immutable(), endpoint);
        }

        @Override
        public boolean isLoaded(BlockPos pos) {
            return true;
        }

        @Override
        public Identifier blockId(BlockPos pos) {
            return blocks.getOrDefault(pos, AIR);
        }

        @Override
        public Optional<TomsReadOnlyInventoryEndpoint> inventoryAt(BlockPos pos, Direction side) {
            return Optional.ofNullable(inventories.get(pos));
        }
    }
}
