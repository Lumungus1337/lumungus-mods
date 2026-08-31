package dev.lumungus.integration.toms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

final class TomsReadOnlyNetworkScannerTest {
    @Test
    void discoversConnectedTomsBlocksWithoutFollowingOrdinaryInventories() {
        FakeWorld world = new FakeWorld();
        world.put(0, "inventory_connector");
        world.put(1, "trim");
        world.put(2, "painted_trim");
        world.putOther(3, "minecraft:chest");

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, pos(0), 100);

        assertEquals(3, report.blocks().size());
        assertEquals(3, report.convertibleCount());
        assertEquals(0, report.readOnlySupportedCount());
        assertEquals(0, report.blockingCount());
        assertTrue(report.safeForInventorySnapshot());
    }

    @Test
    void acceptsBasicInventoryHoppersForReadOnlySnapshots() {
        FakeWorld world = new FakeWorld();
        world.put(0, "inventory_connector");
        world.put(1, "basic_inventory_hopper");

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, pos(0), 100);

        assertEquals(2, report.blocks().size());
        assertEquals(1, report.convertibleCount());
        assertEquals(1, report.readOnlySupportedCount());
        assertEquals(0, report.blockingCount());
        assertTrue(report.safeForInventorySnapshot());
    }

    @Test
    void reportsUnknownBlocksAndContentBearingFilingCabinetsAsBlockers() {
        FakeWorld world = new FakeWorld();
        world.put(0, "inventory_connector");
        world.put(1, "future_network_block");
        world.put(2, "filing_cabinet");

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, pos(0), 100);

        assertEquals(3, report.blocks().size());
        assertEquals(0, report.readOnlySupportedCount());
        assertEquals(2, report.blockingCount());
        assertFalse(report.safeForInventorySnapshot());
    }

    @Test
    void stopsAtNodeLimitWithoutSilentlyApprovingPartialScan() {
        FakeWorld world = new FakeWorld();
        for (int x = 0; x < 20; x++) {
            world.put(x, "inventory_cable");
        }

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, pos(0), 5);

        assertEquals(5, report.blocks().size());
        assertTrue(report.nodeLimitReached());
        assertFalse(report.safeForInventorySnapshot());
    }

    @Test
    void refusesApprovalAtAnUnloadedBoundary() {
        FakeWorld world = new FakeWorld();
        world.put(0, "inventory_connector");
        world.put(1, "inventory_cable");
        world.unload(pos(2));

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, pos(0), 100);

        assertTrue(report.unloadedBoundary());
        assertFalse(report.safeForInventorySnapshot());
    }

    @Test
    void refusesApprovalUntilRemoteConnectorsAreFollowed() {
        FakeWorld world = new FakeWorld();
        world.put(0, "inventory_connector");
        world.put(1, "inventory_cable_connector");

        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, pos(0), 100);

        assertTrue(report.remoteConnectionsRequireScan());
        assertFalse(report.safeForInventorySnapshot());
    }

    private static BlockPos pos(int x) {
        return new BlockPos(x, 0, 0);
    }

    private static final class FakeWorld implements TomsBlockWorldView {
        private static final Identifier AIR = Identifier.parse("minecraft:air");

        private final Map<BlockPos, Identifier> blocks = new HashMap<>();
        private final Set<BlockPos> unloaded = new HashSet<>();

        void put(int x, String path) {
            blocks.put(pos(x), Identifier.fromNamespaceAndPath("toms_storage", path));
        }

        void putOther(int x, String id) {
            blocks.put(pos(x), Identifier.parse(id));
        }

        void unload(BlockPos pos) {
            unloaded.add(pos.immutable());
        }

        @Override
        public boolean isLoaded(BlockPos pos) {
            return !unloaded.contains(pos);
        }

        @Override
        public Identifier blockId(BlockPos pos) {
            return blocks.getOrDefault(pos, AIR);
        }
    }
}
