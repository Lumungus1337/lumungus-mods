package dev.lumungus.storage.gametest;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.storage.block.CraftingTerminalBlock;
import dev.lumungus.storage.block.DriveBayBlock;
import dev.lumungus.storage.block.InventoryCableBlock;
import dev.lumungus.storage.block.InventoryConnectorBlock;
import dev.lumungus.storage.block.StorageBreakerBlock;
import dev.lumungus.storage.block.StorageControllerBlock;
import dev.lumungus.storage.block.StorageOutputBlock;
import dev.lumungus.storage.block.StoragePlacerBlock;
import dev.lumungus.storage.block.WirelessInventoryConnectorBlock;
import dev.lumungus.storage.block.WirelessStorageControllerBlock;
import dev.lumungus.storage.block.entity.CraftingTerminalBlockEntity;
import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.block.entity.InventoryConnectorBlockEntity;
import dev.lumungus.storage.block.entity.StorageBreakerBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.block.entity.StorageOutputBlockEntity;
import dev.lumungus.storage.block.entity.StoragePlacerBlockEntity;
import dev.lumungus.storage.block.entity.WirelessInventoryConnectorBlockEntity;
import dev.lumungus.storage.block.entity.WirelessStorageControllerBlockEntity;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.inventory.FabricItemStorageAccess;
import dev.lumungus.storage.menu.DriveBayMenu;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.menu.WirelessModuleMenu;
import dev.lumungus.storage.network.TerminalActionPayload;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import dev.lumungus.storage.registry.LumungusStorageItems;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;

public final class StorageNetworkGameTest implements CustomTestMethodInvoker {
    private static final BlockPos FIRST_CONTROLLER = new BlockPos(1, 1, 1);
    private static final BlockPos DRIVE_BAY = new BlockPos(4, 1, 1);
    private static final BlockPos SECOND_CONTROLLER = new BlockPos(3, 1, 1);
    private static final BlockPos CRAFTING_TERMINAL = new BlockPos(2, 1, 1);
    private static final BlockPos PHYSICAL_CHEST = new BlockPos(6, 1, 1);
    private static final BlockPos PHYSICAL_CONTROLLER = new BlockPos(5, 1, 3);
    private static final BlockPos FIRST_INVENTORY_CONNECTOR = new BlockPos(6, 1, 3);
    private static final BlockPos CONNECTED_CHEST = new BlockPos(7, 1, 3);
    private static final BlockPos SECOND_INVENTORY_CONNECTOR = new BlockPos(8, 1, 3);
    private static final BlockPos CABLE_CONTROLLER = new BlockPos(1, 1, 6);
    private static final BlockPos DISTANT_CONNECTOR = new BlockPos(11, 1, 6);
    private static final BlockPos DISTANT_CHEST = new BlockPos(12, 1, 6);
    private static final BlockPos DEVICE_CABLE_CONTROLLER = new BlockPos(1, 1, 9);
    private static final BlockPos DISTANT_DRIVE_BAY = new BlockPos(11, 1, 9);
    private static final BlockPos DISTANT_TERMINAL = new BlockPos(10, 1, 10);
    private static final BlockPos WORK_CONTROLLER = new BlockPos(1, 1, 18);
    private static final BlockPos WORK_DRIVE_BAY = new BlockPos(2, 1, 18);

    @GameTest(padding = 32)
    public void inventoryTrimConnectsTouchingChestsWithoutSeparateConnectors(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 15);
        BlockPos chestPos = new BlockPos(13, 1, 15);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        for (int x = 2; x <= 12; x++) {
            context.setBlock(new BlockPos(x, 1, 15), LumungusStorageBlocks.INVENTORY_TRIM);
        }
        context.setBlock(chestPos, Blocks.CHEST);

