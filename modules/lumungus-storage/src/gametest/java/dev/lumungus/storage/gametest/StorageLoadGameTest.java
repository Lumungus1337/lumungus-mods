package dev.lumungus.storage.gametest;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.storage.StorageSnapshot;
import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.block.entity.InventoryConnectorBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

public final class StorageLoadGameTest implements CustomTestMethodInvoker {
    private static final BlockPos CONTROLLER_POS = new BlockPos(1, 1, 7);
    private static final int FIRST_COLUMN = 2;
    private static final int LAST_COLUMN = 13;
    private static final int FILLED_SLOTS_PER_CHEST = 26;
    private static final int SCAN_ITERATIONS = 30;
    private static final int LARGE_WAREHOUSE_LEVELS = 10;
    private static final int LARGE_SCAN_ITERATIONS = 5;
    private static final long MAX_SCAN_TIME_MILLIS = 10_000;
    private static final long MAX_LARGE_SCAN_TIME_MILLIS = 15_000;
    private static final List<Item> TEST_ITEMS = List.of(
            Items.COBBLESTONE,
            Items.DIRT,
            Items.OAK_LOG,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.REDSTONE,
            Items.COAL,
            Items.DIAMOND,
            Items.SAND,
            Items.GRAVEL,
            Items.STONE,
            Items.GLASS
    );
    private static final List<Item> LARGE_TEST_ITEMS = List.of(
            Items.COBBLESTONE,
            Items.DIRT,
            Items.OAK_LOG,
            Items.IRON_INGOT,
            Items.GOLD_INGOT,
            Items.REDSTONE,
            Items.COAL,
            Items.DIAMOND,
            Items.SAND,
            Items.GRAVEL,
            Items.STONE,
            Items.GLASS,
            Items.NETHERRACK,
            Items.DEEPSLATE,
            Items.COPPER_INGOT,
            Items.LAPIS_LAZULI,
            Items.EMERALD,
            Items.QUARTZ,
            Items.ANDESITE,
            Items.DIORITE,
            Items.GRANITE,
            Items.TUFF,
            Items.BLACKSTONE,
            Items.END_STONE
    );

    @GameTest
    public void scansLargeMultiLevelPhysicalWarehouse(GameTestHelper context) {
        int inventoryCount = (LAST_COLUMN - FIRST_COLUMN + 1) * LARGE_WAREHOUSE_LEVELS * 2;
        context.setBlock(CONTROLLER_POS, LumungusStorageBlocks.STORAGE_CONTROLLER);

        long expectedTotal = 0;
        long expectedCobblestone = 0;
        int inventoryIndex = 0;
        for (int x = FIRST_COLUMN; x <= LAST_COLUMN; x++) {
            for (int y = 1; y <= LARGE_WAREHOUSE_LEVELS; y++) {
                context.setBlock(new BlockPos(x, y, 7), LumungusStorageBlocks.INVENTORY_CABLE);
                InventoryFillResult north = addInventory(
                        context,
                        new BlockPos(x, y, 6),
                        new BlockPos(x, y, 5),
                        inventoryIndex++,
                        LARGE_TEST_ITEMS
                );
                InventoryFillResult south = addInventory(
                        context,
                        new BlockPos(x, y, 8),
                        new BlockPos(x, y, 9),
                        inventoryIndex++,
                        LARGE_TEST_ITEMS
                );
                expectedTotal += north.total() + south.total();
                expectedCobblestone += north.cobblestone() + south.cobblestone();
            }
        }

        StorageControllerBlockEntity controller = context.getBlockEntity(
                CONTROLLER_POS,
                StorageControllerBlockEntity.class
        );
        StorageNetworkTopology.invalidate(context.getLevel());
        StorageControllerBlockEntity.NetworkStatus status = controller.refreshNetwork();
        context.assertValueEqual(status.linkedInventoryConnectors(), inventoryCount, "Large warehouse connectors");

        StorageSnapshot initial = controller.snapshot();
        context.assertValueEqual(initial.storedTotalAmount(), expectedTotal, "Large warehouse item total");
        context.assertValueEqual(initial.storedDistinctTypes(), LARGE_TEST_ITEMS.size(), "Large warehouse types");
        long physicalCapacity = controller.capacity().maxTotalAmount();
        context.assertTrue(
                physicalCapacity >= expectedTotal + inventoryCount * 64L,
                "Large warehouse did not expose its physical free space: " + physicalCapacity
        );

        long started = System.nanoTime();
        for (int iteration = 0; iteration < LARGE_SCAN_ITERATIONS; iteration++) {
            long scannedTotal = 0;
            for (Item item : LARGE_TEST_ITEMS) {
                scannedTotal += controller.count(new ItemStack(item));
            }
            context.assertValueEqual(scannedTotal, expectedTotal, "Repeated large warehouse scan");
        }
        long elapsedMillis = (System.nanoTime() - started) / 1_000_000;
        context.assertTrue(
                elapsedMillis < MAX_LARGE_SCAN_TIME_MILLIS,
                "Large warehouse scans exceeded " + MAX_LARGE_SCAN_TIME_MILLIS + " ms: " + elapsedMillis + " ms"
        );

        StorageNetworkTopology.CacheStats cacheStats = StorageNetworkTopology.cacheStats(context.getLevel());
        context.assertTrue(
                cacheStats.hits() >= (long) LARGE_SCAN_ITERATIONS * LARGE_TEST_ITEMS.size(),
                "Large warehouse topology cache was not reused"
        );
        context.assertTrue(cacheStats.hits() > cacheStats.misses(), "Large warehouse cache misses dominated hits");

        context.assertTrue(
                controller.insert(new ItemStack(Items.NETHER_STAR, 64), TransferMode.EXECUTE).isEmpty(),
                "Large warehouse insertion failed"
        );
        ItemStack extracted = controller.extract(
                new ItemStack(Items.COBBLESTONE),
                64,
                TransferMode.EXECUTE
        );
        context.assertValueEqual(extracted.getCount(), 64, "Large warehouse extraction amount");
        context.assertValueEqual(
                controller.count(new ItemStack(Items.COBBLESTONE)),
                expectedCobblestone - 64,
                "Large warehouse remaining cobblestone"
        );

        LumungusStorage.LOGGER.info(
                "Large physical warehouse test: {} inventories, {} items / {} capacity, {} types, {} full scans in {} ms",
                inventoryCount,
                expectedTotal,
                physicalCapacity,
                LARGE_TEST_ITEMS.size(),
                LARGE_SCAN_ITERATIONS,
                elapsedMillis
        );
        context.succeed();
    }