        ChestBlockEntity chest = context.getBlockEntity(chestPos, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.DIAMOND, 31));
        StorageControllerBlockEntity controller = context.getBlockEntity(
                controllerPos,
                StorageControllerBlockEntity.class
        );

        context.assertValueEqual(controller.count(new ItemStack(Items.DIAMOND)), 31L, "Trim-connected chest count");

        context.setBlock(new BlockPos(7, 1, 15), Blocks.AIR);
        context.assertValueEqual(controller.count(new ItemStack(Items.DIAMOND)), 0L, "Disconnected trim chest count");
        context.assertValueEqual(chest.getItem(0).getCount(), 31, "Trim disconnect preserved chest contents");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void storageOutputExportsAFilteredStackIntoAdjacentInventory(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(WORK_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        BlockPos outputPos = new BlockPos(3, 1, 18);
        BlockPos chestPos = new BlockPos(4, 1, 18);
        BlockPos ignoredChestPos = new BlockPos(3, 1, 19);
        context.setBlock(outputPos, LumungusStorageBlocks.STORAGE_OUTPUT.defaultBlockState()
                .setValue(StorageOutputBlock.FACING, Direction.EAST));
        context.setBlock(chestPos, Blocks.CHEST);
        context.setBlock(ignoredChestPos, Blocks.CHEST);

        DriveBayBlockEntity driveBay = context.getBlockEntity(WORK_DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.COPPER_INGOT, 48), TransferMode.EXECUTE);
        controller.insert(new ItemStack(Items.DIAMOND, 5), TransferMode.EXECUTE);

        StorageOutputBlockEntity output = context.getBlockEntity(outputPos, StorageOutputBlockEntity.class);
        output.setFilter(new ItemStack(Items.COPPER_INGOT));
        output.exportOneStack();

        ChestBlockEntity chest = context.getBlockEntity(chestPos, ChestBlockEntity.class);
        ChestBlockEntity ignoredChest = context.getBlockEntity(ignoredChestPos, ChestBlockEntity.class);
        context.assertValueEqual(chest.getItem(0).getCount(), 48, "Exported copper");
        context.assertTrue(ignoredChest.isEmpty(), "Output exported into a non-facing inventory");
        context.assertValueEqual(controller.count(new ItemStack(Items.COPPER_INGOT)), 0L, "Remaining copper");
        context.assertValueEqual(controller.count(new ItemStack(Items.DIAMOND)), 5L, "Unfiltered diamonds");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void storageBreakerBreaksFilteredBlockIntoStorage(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(WORK_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        BlockPos breakerPos = new BlockPos(3, 2, 18);
        BlockPos targetPos = breakerPos.east();
        BlockPos ignoredTargetPos = breakerPos.below();
        context.setBlock(breakerPos, LumungusStorageBlocks.STORAGE_BREAKER.defaultBlockState()
                .setValue(StorageBreakerBlock.FACING, Direction.EAST));
        context.setBlock(targetPos, Blocks.DIRT);
        context.setBlock(ignoredTargetPos, Blocks.STONE);

        DriveBayBlockEntity driveBay = context.getBlockEntity(WORK_DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageBreakerBlockEntity breaker = context.getBlockEntity(breakerPos, StorageBreakerBlockEntity.class);
        breaker.setFilter(new ItemStack(Items.DIRT));
        breaker.breakBelow();

        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        context.assertTrue(context.getLevel().getBlockState(context.absolutePos(targetPos)).isAir(), "Dirt was not broken");
        context.assertTrue(
                context.getLevel().getBlockState(context.absolutePos(ignoredTargetPos)).is(Blocks.STONE),
                "Breaker ignored its facing direction"
        );
        context.assertValueEqual(controller.count(new ItemStack(Items.DIRT)), 1L, "Stored dirt drop");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void storageBreakerDoesNotBreakLumungusDevices(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(WORK_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        BlockPos breakerPos = new BlockPos(3, 2, 18);
        BlockPos protectedDevicePos = breakerPos.east();
        context.setBlock(breakerPos, LumungusStorageBlocks.STORAGE_BREAKER.defaultBlockState()
                .setValue(StorageBreakerBlock.FACING, Direction.EAST));
        context.setBlock(protectedDevicePos, LumungusStorageBlocks.STORAGE_PLACER);

        DriveBayBlockEntity driveBay = context.getBlockEntity(WORK_DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageBreakerBlockEntity breaker = context.getBlockEntity(breakerPos, StorageBreakerBlockEntity.class);
        breaker.breakBelow();

        context.assertTrue(
                context.getLevel().getBlockState(context.absolutePos(protectedDevicePos)).is(LumungusStorageBlocks.STORAGE_PLACER),
                "Breaker removed a Lumungus device"
        );
        context.succeed();
    }

    @GameTest(padding = 32)
    public void storagePlacerPlacesFilteredBlockFromStorage(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(WORK_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        BlockPos placerPos = new BlockPos(3, 2, 18);
        BlockPos targetPos = placerPos.east();
        BlockPos ignoredTargetPos = placerPos.below();
        context.setBlock(placerPos, LumungusStorageBlocks.STORAGE_PLACER.defaultBlockState()
                .setValue(StoragePlacerBlock.FACING, Direction.EAST));

        DriveBayBlockEntity driveBay = context.getBlockEntity(WORK_DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.DIRT, 3), TransferMode.EXECUTE);

        StoragePlacerBlockEntity placer = context.getBlockEntity(placerPos, StoragePlacerBlockEntity.class);
        placer.setFilter(new ItemStack(Items.DIRT));
        placer.placeBelow();

        context.assertTrue(context.getLevel().getBlockState(context.absolutePos(targetPos)).is(Blocks.DIRT), "Dirt was not placed");
        context.assertTrue(
                context.getLevel().getBlockState(context.absolutePos(ignoredTargetPos)).isAir(),
                "Placer ignored its facing direction"
        );
        context.assertValueEqual(controller.count(new ItemStack(Items.DIRT)), 2L, "Remaining stored dirt");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void poweredStorageWorkBlocksPauseAutomation(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(WORK_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        DriveBayBlockEntity driveBay = context.getBlockEntity(WORK_DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.COPPER_INGOT, 32), TransferMode.EXECUTE);
        controller.insert(new ItemStack(Items.DIRT, 3), TransferMode.EXECUTE);

        BlockPos outputPos = new BlockPos(3, 1, 18);
        BlockPos outputChestPos = outputPos.east();
        context.setBlock(outputPos, LumungusStorageBlocks.STORAGE_OUTPUT.defaultBlockState()
                .setValue(StorageOutputBlock.FACING, Direction.EAST));
        context.setBlock(outputChestPos, Blocks.CHEST);
        context.setBlock(outputPos.above(), Blocks.REDSTONE_BLOCK);
        StorageOutputBlockEntity output = context.getBlockEntity(outputPos, StorageOutputBlockEntity.class);
        output.setFilter(new ItemStack(Items.COPPER_INGOT));
        output.exportOneStack();
        ChestBlockEntity outputChest = context.getBlockEntity(outputChestPos, ChestBlockEntity.class);
        context.assertTrue(outputChest.isEmpty(), "Powered Output exported items");

        BlockPos breakerPos = new BlockPos(6, 2, 18);
        BlockPos breakerTargetPos = breakerPos.east();
        context.setBlock(breakerPos, LumungusStorageBlocks.STORAGE_BREAKER.defaultBlockState()
                .setValue(StorageBreakerBlock.FACING, Direction.EAST));
        context.setBlock(breakerTargetPos, Blocks.DIRT);
        context.setBlock(breakerPos.above(), Blocks.REDSTONE_BLOCK);
        StorageBreakerBlockEntity breaker = context.getBlockEntity(breakerPos, StorageBreakerBlockEntity.class);
        breaker.breakBelow();
        context.assertTrue(
                context.getLevel().getBlockState(context.absolutePos(breakerTargetPos)).is(Blocks.DIRT),
                "Powered Breaker removed its target"
        );

        BlockPos placerPos = new BlockPos(9, 2, 18);
        BlockPos placerTargetPos = placerPos.east();
        context.setBlock(placerPos, LumungusStorageBlocks.STORAGE_PLACER.defaultBlockState()
                .setValue(StoragePlacerBlock.FACING, Direction.EAST));
        context.setBlock(placerPos.above(), Blocks.REDSTONE_BLOCK);
        StoragePlacerBlockEntity placer = context.getBlockEntity(placerPos, StoragePlacerBlockEntity.class);
        placer.setFilter(new ItemStack(Items.DIRT));
        placer.placeBelow();
        context.assertTrue(
                context.getLevel().getBlockState(context.absolutePos(placerTargetPos)).isAir(),
                "Powered Placer placed its target"
        );
        context.succeed();
    }

    @GameTest(padding = 32)
    public void unfilteredStorageWorkBlocksUseAvailableResources(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 21);
        BlockPos driveBayPos = new BlockPos(2, 1, 21);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(driveBayPos, LumungusStorageBlocks.DRIVE_BAY);

        DriveBayBlockEntity driveBay = context.getBlockEntity(driveBayPos, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                controllerPos,
                StorageControllerBlockEntity.class
        );

        BlockPos outputPos = new BlockPos(3, 1, 21);
        BlockPos outputChestPos = outputPos.east();
        context.setBlock(outputPos, LumungusStorageBlocks.STORAGE_OUTPUT.defaultBlockState()
                .setValue(StorageOutputBlock.FACING, Direction.EAST));
        context.setBlock(outputChestPos, Blocks.CHEST);
        controller.insert(new ItemStack(Items.COPPER_INGOT, 18), TransferMode.EXECUTE);
        StorageOutputBlockEntity output = context.getBlockEntity(outputPos, StorageOutputBlockEntity.class);
        output.exportOneStack();
        ChestBlockEntity outputChest = context.getBlockEntity(outputChestPos, ChestBlockEntity.class);
        context.assertValueEqual(outputChest.getItem(0).getCount(), 18, "Unfiltered Output item count");

        BlockPos breakerPos = new BlockPos(6, 1, 21);
        BlockPos breakerTargetPos = breakerPos.east();
        context.setBlock(breakerPos, LumungusStorageBlocks.STORAGE_BREAKER.defaultBlockState()
                .setValue(StorageBreakerBlock.FACING, Direction.EAST));
        context.setBlock(breakerTargetPos, Blocks.DIRT);
        StorageBreakerBlockEntity breaker = context.getBlockEntity(breakerPos, StorageBreakerBlockEntity.class);
        breaker.breakBelow();
        context.assertTrue(context.getLevel().getBlockState(context.absolutePos(breakerTargetPos)).isAir(),
                "Unfiltered Breaker left its target in place");
        context.assertValueEqual(controller.count(new ItemStack(Items.DIRT)), 1L, "Unfiltered Breaker stored drop");
        controller.extract(new ItemStack(Items.DIRT), 1, TransferMode.EXECUTE);

        BlockPos placerPos = new BlockPos(9, 1, 21);
        BlockPos placerTargetPos = placerPos.east();
        context.setBlock(placerPos, LumungusStorageBlocks.STORAGE_PLACER.defaultBlockState()
                .setValue(StoragePlacerBlock.FACING, Direction.EAST));
        controller.insert(new ItemStack(Items.STONE, 2), TransferMode.EXECUTE);
        StoragePlacerBlockEntity placer = context.getBlockEntity(placerPos, StoragePlacerBlockEntity.class);
        placer.placeBelow();
        context.assertTrue(context.getLevel().getBlockState(context.absolutePos(placerTargetPos)).is(Blocks.STONE),
                "Unfiltered Placer did not place an available block");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void primedWirelessModuleLinksAnIsolatedWorkBlock(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 27);
        BlockPos driveBayPos = new BlockPos(2, 1, 27);
        BlockPos outputPos = new BlockPos(12, 1, 27);
        BlockPos outputChestPos = outputPos.east();
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(driveBayPos, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(outputPos, LumungusStorageBlocks.STORAGE_OUTPUT.defaultBlockState()
                .setValue(StorageOutputBlock.FACING, Direction.EAST));
        context.setBlock(outputChestPos, Blocks.CHEST);

        DriveBayBlockEntity driveBay = context.getBlockEntity(driveBayPos, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                controllerPos,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.EMERALD, 24), TransferMode.EXECUTE);

        ItemStack module = new ItemStack(LumungusStorageItems.WIRELESS_NETWORK_MODULE);
        module.set(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER, new BoundStorageController(
                context.getLevel().dimension().identifier(),
                context.absolutePos(controllerPos),
                controller.getNetworkId()
        ));
        StorageOutputBlockEntity output = context.getBlockEntity(outputPos, StorageOutputBlockEntity.class);
        context.assertTrue(output.installWirelessModule(module), "Primed wireless module was rejected");
        output.exportOneStack();

        ChestBlockEntity chest = context.getBlockEntity(outputChestPos, ChestBlockEntity.class);
        context.assertValueEqual(chest.getItem(0).getCount(), 24, "Wireless Output item count");
        context.assertValueEqual(controller.count(new ItemStack(Items.EMERALD)), 0L, "Wireless network remainder");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void wirelessStorageControllerOpensALinkedStorageNetwork(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(WORK_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        BlockPos wirelessPos = new BlockPos(20, 1, 18);
        context.setBlock(wirelessPos, LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT);

        DriveBayBlockEntity driveBay = context.getBlockEntity(WORK_DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.DIAMOND, 12), TransferMode.EXECUTE);

        WirelessStorageControllerBlockEntity wireless = context.getBlockEntity(
                wirelessPos,
                WirelessStorageControllerBlockEntity.class
        );
        context.assertTrue(wireless.installWirelessModule(primedModule(controller)), "Network card was rejected");
        context.assertTrue(wireless.linkedController() == controller, "Wireless controller did not link");
        context.assertValueEqual(wireless.linkedController().count(new ItemStack(Items.DIAMOND)), 12L, "Wireless storage count");
        context.succeed();
    }

    @GameTest
    public void wirelessBlocksRequireAPrimedNetworkCard(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        BlockPos wirelessPos = new BlockPos(3, 1, 18);
        context.setBlock(wirelessPos, LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT);
        WirelessStorageControllerBlockEntity wireless = context.getBlockEntity(
                wirelessPos,
                WirelessStorageControllerBlockEntity.class
        );

        context.assertTrue(!wireless.refreshControllerLink(), "Cardless wireless controller linked automatically");
        context.assertTrue(
                !wireless.installWirelessModule(new ItemStack(LumungusStorageItems.WIRELESS_NETWORK_MODULE)),
                "Unprimed network card was accepted"
        );
        context.succeed();
    }

    @GameTest
    public void wirelessModuleMenuShiftMovesPrimedCard(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        BlockPos wirelessPos = new BlockPos(3, 1, 18);
        context.setBlock(wirelessPos, LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        WirelessStorageControllerBlockEntity wireless = context.getBlockEntity(
                wirelessPos,
                WirelessStorageControllerBlockEntity.class
        );
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        player.getInventory().setItem(9, primedModule(controller));
        WirelessModuleMenu menu = new WirelessModuleMenu(
                7,
                player.getInventory(),
                wireless,
                wireless
        );

        int playerCardSlot = -1;
        for (int index = WirelessModuleMenu.PLAYER_INVENTORY_SLOT_START; index < menu.slots.size(); index++) {
            if (menu.slots.get(index).hasItem()) {
                playerCardSlot = index;
                break;
            }
        }
        context.assertTrue(playerCardSlot >= 0, "Player network card slot not found");
        menu.quickMoveStack(player, playerCardSlot);
        context.assertTrue(!wireless.wirelessModule().isEmpty(), "Shift-click did not install network card");
        context.assertTrue(wireless.refreshControllerLink(), "Installed card did not resolve storage");

        menu.quickMoveStack(player, WirelessModuleMenu.MODULE_SLOT);
        context.assertTrue(wireless.wirelessModule().isEmpty(), "Shift-click did not remove network card");
        context.assertTrue(!wireless.refreshControllerLink(), "Removed card left wireless controller linked");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void portableStorageInterfaceCanUseControllerAsTerminalAnchor(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(WORK_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);

        DriveBayBlockEntity driveBay = context.getBlockEntity(WORK_DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.COPPER_INGOT, 42), TransferMode.EXECUTE);

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockPos controllerPos = context.absolutePos(WORK_CONTROLLER);
        LumungusCraftingMenu menu = new LumungusCraftingMenu(
                6,
                player.getInventory(),
                ContainerLevelAccess.create(context.getLevel(), controllerPos),
                controllerPos
        );

        menu.handleTerminalAction(new TerminalActionPayload(
                6,
                TerminalActionPayload.Action.EXTRACT_STACK_TO_CURSOR,
                new ItemStack(Items.COPPER_INGOT)
        ));

        context.assertValueEqual(menu.getCarried().getCount(), 42, "Portable interface carried amount");
        context.assertValueEqual(controller.count(new ItemStack(Items.COPPER_INGOT)), 0L, "Portable interface network amount");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void boundPortableInterfaceNeverFallsBackToAnotherNetwork(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 24);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                controllerPos,
                StorageControllerBlockEntity.class
        );

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        ItemStack portable = new ItemStack(LumungusStorageItems.PORTABLE_STORAGE_INTERFACE_SHORT);
        portable.set(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER, new BoundStorageController(
                context.getLevel().dimension().identifier(),
                context.absolutePos(controllerPos),
                UUID.randomUUID()
        ));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, portable);

        LumungusStorageItems.PORTABLE_STORAGE_INTERFACE_SHORT.use(
                context.getLevel(),
                player,
                net.minecraft.world.InteractionHand.MAIN_HAND
        );

        context.assertTrue(
                !(player.containerMenu instanceof LumungusCraftingMenu),
                "Invalid binding silently opened a different nearby storage network"
        );
        context.assertTrue(controller != null, "Nearby controller missing");
        context.succeed();
    }

    @GameTest
    public void unboundPortableInterfaceDoesNotScanForNearbyNetworks(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(LumungusStorageItems.PORTABLE_STORAGE_INTERFACE_MULTIDIMENSIONAL)
        );

        LumungusStorageItems.PORTABLE_STORAGE_INTERFACE_MULTIDIMENSIONAL.use(
                context.getLevel(),
                player,
                net.minecraft.world.InteractionHand.MAIN_HAND
        );

        context.assertTrue(
                !(player.containerMenu instanceof LumungusCraftingMenu),
                "Unbound portable interface opened a nearby storage network"
        );
        context.succeed();
    }

    @GameTest(padding = 32)
    public void wirelessInventoryConnectorLinksRemoteChestThroughWirelessController(GameTestHelper context) {
        context.setBlock(WORK_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        BlockPos wirelessControllerPos = new BlockPos(8, 1, 18);
        BlockPos wirelessConnectorPos = new BlockPos(14, 1, 18);
        BlockPos chestPos = new BlockPos(15, 1, 18);
        context.setBlock(wirelessControllerPos, LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT);
        context.setBlock(wirelessConnectorPos, LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_SHORT);
        context.setBlock(chestPos, Blocks.CHEST);

        WirelessStorageControllerBlockEntity wirelessController = context.getBlockEntity(
                wirelessControllerPos,
                WirelessStorageControllerBlockEntity.class
        );
        WirelessInventoryConnectorBlockEntity wirelessConnector = context.getBlockEntity(
                wirelessConnectorPos,
                WirelessInventoryConnectorBlockEntity.class
        );
        StorageControllerBlockEntity controller = context.getBlockEntity(
                WORK_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        ChestBlockEntity chest = context.getBlockEntity(chestPos, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.EMERALD, 18));

        context.assertTrue(wirelessController.installWirelessModule(primedModule(controller)), "Controller card rejected");
        context.assertTrue(wirelessConnector.installWirelessModule(primedModule(controller)), "Connector card rejected");
        context.assertTrue(wirelessController.refreshControllerLink(), "Wireless controller did not link");
        context.assertTrue(wirelessConnector.refreshControllerLink(), "Wireless inventory connector did not link");
        context.assertValueEqual(wirelessConnector.endpoints().size(), 1, "Wireless inventory endpoints");
        context.assertValueEqual(controller.count(new ItemStack(Items.EMERALD)), 18L, "Wireless chest storage count");

        ItemStack remainder = controller.insert(new ItemStack(Items.EMERALD, 5), TransferMode.EXECUTE);
        context.assertTrue(remainder.isEmpty(), "Wireless chest insert remainder");
        context.assertValueEqual(controller.count(new ItemStack(Items.EMERALD)), 23L, "Wireless chest post-insert count");
        context.succeed();
    }

    @GameTest(padding = 32, maxTicks = 60)
    public void wiredInventoryConnectorAutoSendsOnlyItsInventoryToDriveBays(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 24);
        BlockPos driveBayPos = new BlockPos(2, 1, 24);
        BlockPos connectorPos = new BlockPos(3, 1, 24);
        BlockPos chestPos = new BlockPos(4, 1, 24);
        BlockPos otherConnectorPos = new BlockPos(2, 1, 25);
        BlockPos otherChestPos = new BlockPos(2, 1, 26);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(driveBayPos, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(connectorPos, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(chestPos, Blocks.CHEST);
        context.setBlock(otherConnectorPos, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(otherChestPos, Blocks.CHEST);

        DriveBayBlockEntity driveBay = context.getBlockEntity(driveBayPos, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        InventoryConnectorBlockEntity connector = context.getBlockEntity(
                connectorPos,
                InventoryConnectorBlockEntity.class
        );
        ChestBlockEntity chest = context.getBlockEntity(chestPos, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.COPPER_INGOT, 48));
        ChestBlockEntity otherChest = context.getBlockEntity(otherChestPos, ChestBlockEntity.class);
        otherChest.setItem(0, new ItemStack(Items.DIAMOND, 12));

        context.assertTrue(!connector.autoSendToDriveBays(), "Wired auto-send was enabled by default");
        context.assertTrue(connector.toggleAutoSendToDriveBays(), "Wired auto-send did not enable");
        context.succeedWhen(() -> {
            context.assertTrue(chest.isEmpty(), "Wired tick auto-send left items in its chest");
            context.assertValueEqual(driveBay.count(new ItemStack(Items.COPPER_INGOT)), 48L, "Wired Drive Bay count");
            context.assertValueEqual(otherChest.getItem(0).getCount(), 12, "Auto-send emptied another connector");
            context.assertTrue(!connector.toggleAutoSendToDriveBays(), "Wired auto-send did not disable");
        });
    }

    @GameTest(padding = 32, maxTicks = 60)
    public void wirelessInventoryConnectorAutoSendsItsInventoryToDriveBays(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 30);
        BlockPos driveBayPos = new BlockPos(2, 1, 30);
        BlockPos wirelessControllerPos = new BlockPos(8, 1, 30);
        BlockPos wirelessConnectorPos = new BlockPos(14, 1, 30);
        BlockPos chestPos = new BlockPos(15, 1, 30);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(driveBayPos, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(wirelessControllerPos, LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT);
        context.setBlock(wirelessConnectorPos, LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_SHORT);
        context.setBlock(chestPos, Blocks.CHEST);

        DriveBayBlockEntity driveBay = context.getBlockEntity(driveBayPos, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        WirelessStorageControllerBlockEntity wirelessController = context.getBlockEntity(
                wirelessControllerPos,
                WirelessStorageControllerBlockEntity.class
        );
        WirelessInventoryConnectorBlockEntity connector = context.getBlockEntity(
                wirelessConnectorPos,
                WirelessInventoryConnectorBlockEntity.class
        );
        StorageControllerBlockEntity controller = context.getBlockEntity(controllerPos, StorageControllerBlockEntity.class);
        ChestBlockEntity chest = context.getBlockEntity(chestPos, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.AMETHYST_SHARD, 27));

        context.assertTrue(wirelessController.installWirelessModule(primedModule(controller)), "Controller card rejected");
        context.assertTrue(connector.installWirelessModule(primedModule(controller)), "Connector card rejected");
        context.assertTrue(wirelessController.refreshControllerLink(), "Wireless auto-send controller did not link");
        context.assertTrue(connector.refreshControllerLink(), "Wireless auto-send connector did not link");
        context.assertTrue(!connector.autoSendToDriveBays(), "Wireless auto-send was enabled by default");
        context.assertTrue(connector.toggleAutoSendToDriveBays(), "Wireless auto-send did not enable");
        context.succeedWhen(() -> {
            context.assertTrue(
                    connector.autoSendAttempts() > 0,
                    "Wireless connector did not attempt auto-send after enabling"
            );
            context.assertTrue(chest.isEmpty(), "Wireless tick auto-send left items in its chest");
            context.assertValueEqual(driveBay.count(new ItemStack(Items.AMETHYST_SHARD)), 27L, "Wireless Drive Bay count");
        });
    }

    @GameTest
    public void multidimensionalWirelessInventoryConnectorRefreshIsBounded(GameTestHelper context) {
        BlockPos connectorPos = new BlockPos(1, 1, 1);
        context.setBlock(connectorPos, LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_MULTIDIMENSIONAL);

        WirelessInventoryConnectorBlockEntity connector = context.getBlockEntity(
                connectorPos,
                WirelessInventoryConnectorBlockEntity.class
        );

        long startedAt = System.nanoTime();
        connector.refreshControllerLink();
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;

        context.assertTrue(
                elapsedMillis < 2_000L,
                "Wireless connector refresh exceeded 2 seconds: " + elapsedMillis + " ms"
        );
        context.succeed();
    }

    @GameTest
    public void multidimensionalWirelessControllerUsesBoundOverworldStorageFromNether(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 1);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                controllerPos,
                StorageControllerBlockEntity.class
        );

        ServerLevel nether = context.getLevel().getServer().getLevel(Level.NETHER);
        context.assertTrue(nether != null, "Nether level was unavailable");
        BlockPos testOrigin = context.absolutePos(new BlockPos(3, 1, 1));
        BlockPos wirelessPos = new BlockPos(testOrigin.getX(), 64, testOrigin.getZ());
        nether.getChunkAt(wirelessPos);
        nether.setBlockAndUpdate(
                wirelessPos,
                LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL.defaultBlockState()
        );
        context.assertTrue(
                nether.getBlockEntity(wirelessPos) instanceof WirelessStorageControllerBlockEntity,
                "Nether wireless controller block entity was unavailable"
        );
        WirelessStorageControllerBlockEntity wireless = (WirelessStorageControllerBlockEntity) nether.getBlockEntity(
                wirelessPos
        );
        context.assertTrue(wireless.installWirelessModule(primedModule(controller)), "Nether controller rejected network card");
        context.assertTrue(wireless.linkedController() == controller, "Nether wireless controller did not resolve Overworld storage");
        nether.setBlockAndUpdate(wirelessPos, Blocks.AIR.defaultBlockState());
        context.succeed();
    }

    @GameTest
    public void multidimensionalWirelessControllerUsesBoundNetherStorageFromOverworld(GameTestHelper context) {
        ServerLevel nether = context.getLevel().getServer().getLevel(Level.NETHER);
        context.assertTrue(nether != null, "Nether level was unavailable");
        BlockPos testOrigin = context.absolutePos(new BlockPos(1, 1, 1));
        BlockPos controllerPos = new BlockPos(testOrigin.getX(), 64, testOrigin.getZ());
        nether.getChunkAt(controllerPos);
        nether.setBlockAndUpdate(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER.defaultBlockState());
        context.assertTrue(
                nether.getBlockEntity(controllerPos) instanceof StorageControllerBlockEntity,
                "Nether storage controller block entity was unavailable"
        );
        StorageControllerBlockEntity controller = (StorageControllerBlockEntity) nether.getBlockEntity(controllerPos);

        BlockPos wirelessPos = new BlockPos(3, 1, 1);
        context.setBlock(wirelessPos, LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL);
        WirelessStorageControllerBlockEntity wireless = context.getBlockEntity(
                wirelessPos,
                WirelessStorageControllerBlockEntity.class
        );
        context.assertTrue(wireless.installWirelessModule(primedModule(controller)), "Overworld controller rejected network card");
        context.assertTrue(wireless.refreshControllerLink(), "Overworld wireless controller did not resolve its card");
        context.assertTrue(
                wireless.linkedController() == controller,
                "Overworld wireless controller did not resolve Nether storage"
        );
        nether.setBlockAndUpdate(controllerPos, Blocks.AIR.defaultBlockState());
        context.succeed();
    }

    @GameTest(padding = 128)
    public void dimensionWirelessInventoryConnectorIgnoresShortRangeLimit(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 64);
        BlockPos wirelessControllerPos = new BlockPos(60, 1, 64);
        BlockPos wirelessConnectorPos = new BlockPos(120, 1, 64);
        BlockPos chestPos = new BlockPos(121, 1, 64);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(wirelessControllerPos, LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_DIMENSION);
        context.setBlock(wirelessConnectorPos, LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_DIMENSION);
        context.setBlock(chestPos, Blocks.CHEST);

        StorageControllerBlockEntity controller = context.getBlockEntity(
                controllerPos,
                StorageControllerBlockEntity.class
        );
        WirelessStorageControllerBlockEntity wirelessController = context.getBlockEntity(
                wirelessControllerPos,
                WirelessStorageControllerBlockEntity.class
        );
        WirelessInventoryConnectorBlockEntity wirelessConnector = context.getBlockEntity(
                wirelessConnectorPos,
                WirelessInventoryConnectorBlockEntity.class
        );
        ChestBlockEntity chest = context.getBlockEntity(chestPos, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.AMETHYST_SHARD, 27));

        context.assertTrue(wirelessController.installWirelessModule(primedModule(controller)), "Controller card rejected");
        context.assertTrue(wirelessConnector.installWirelessModule(primedModule(controller)), "Connector card rejected");
        context.assertTrue(wirelessController.refreshControllerLink(), "Dimension wireless controller did not link");
        context.assertTrue(wirelessConnector.refreshControllerLink(), "Dimension wireless connector did not link");
        context.assertValueEqual(controller.count(new ItemStack(Items.AMETHYST_SHARD)), 27L, "Dimension wireless inventory count");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void topologyCacheInvalidatesOnlyTheChangedComponent(GameTestHelper context) {
        BlockPos firstController = new BlockPos(1, 1, 13);
        BlockPos firstStart = new BlockPos(2, 1, 13);
        BlockPos secondController = new BlockPos(8, 1, 13);
        BlockPos secondStart = new BlockPos(9, 1, 13);
        context.setBlock(firstController, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(secondController, LumungusStorageBlocks.STORAGE_CONTROLLER);
        for (int offset = 0; offset < 3; offset++) {
            context.setBlock(firstStart.offset(offset, 0, 0), LumungusStorageBlocks.INVENTORY_CABLE);
            context.setBlock(secondStart.offset(offset, 0, 0), LumungusStorageBlocks.INVENTORY_CABLE);
        }
        context.setBlock(firstStart.offset(3, 0, 0), LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(secondStart.offset(3, 0, 0), LumungusStorageBlocks.INVENTORY_CONNECTOR);

        StorageNetworkTopology.invalidate(context.getLevel());
        BlockPos absoluteFirst = context.absolutePos(firstController);
        BlockPos absoluteSecond = context.absolutePos(secondController);
        StorageNetworkTopology.connectedNodes(context.getLevel(), absoluteFirst);
        StorageNetworkTopology.connectedNodes(context.getLevel(), absoluteSecond);
        context.assertValueEqual(
                StorageNetworkTopology.cacheStats(context.getLevel()).cachedNodes(),
                10,
                "Two cached storage components"
        );

        context.setBlock(firstStart.offset(1, 0, 0), Blocks.AIR);
        context.assertValueEqual(
                StorageNetworkTopology.cacheStats(context.getLevel()).cachedNodes(),
                5,
                "Unaffected cached component"
        );
        long hitsBefore = StorageNetworkTopology.cacheStats(context.getLevel()).hits();
        StorageNetworkTopology.connectedNodes(context.getLevel(), absoluteSecond);
        context.assertValueEqual(
                StorageNetworkTopology.cacheStats(context.getLevel()).hits(),
                hitsBefore + 1,
                "Unaffected component cache hit"
        );
        StorageNetworkTopology.connectedNodes(context.getLevel(), absoluteFirst);
        context.assertValueEqual(
                StorageNetworkTopology.cacheStats(context.getLevel()).cachedNodes(),
                7,
                "Rebuilt changed component"
        );
        context.succeed();
    }

    @GameTest(padding = 32)
    public void cableNetworkCrossesALoadedChunkBoundary(GameTestHelper context) {
        BlockPos anchor = context.absolutePos(new BlockPos(8, 1, 8));
        int boundaryX = Math.floorDiv(anchor.getX(), 16) * 16 + 16;
        BlockPos controllerPos = new BlockPos(boundaryX - 3, anchor.getY(), anchor.getZ());
        BlockPos connectorPos = new BlockPos(boundaryX + 2, anchor.getY(), anchor.getZ());
        BlockPos chestPos = new BlockPos(boundaryX + 3, anchor.getY(), anchor.getZ());
        context.assertTrue(
                context.getLevel().isLoaded(controllerPos) && context.getLevel().isLoaded(chestPos),
                "Both chunk-boundary test sides must be loaded"
        );

        context.getLevel().setBlockAndUpdate(
                controllerPos,
                LumungusStorageBlocks.STORAGE_CONTROLLER.defaultBlockState()
        );
        for (int x = boundaryX - 2; x <= boundaryX + 1; x++) {
            BlockPos cablePos = new BlockPos(x, anchor.getY(), anchor.getZ());
            context.getLevel().setBlockAndUpdate(
                    cablePos,
                    LumungusStorageBlocks.INVENTORY_CABLE.defaultBlockState()
            );
        }
        context.getLevel().setBlockAndUpdate(
                connectorPos,
                LumungusStorageBlocks.INVENTORY_CONNECTOR.defaultBlockState()
        );
        context.getLevel().setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());

        context.assertTrue(
                !new ChunkPos(controllerPos.getX() >> 4, controllerPos.getZ() >> 4).equals(
                        new ChunkPos(connectorPos.getX() >> 4, connectorPos.getZ() >> 4)
                ),
                "Controller and connector unexpectedly share one chunk"
        );
        ChestBlockEntity chest = (ChestBlockEntity) context.getLevel().getBlockEntity(chestPos);
        StorageControllerBlockEntity controller =
                (StorageControllerBlockEntity) context.getLevel().getBlockEntity(controllerPos);
        InventoryConnectorBlockEntity connector =
                (InventoryConnectorBlockEntity) context.getLevel().getBlockEntity(connectorPos);
        context.assertTrue(chest != null && controller != null && connector != null, "Chunk-boundary blocks missing");
        chest.setItem(0, new ItemStack(Items.REDSTONE, 23));

        context.assertTrue(connector.refreshControllerLink(), "Connector did not link across the chunk boundary");
        context.assertValueEqual(controller.count(new ItemStack(Items.REDSTONE)), 23L, "Cross-chunk item count");
        context.succeed();
    }

    @GameTest(padding = 8)
    public void pneumaticPipeConnectsOnlyToStorageNodes(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 1);
        BlockPos pipePos = new BlockPos(2, 1, 1);
        BlockPos connectorPos = new BlockPos(3, 1, 1);
        BlockPos chestPos = new BlockPos(2, 1, 2);

        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(pipePos, LumungusStorageBlocks.PNEUMATIC_PIPE);
        context.setBlock(connectorPos, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(chestPos, Blocks.CHEST);

        BlockState pipeState = context.getLevel().getBlockState(context.absolutePos(pipePos));
        context.assertTrue(pipeState.getValue(InventoryCableBlock.WEST), "Pipe did not connect to controller");
        context.assertTrue(pipeState.getValue(InventoryCableBlock.EAST), "Pipe did not connect to connector");
        context.assertTrue(!pipeState.getValue(InventoryCableBlock.SOUTH), "Pipe connected to a normal chest");
        context.assertTrue(!pipeState.getValue(InventoryCableBlock.NORTH), "Pipe connected to air");
        context.assertTrue(!pipeState.getValue(InventoryCableBlock.UP), "Pipe connected upward without a storage node");
        context.assertTrue(!pipeState.getValue(InventoryCableBlock.DOWN), "Pipe connected downward into the floor");

        Set<BlockPos> nodes = StorageNetworkTopology.connectedNodes(context.getLevel(), context.absolutePos(controllerPos));
        context.assertTrue(nodes.contains(context.absolutePos(connectorPos)), "Pipe network did not reach connector");
        context.assertTrue(!nodes.contains(context.absolutePos(chestPos)), "Pipe network included a normal chest");
        context.succeed();
    }

    @GameTest(padding = 8)
    public void pneumaticPipeIgnoresDeadSideBranches(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 3);
        BlockPos pipePos = new BlockPos(2, 1, 3);
        BlockPos connectorPos = new BlockPos(3, 1, 3);
        BlockPos deadBranchPos = new BlockPos(2, 1, 4);

        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(pipePos, LumungusStorageBlocks.PNEUMATIC_PIPE);
        context.setBlock(connectorPos, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(deadBranchPos, LumungusStorageBlocks.PNEUMATIC_PIPE);

        BlockState pipeState = context.getLevel().getBlockState(context.absolutePos(pipePos));
        context.assertTrue(pipeState.getValue(InventoryCableBlock.WEST), "Pipe did not connect to controller");
        context.assertTrue(pipeState.getValue(InventoryCableBlock.EAST), "Pipe did not connect to connector");
        context.assertTrue(!pipeState.getValue(InventoryCableBlock.SOUTH), "Pipe connected to a dead side branch");
        context.assertTrue(!pipeState.getValue(InventoryCableBlock.NORTH), "Pipe connected to air");

        Set<BlockPos> nodes = StorageNetworkTopology.connectedNodes(context.getLevel(), context.absolutePos(controllerPos));
        context.assertTrue(nodes.contains(context.absolutePos(connectorPos)), "Main pipe network did not reach connector");
        context.assertTrue(!nodes.contains(context.absolutePos(deadBranchPos)), "Pipe network included a dead side branch");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void cableLinksDistantDriveBayAndCraftingTerminal(GameTestHelper context) {
        context.setBlock(DEVICE_CABLE_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        for (int x = 2; x <= 10; x++) {
            context.setBlock(new BlockPos(x, 1, 9), LumungusStorageBlocks.INVENTORY_CABLE);
        }
        context.setBlock(DISTANT_DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(DISTANT_TERMINAL, LumungusStorageBlocks.CRAFTING_TERMINAL);

        StorageControllerBlockEntity controller = context.getBlockEntity(
                DEVICE_CABLE_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        DriveBayBlockEntity driveBay = context.getBlockEntity(DISTANT_DRIVE_BAY, DriveBayBlockEntity.class);
        CraftingTerminalBlockEntity terminal = context.getBlockEntity(
                DISTANT_TERMINAL,
                CraftingTerminalBlockEntity.class
        );
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);

        context.assertTrue(driveBay.refreshControllerLink(), "Cable did not link the distant Drive Bay");
        context.assertTrue(terminal.refreshControllerLink(), "Cable did not link the distant terminal");
        context.assertTrue(terminal.linkedController() == controller, "Terminal linked to the wrong controller");
        context.assertTrue(
                controller.insert(new ItemStack(Items.GOLD_INGOT, 12), TransferMode.EXECUTE).isEmpty(),
                "Controller could not insert into the distant Drive Bay"
        );
        context.assertValueEqual(controller.count(new ItemStack(Items.GOLD_INGOT)), 12L, "Distant Drive Bay count");

        context.setBlock(new BlockPos(6, 1, 9), Blocks.AIR);
        context.assertTrue(!driveBay.refreshControllerLink(), "Broken cable left the Drive Bay linked");
        context.assertTrue(!terminal.refreshControllerLink(), "Broken cable left the terminal linked");
        context.assertValueEqual(controller.count(new ItemStack(Items.GOLD_INGOT)), 0L, "Disconnected Drive Bay count");
        context.assertValueEqual(driveBay.count(new ItemStack(Items.GOLD_INGOT)), 12L, "Drive Bay contents after split");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void cableConnectsAndDisconnectsADistantPhysicalInventory(GameTestHelper context) {
        context.setBlock(CABLE_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        for (int x = 2; x <= 10; x++) {
            context.setBlock(new BlockPos(x, 1, 6), LumungusStorageBlocks.INVENTORY_CABLE);
        }
        context.setBlock(DISTANT_CONNECTOR, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(DISTANT_CHEST, Blocks.CHEST);

        ChestBlockEntity chest = context.getBlockEntity(DISTANT_CHEST, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.IRON_INGOT, 37));
        StorageControllerBlockEntity controller = context.getBlockEntity(
                CABLE_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        InventoryConnectorBlockEntity connector = context.getBlockEntity(
                DISTANT_CONNECTOR,
                InventoryConnectorBlockEntity.class
        );

        StorageNetworkTopology.invalidate(context.getLevel());
        BlockPos absoluteControllerPos = context.absolutePos(CABLE_CONTROLLER);
        StorageNetworkTopology.connectedNodes(context.getLevel(), absoluteControllerPos);
        StorageNetworkTopology.connectedNodes(context.getLevel(), absoluteControllerPos);
        StorageNetworkTopology.CacheStats warmCache = StorageNetworkTopology.cacheStats(context.getLevel());
        context.assertValueEqual(warmCache.misses(), 1L, "Topology cache misses");
        context.assertValueEqual(warmCache.hits(), 1L, "Topology cache hits");
        context.assertValueEqual(warmCache.cachedNodes(), 11, "Cached topology nodes");

        context.assertTrue(connector.refreshControllerLink(), "Cable did not link the distant connector");
        context.assertValueEqual(controller.count(new ItemStack(Items.IRON_INGOT)), 37L, "Cable inventory count");

        context.setBlock(new BlockPos(6, 1, 6), Blocks.AIR);
        context.assertTrue(!connector.refreshControllerLink(), "Broken cable left the distant connector linked");
        context.assertValueEqual(controller.count(new ItemStack(Items.IRON_INGOT)), 0L, "Disconnected inventory count");
        context.assertValueEqual(chest.getItem(0).getCount(), 37, "Disconnected chest contents");

        context.setBlock(new BlockPos(6, 1, 6), LumungusStorageBlocks.INVENTORY_CABLE);
        context.assertTrue(connector.refreshControllerLink(), "Restored cable did not merge the network");
        context.assertValueEqual(controller.count(new ItemStack(Items.IRON_INGOT)), 37L, "Reconnected inventory count");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void controllerUsesPhysicalChestWithoutCellsAndDeduplicatesConnectors(GameTestHelper context) {
        context.setBlock(PHYSICAL_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(FIRST_INVENTORY_CONNECTOR, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(CONNECTED_CHEST, Blocks.CHEST);
        context.setBlock(SECOND_INVENTORY_CONNECTOR, LumungusStorageBlocks.INVENTORY_CONNECTOR);

        ChestBlockEntity chest = context.getBlockEntity(CONNECTED_CHEST, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 48));
        StorageControllerBlockEntity controller = context.getBlockEntity(
                PHYSICAL_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        InventoryConnectorBlockEntity firstConnector = context.getBlockEntity(
                FIRST_INVENTORY_CONNECTOR,
                InventoryConnectorBlockEntity.class
        );
        InventoryConnectorBlockEntity secondConnector = context.getBlockEntity(
                SECOND_INVENTORY_CONNECTOR,
                InventoryConnectorBlockEntity.class
        );

        context.assertTrue(firstConnector.refreshControllerLink(), "First connector did not find the controller");
        context.assertTrue(secondConnector.refreshControllerLink(), "Second connector did not find the controller");
        context.assertValueEqual(controller.count(new ItemStack(Items.COBBLESTONE)), 48L, "Deduplicated chest count");

        ItemStack insertRemainder = controller.insert(
                new ItemStack(Items.COBBLESTONE, 16),
                TransferMode.EXECUTE
        );
        context.assertTrue(insertRemainder.isEmpty(), "Controller did not insert into the physical chest");
        context.assertValueEqual(controller.count(new ItemStack(Items.COBBLESTONE)), 64L, "Post-insert count");

        ItemStack extracted = controller.extract(
                new ItemStack(Items.COBBLESTONE),
                20,
                TransferMode.EXECUTE
        );
        context.assertValueEqual(extracted.getCount(), 20, "Physical extraction");
        context.assertValueEqual(controller.count(new ItemStack(Items.COBBLESTONE)), 44L, "Post-extract count");
        context.assertTrue(!chest.isEmpty(), "Physical chest was unexpectedly emptied");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void controllerMovesPhysicalInventoryContentsIntoDriveBays(GameTestHelper context) {
        context.setBlock(PHYSICAL_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(new BlockPos(4, 1, 3), LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(FIRST_INVENTORY_CONNECTOR, LumungusStorageBlocks.INVENTORY_CONNECTOR);
        context.setBlock(CONNECTED_CHEST, Blocks.CHEST);

        ChestBlockEntity chest = context.getBlockEntity(CONNECTED_CHEST, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
        chest.setItem(1, new ItemStack(Items.DIAMOND, 3));

        StorageControllerBlockEntity controller = context.getBlockEntity(
                PHYSICAL_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        DriveBayBlockEntity driveBay = context.getBlockEntity(new BlockPos(4, 1, 3), DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);

        StorageControllerBlockEntity.BayMoveResult result = controller.movePhysicalInventoriesIntoDriveBays();

        context.assertValueEqual(result.movedItems(), 67L, "Moved items");
        context.assertValueEqual(driveBay.count(new ItemStack(Items.COBBLESTONE)), 64L, "Drive Bay cobblestone");
        context.assertValueEqual(driveBay.count(new ItemStack(Items.DIAMOND)), 3L, "Drive Bay diamonds");
        context.assertTrue(chest.isEmpty(), "Source chest was not emptied");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void physicalInventoryAdapterIsTransactionalAndComponentSafe(GameTestHelper context) {
        context.setBlock(PHYSICAL_CHEST, Blocks.CHEST);
        ChestBlockEntity chest = context.getBlockEntity(PHYSICAL_CHEST, ChestBlockEntity.class);
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 32));
        chest.setItem(1, new ItemStack(Items.COBBLESTONE, 16));
        ItemStack namedCobblestone = new ItemStack(Items.COBBLESTONE, 3);
        namedCobblestone.set(DataComponents.CUSTOM_NAME, Component.literal("Reserved"));
        chest.setItem(2, namedCobblestone);

        BlockPos chestPos = context.absolutePos(PHYSICAL_CHEST);
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(context.getLevel(), chestPos, Direction.UP);
        context.assertTrue(storage != null, "Fabric item storage was not exposed by the physical chest");
        FabricItemStorageAccess access = new FabricItemStorageAccess(storage);

        context.assertValueEqual(access.count(new ItemStack(Items.COBBLESTONE)), 48L, "Plain cobblestone");
        context.assertValueEqual(access.count(namedCobblestone), 3L, "Named cobblestone");
        context.assertValueEqual(access.storedResources().size(), 2, "Distinct physical resources");
        context.assertValueEqual(access.snapshot().storedTotalAmount(), 51L, "Physical item total");

        ItemStack simulatedRemainder = access.insert(
                new ItemStack(Items.OAK_LOG, 10),
                TransferMode.SIMULATE
        );
        context.assertTrue(simulatedRemainder.isEmpty(), "Simulated insertion unexpectedly failed");
        context.assertValueEqual(access.count(new ItemStack(Items.OAK_LOG)), 0L, "Simulated oak logs");

        ItemStack insertedRemainder = access.insert(
                new ItemStack(Items.OAK_LOG, 10),
                TransferMode.EXECUTE
        );
        context.assertTrue(insertedRemainder.isEmpty(), "Executed insertion unexpectedly failed");
        context.assertValueEqual(access.count(new ItemStack(Items.OAK_LOG)), 10L, "Inserted oak logs");

        ItemStack simulatedExtraction = access.extract(
                new ItemStack(Items.COBBLESTONE),
                40,
                TransferMode.SIMULATE
        );
        context.assertValueEqual(simulatedExtraction.getCount(), 40, "Simulated extraction");
        context.assertValueEqual(access.count(new ItemStack(Items.COBBLESTONE)), 48L, "Post-simulation count");

        ItemStack extracted = access.extract(
                new ItemStack(Items.COBBLESTONE),
                40,
                TransferMode.EXECUTE
        );
        context.assertValueEqual(extracted.getCount(), 40, "Executed extraction");
        context.assertValueEqual(access.count(new ItemStack(Items.COBBLESTONE)), 8L, "Remaining cobblestone");
        context.assertValueEqual(access.count(namedCobblestone), 3L, "Preserved named cobblestone");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void driveBayOwnershipIsStableAndLossless(GameTestHelper context) {
        context.setBlock(FIRST_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);

        StorageControllerBlockEntity first = context.getBlockEntity(
                FIRST_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        context.assertTrue(driveBay.refreshControllerLink(), "Drive Bay did not find its first controller");
        context.assertTrue(driveBay.isLinkedTo(first), "Drive Bay linked to the wrong first controller");

        ItemStack cellRemainder = driveBay.insertCell(
                new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                TransferMode.EXECUTE
        );
        context.assertTrue(cellRemainder.isEmpty(), "Storage Cell was not inserted");
        ItemStack itemRemainder = first.insert(new ItemStack(Items.COBBLESTONE, 16), TransferMode.EXECUTE);
        context.assertTrue(itemRemainder.isEmpty(), "Items were not inserted through the first controller");
        context.assertValueEqual(first.count(new ItemStack(Items.COBBLESTONE)), 16L, "First network count");

        context.setBlock(SECOND_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        StorageControllerBlockEntity second = context.getBlockEntity(
                SECOND_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        context.assertTrue(driveBay.refreshControllerLink(), "Drive Bay lost its valid first controller");
        context.assertTrue(driveBay.isLinkedTo(first), "A closer controller stole an already owned Drive Bay");
        context.assertValueEqual(second.count(new ItemStack(Items.COBBLESTONE)), 0L, "Second network count");

        context.setBlock(FIRST_CONTROLLER, Blocks.AIR);
        context.assertTrue(driveBay.refreshControllerLink(), "Drive Bay did not recover after controller removal");
        context.assertTrue(driveBay.isLinkedTo(second), "Drive Bay did not move to the remaining controller");
        context.assertValueEqual(second.count(new ItemStack(Items.COBBLESTONE)), 16L, "Recovered network count");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void driveBayAcceptsEightStorageCells(GameTestHelper context) {
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);

        for (int index = 0; index < DriveBayBlockEntity.CELL_SLOTS; index++) {
            ItemStack remainder = driveBay.insertCell(
                    new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                    TransferMode.EXECUTE
            );
            context.assertTrue(remainder.isEmpty(), "Cell " + index + " was not inserted");
        }

        ItemStack rejected = driveBay.insertCell(
                new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                TransferMode.EXECUTE
        );
        context.assertValueEqual(rejected.getCount(), 1, "Ninth Cell should be rejected");
        context.assertValueEqual(driveBay.cellCount(), DriveBayBlockEntity.CELL_SLOTS, "Filled Cell slots");
        context.assertValueEqual(driveBay.freeCellSlots(), 0, "Free Cell slots");
        context.assertValueEqual(
                driveBay.capacity().maxTotalAmount(),
                8L * 16_384L,
                "Drive Bay total capacity"
        );

        ItemStack insertedRemainder = driveBay.insert(new ItemStack(Items.COBBLESTONE, 64), TransferMode.EXECUTE);
        context.assertTrue(insertedRemainder.isEmpty(), "Multi-Cell insert failed");
        ItemStack extracted = driveBay.extract(new ItemStack(Items.COBBLESTONE), 64, TransferMode.EXECUTE);
        context.assertValueEqual(extracted.getCount(), 64, "Multi-Cell extract");
        context.assertValueEqual(driveBay.removeCell(TransferMode.EXECUTE).getCount(), 1, "Removed Cell");
        context.assertValueEqual(driveBay.cellCount(), DriveBayBlockEntity.CELL_SLOTS - 1, "Cell count after removal");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void driveBayMenuShiftMovesStorageCells(GameTestHelper context) {
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        player.getInventory().setItem(9, new ItemStack(LumungusStorageItems.STORAGE_CELL_16K));

        BlockPos driveBayPos = context.absolutePos(DRIVE_BAY);
        DriveBayMenu menu = new DriveBayMenu(4, player.getInventory(), driveBay, driveBayPos);
        int playerCellSlot = -1;
        for (int index = DriveBayMenu.PLAYER_INVENTORY_SLOT_START; index < menu.slots.size(); index++) {
            if (menu.slots.get(index).hasItem()) {
                playerCellSlot = index;
                break;
            }
        }

        context.assertTrue(playerCellSlot >= 0, "Player Cell slot not found");
        menu.quickMoveStack(player, playerCellSlot);
        context.assertValueEqual(driveBay.cellCount(), 1, "Shift-click did not insert a Cell");
        context.assertTrue(player.getInventory().getItem(9).isEmpty(), "Player inventory kept shifted Cell");

        menu.quickMoveStack(player, DriveBayMenu.CELL_SLOT_START);
        context.assertValueEqual(driveBay.cellCount(), 0, "Shift-click did not remove a Cell");
        context.assertTrue(
                player.getInventory().countItem(LumungusStorageItems.STORAGE_CELL_16K) == 1,
                "Player inventory did not receive the shifted Cell"
        );
        context.succeed();
    }

    @GameTest(padding = 32)
    public void controllerUnpacksFilledShulkerBoxesOnInsert(GameTestHelper context) {
        context.setBlock(FIRST_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);

        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(
                new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                TransferMode.EXECUTE
        );
        StorageControllerBlockEntity controller = context.getBlockEntity(
                FIRST_CONTROLLER,
                StorageControllerBlockEntity.class
        );

        ItemStack shulkerBox = new ItemStack(Items.SHULKER_BOX);
        shulkerBox.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(
                new ItemStack(Items.DIAMOND, 32),
                new ItemStack(Items.COBBLESTONE, 64)
        )));

        ItemStack remainder = controller.insert(shulkerBox, TransferMode.EXECUTE);

        context.assertTrue(remainder.isEmpty(), "Filled shulker was not accepted");
        context.assertValueEqual(controller.count(new ItemStack(Items.DIAMOND)), 32L, "Unpacked diamonds");
        context.assertValueEqual(controller.count(new ItemStack(Items.COBBLESTONE)), 64L, "Unpacked cobblestone");
        context.assertValueEqual(controller.count(new ItemStack(Items.SHULKER_BOX)), 1L, "Returned empty shulker");
        context.assertValueEqual(controller.count(shulkerBox), 0L, "Filled shulker should not remain stored");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void craftingTerminalPacksSelectedItemsIntoShulkerBox(GameTestHelper context) {
        context.setBlock(FIRST_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(CRAFTING_TERMINAL, LumungusStorageBlocks.CRAFTING_TERMINAL);

        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(
                new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                TransferMode.EXECUTE
        );
        StorageControllerBlockEntity controller = context.getBlockEntity(
                FIRST_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.SHULKER_BOX), TransferMode.EXECUTE);
        controller.insert(new ItemStack(Items.COBBLESTONE, 200), TransferMode.EXECUTE);

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockPos playerPos = context.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);
        BlockPos terminalPos = context.absolutePos(CRAFTING_TERMINAL);
        LumungusCraftingMenu menu = new LumungusCraftingMenu(
                5,
                player.getInventory(),
                ContainerLevelAccess.create(context.getLevel(), terminalPos),
                terminalPos
        );

        menu.handleTerminalAction(new TerminalActionPayload(
                5,
                TerminalActionPayload.Action.EXTRACT_SHULKER_TO_CURSOR,
                new ItemStack(Items.COBBLESTONE)
        ));

        ItemStack carried = menu.getCarried();
        context.assertTrue(carried.is(Items.SHULKER_BOX), "Cursor did not receive a shulker box");
        ItemContainerContents contents = carried.get(DataComponents.CONTAINER);
        int storedCobblestone = contents == null
                ? 0
                : contents.nonEmptyItemCopyStream().mapToInt(ItemStack::getCount).sum();
        context.assertValueEqual(storedCobblestone, 200, "Packed shulker cobblestone");
        context.assertValueEqual(controller.count(new ItemStack(Items.COBBLESTONE)), 0L, "Network cobblestone");
        context.assertValueEqual(controller.count(new ItemStack(Items.SHULKER_BOX)), 0L, "Consumed empty shulker");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void partialShiftCraftPreservesEveryResultItem(GameTestHelper context) {
        context.setBlock(CRAFTING_TERMINAL, LumungusStorageBlocks.CRAFTING_TERMINAL);
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockPos playerPos = context.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);

        for (int index = 0; index < 36; index++) {
            player.getInventory().setItem(index, new ItemStack(Items.COBBLESTONE, 64));
        }
        player.getInventory().setItem(0, new ItemStack(Items.OAK_PLANKS, 62));

        BlockPos terminalPos = context.absolutePos(CRAFTING_TERMINAL);
        LumungusCraftingMenu menu = new LumungusCraftingMenu(
                1,
                player.getInventory(),
                ContainerLevelAccess.create(context.getLevel(), terminalPos),
                terminalPos
        );
        menu.getInputGridSlots().getFirst().setByPlayer(new ItemStack(Items.OAK_LOG, 2));
        menu.slotsChanged(menu.getInputGridSlots().getFirst().container);

        context.assertValueEqual(menu.getResultSlot().getItem().getCount(), 4, "Initial crafting result");
        menu.quickMoveStack(player, LumungusCraftingMenu.RESULT_SLOT);

        context.assertValueEqual(player.getInventory().getItem(0).getCount(), 64, "Moved result amount");
        context.assertValueEqual(menu.getResultSlot().getItem().getCount(), 4, "Next crafting result");
        context.assertValueEqual(
                menu.getInputGridSlots().getFirst().getItem().getCount(),
                1,
                "Remaining crafting ingredient"
        );
        context.assertItemEntityCountIs(Items.OAK_PLANKS, new BlockPos(2, 2, 2), 2.0, 2);
        context.succeed();
    }

    @GameTest(padding = 32)
    public void missingJeiIngredientsLeaveTheExistingGridUntouched(GameTestHelper context) {
        context.setBlock(FIRST_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(CRAFTING_TERMINAL, LumungusStorageBlocks.CRAFTING_TERMINAL);

        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(
                new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                TransferMode.EXECUTE
        );
        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockPos playerPos = context.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);

        BlockPos terminalPos = context.absolutePos(CRAFTING_TERMINAL);
        LumungusCraftingMenu menu = new LumungusCraftingMenu(
                2,
                player.getInventory(),
                ContainerLevelAccess.create(context.getLevel(), terminalPos),
                terminalPos
        );
        menu.getInputGridSlots().getFirst().setByPlayer(new ItemStack(Items.DIAMOND));
        menu.placeRecipeFromNetwork(Identifier.parse("minecraft:oak_planks"), false);

        ItemStack unchanged = menu.getInputGridSlots().getFirst().getItem();
        context.assertTrue(unchanged.is(Items.DIAMOND), "Failed JEI transfer replaced the existing item");
        context.assertValueEqual(unchanged.getCount(), 1, "Existing grid item count");
        for (int index = 1; index < menu.getInputGridSlots().size(); index++) {
            context.assertTrue(
                    menu.getInputGridSlots().get(index).getItem().isEmpty(),
                    "Failed JEI transfer populated another grid slot"
            );
        }
        context.succeed();
    }

    @GameTest(padding = 32)
    public void craftingTerminalRecursivelyPreparesBarrelIngredientsFromLogs(GameTestHelper context) {
        context.setBlock(FIRST_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(CRAFTING_TERMINAL, LumungusStorageBlocks.CRAFTING_TERMINAL);

        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(
                new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                TransferMode.EXECUTE
        );
        StorageControllerBlockEntity controller = context.getBlockEntity(
                FIRST_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.OAK_LOG, 3), TransferMode.EXECUTE);

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockPos playerPos = context.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);
        BlockPos terminalPos = context.absolutePos(CRAFTING_TERMINAL);
        LumungusCraftingMenu menu = new LumungusCraftingMenu(
                3,
                player.getInventory(),
                ContainerLevelAccess.create(context.getLevel(), terminalPos),
                terminalPos
        );

        menu.placeRecipeFromNetwork(Identifier.parse("minecraft:barrel"), false);

        context.assertTrue(menu.getResultSlot().getItem().is(Items.BARREL), "Recursive barrel result");
        context.assertValueEqual(controller.count(new ItemStack(Items.OAK_LOG)), 0L, "Consumed oak logs");
        context.assertValueEqual(controller.count(new ItemStack(Items.OAK_PLANKS)), 3L, "Surplus oak planks");
        context.assertValueEqual(controller.count(new ItemStack(Items.OAK_SLAB)), 4L, "Surplus oak slabs");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void craftingTerminalUsesRequestedResultAmountForJeiTransfer(GameTestHelper context) {
        context.setBlock(FIRST_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(CRAFTING_TERMINAL, LumungusStorageBlocks.CRAFTING_TERMINAL);

        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                FIRST_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.OAK_LOG, 3), TransferMode.EXECUTE);

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockPos playerPos = context.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);
        BlockPos terminalPos = context.absolutePos(CRAFTING_TERMINAL);
        LumungusCraftingMenu menu = new LumungusCraftingMenu(
                4,
                player.getInventory(),
                ContainerLevelAccess.create(context.getLevel(), terminalPos),
                terminalPos
        );

        menu.placeRecipeFromNetwork(Identifier.parse("minecraft:oak_planks"), 10);

        context.assertValueEqual(
                menu.getInputGridSlots().getFirst().getItem().getCount(),
                3,
                "Ten requested planks require three log crafts"
        );
        context.assertValueEqual(menu.getResultSlot().getItem().getCount(), 4, "Recipe batch output remains four planks");
        context.succeed();
    }

    @GameTest(padding = 32)
    public void craftingTerminalRecursivelyPreparesRequestedBarrelAmount(GameTestHelper context) {
        context.setBlock(FIRST_CONTROLLER, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(DRIVE_BAY, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(CRAFTING_TERMINAL, LumungusStorageBlocks.CRAFTING_TERMINAL);

        DriveBayBlockEntity driveBay = context.getBlockEntity(DRIVE_BAY, DriveBayBlockEntity.class);
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        StorageControllerBlockEntity controller = context.getBlockEntity(
                FIRST_CONTROLLER,
                StorageControllerBlockEntity.class
        );
        controller.insert(new ItemStack(Items.OAK_LOG, 4), TransferMode.EXECUTE);

        ServerPlayer player = context.makeMockServerPlayerInLevel();
        BlockPos playerPos = context.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(playerPos.getX() + 0.5, playerPos.getY(), playerPos.getZ() + 0.5);
        BlockPos terminalPos = context.absolutePos(CRAFTING_TERMINAL);
        LumungusCraftingMenu menu = new LumungusCraftingMenu(
                5,
                player.getInventory(),
                ContainerLevelAccess.create(context.getLevel(), terminalPos),
                terminalPos
        );

        menu.placeRecipeFromNetwork(Identifier.parse("minecraft:barrel"), 2);

        context.assertTrue(menu.getResultSlot().getItem().is(Items.BARREL), "Recursive barrel result");
        for (Slot slot : menu.getInputGridSlots()) {
            if (!slot.getItem().isEmpty()) {
                context.assertValueEqual(slot.getItem().getCount(), 2, "Two barrel ingredient sets");
            }
        }
        context.assertValueEqual(controller.count(new ItemStack(Items.OAK_LOG)), 0L, "Consumed oak logs");
        context.assertValueEqual(controller.count(new ItemStack(Items.OAK_PLANKS)), 1L, "Surplus oak plank");
        context.assertValueEqual(controller.count(new ItemStack(Items.OAK_SLAB)), 2L, "Surplus oak slabs");
        context.succeed();
    }

    @GameTest
    public void controllerAndTerminalPreserveHorizontalDisplayFacing(GameTestHelper context) {
        BlockState controller = LumungusStorageBlocks.STORAGE_CONTROLLER.defaultBlockState()
                .setValue(StorageControllerBlock.FACING, Direction.NORTH)
                .rotate(Rotation.CLOCKWISE_90);
        BlockState terminal = LumungusStorageBlocks.CRAFTING_TERMINAL.defaultBlockState()
                .setValue(CraftingTerminalBlock.FACING, Direction.NORTH)
                .rotate(Rotation.CLOCKWISE_90);

        context.setBlock(new BlockPos(1, 1, 1), controller);
        context.setBlock(new BlockPos(2, 1, 1), terminal);

        context.assertValueEqual(
                context.getBlockState(new BlockPos(1, 1, 1)).getValue(StorageControllerBlock.FACING),
                Direction.EAST,
                "Storage Controller display facing"
        );
        context.assertValueEqual(
                context.getBlockState(new BlockPos(2, 1, 1)).getValue(CraftingTerminalBlock.FACING),
                Direction.EAST,
                "Crafting Terminal display facing"
        );
        context.succeed();
    }

    @GameTest
    public void designerBlocksPreserveHorizontalFrontFacing(GameTestHelper context) {
        BlockState driveBay = LumungusStorageBlocks.DRIVE_BAY.defaultBlockState()
                .setValue(DriveBayBlock.FACING, Direction.NORTH)
                .rotate(Rotation.CLOCKWISE_90);
        BlockState inventoryConnector = LumungusStorageBlocks.INVENTORY_CONNECTOR.defaultBlockState()
                .setValue(InventoryConnectorBlock.FACING, Direction.NORTH)
                .rotate(Rotation.CLOCKWISE_90);
        BlockState inventoryTrim = LumungusStorageBlocks.INVENTORY_TRIM.defaultBlockState()
                .setValue(InventoryConnectorBlock.FACING, Direction.NORTH)
                .rotate(Rotation.CLOCKWISE_90);
        BlockState wirelessController = LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL
                .defaultBlockState()
                .setValue(WirelessStorageControllerBlock.FACING, Direction.NORTH)
                .rotate(Rotation.CLOCKWISE_90);
        BlockState wirelessConnector = LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_MULTIDIMENSIONAL
                .defaultBlockState()
                .setValue(WirelessInventoryConnectorBlock.FACING, Direction.NORTH)
                .rotate(Rotation.CLOCKWISE_90);

        context.assertValueEqual(driveBay.getValue(DriveBayBlock.FACING), Direction.EAST, "Drive Bay front");
        context.assertValueEqual(
                inventoryConnector.getValue(InventoryConnectorBlock.FACING),
                Direction.EAST,
                "Inventory Connector front"
        );
        context.assertValueEqual(
                inventoryTrim.getValue(InventoryConnectorBlock.FACING),
                Direction.EAST,
                "Inventory Trim front"
        );
        context.assertValueEqual(
                wirelessController.getValue(WirelessStorageControllerBlock.FACING),
                Direction.EAST,
                "Wireless Storage Controller front"
        );
        context.assertValueEqual(
                wirelessConnector.getValue(WirelessInventoryConnectorBlock.FACING),
                Direction.EAST,
                "Wireless Inventory Connector front"
        );
        context.succeed();
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }

    private static ItemStack primedModule(StorageControllerBlockEntity controller) {
        ItemStack module = new ItemStack(LumungusStorageItems.WIRELESS_NETWORK_MODULE);
        module.set(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER, new BoundStorageController(
                controller.getLevel().dimension().identifier(),
                controller.getBlockPos().immutable(),
                controller.getNetworkId()
        ));
        return module;
    }
}