    @GameTest
    public void scansDensePhysicalWarehouseWithoutCells(GameTestHelper context) {
        context.setBlock(CONTROLLER_POS, LumungusStorageBlocks.STORAGE_CONTROLLER);
        for (int x = FIRST_COLUMN; x <= LAST_COLUMN; x++) {
            context.setBlock(new BlockPos(x, 1, 7), LumungusStorageBlocks.INVENTORY_CABLE);
        }

        long expectedTotal = 0;
        long expectedCobblestone = 0;
        int inventoryIndex = 0;
        List<BlockPos> connectorPositions = new ArrayList<>();
        for (int x = FIRST_COLUMN; x <= LAST_COLUMN; x++) {
            BlockPos northConnectorPos = new BlockPos(x, 1, 6);
            BlockPos southConnectorPos = new BlockPos(x, 1, 8);
            BlockPos upperConnectorPos = new BlockPos(x, 2, 7);
            connectorPositions.add(northConnectorPos);
            connectorPositions.add(southConnectorPos);
            connectorPositions.add(upperConnectorPos);
            InventoryFillResult north = addInventory(
                    context,
                    northConnectorPos,
                    new BlockPos(x, 1, 5),
                    inventoryIndex++,
                    TEST_ITEMS
            );
            InventoryFillResult south = addInventory(
                    context,
                    southConnectorPos,
                    new BlockPos(x, 1, 9),
                    inventoryIndex++,
                    TEST_ITEMS
            );
            InventoryFillResult upper = addInventory(
                    context,
                    upperConnectorPos,
                    new BlockPos(x, 3, 7),
                    inventoryIndex++,
                    TEST_ITEMS
            );
            expectedTotal += north.total() + south.total() + upper.total();
            expectedCobblestone += north.cobblestone() + south.cobblestone() + upper.cobblestone();
        }

        StorageControllerBlockEntity controller = context.getBlockEntity(
                CONTROLLER_POS,
                StorageControllerBlockEntity.class
        );
        StorageNetworkTopology.invalidate(context.getLevel());
        StorageControllerBlockEntity.NetworkStatus status = controller.refreshNetwork();
        List<String> missingConnectors = connectorPositions.stream()
                .map(pos -> context.getBlockEntity(pos, InventoryConnectorBlockEntity.class))
                .filter(connector -> !connector.isLinkedTo(controller))
                .map(connector -> connector.getBlockPos().toShortString()
                        + " loaded=" + context.getLevel().isLoaded(connector.getBlockPos())
                        + " reachesController=" + StorageNetworkTopology.connectedNodes(
                                context.getLevel(),
                                connector.getBlockPos()
                        ).contains(controller.getBlockPos()))
                .toList();
        context.assertTrue(
                status.linkedInventoryConnectors() == 36,
                "Linked load-test connectors: expected 36, actual "
                        + status.linkedInventoryConnectors()
                        + ", missing "
                        + missingConnectors
        );
        context.assertValueEqual(status.linkedDriveBays(), 0, "Load-test Drive Bays");

        StorageSnapshot initial = controller.snapshot();
        context.assertValueEqual(initial.storedTotalAmount(), expectedTotal, "Initial warehouse item total");
        context.assertValueEqual(initial.storedDistinctTypes(), TEST_ITEMS.size(), "Warehouse item types");
        long physicalCapacity = controller.capacity().maxTotalAmount();
        context.assertTrue(
                physicalCapacity >= expectedTotal + 36L * 64,
                "Warehouse did not expose the guaranteed physical free space: " + physicalCapacity
        );

        long started = System.nanoTime();
        for (int iteration = 0; iteration < SCAN_ITERATIONS; iteration++) {
            long scannedTotal = 0;
            for (Item item : TEST_ITEMS) {
                scannedTotal += controller.count(new ItemStack(item));
            }
            context.assertValueEqual(scannedTotal, expectedTotal, "Repeated warehouse scan");
        }
        long elapsedMillis = (System.nanoTime() - started) / 1_000_000;
        context.assertTrue(
                elapsedMillis < MAX_SCAN_TIME_MILLIS,
                "Warehouse scans exceeded " + MAX_SCAN_TIME_MILLIS + " ms: " + elapsedMillis + " ms"
        );

        StorageNetworkTopology.CacheStats cacheStats = StorageNetworkTopology.cacheStats(context.getLevel());
        context.assertTrue(
                cacheStats.hits() >= (long) SCAN_ITERATIONS * TEST_ITEMS.size(),
                "Warehouse topology cache was not reused"
        );
        context.assertTrue(cacheStats.hits() > cacheStats.misses(), "Warehouse cache misses dominated hits");

        ItemStack insertRemainder = controller.insert(new ItemStack(Items.NETHER_STAR, 36), TransferMode.EXECUTE);
        context.assertTrue(insertRemainder.isEmpty(), "Warehouse did not use its distributed free slots");
        context.assertValueEqual(controller.count(new ItemStack(Items.NETHER_STAR)), 36L, "Inserted nether stars");

        ItemStack extracted = controller.extract(
                new ItemStack(Items.COBBLESTONE),
                64,
                TransferMode.EXECUTE
        );
        context.assertValueEqual(extracted.getCount(), 64, "Warehouse extraction amount");
        context.assertValueEqual(
                controller.count(new ItemStack(Items.COBBLESTONE)),
                expectedCobblestone - 64,
                "Remaining warehouse cobblestone"
        );

        LumungusStorage.LOGGER.info(
                "Physical warehouse load test: {} inventories, {} items / {} capacity, {} types, {} full scans in {} ms",
                36,
                expectedTotal,
                physicalCapacity,
                TEST_ITEMS.size(),
                SCAN_ITERATIONS,
                elapsedMillis
        );
        context.succeed();
    }

    private static InventoryFillResult addInventory(
            GameTestHelper context,
            BlockPos connectorPos,
            BlockPos chestPos,
            int inventoryIndex,
            List<Item> items
    ) {
        context.setBlock(connectorPos, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = context.getBlockEntity(chestPos, ChestBlockEntity.class);

        long cobblestone = 0;
        for (int slot = 0; slot < FILLED_SLOTS_PER_CHEST; slot++) {
            Item item = items.get((inventoryIndex + slot) % items.size());
            chest.setItem(slot, new ItemStack(item, 64));
            if (item == Items.COBBLESTONE) {
                cobblestone += 64;
            }
        }
        return new InventoryFillResult(FILLED_SLOTS_PER_CHEST * 64L, cobblestone);
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }

    private record InventoryFillResult(long total, long cobblestone) {
    }
}
