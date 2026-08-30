package dev.lumungus.storage.gametest;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import dev.lumungus.storage.registry.LumungusStorageItems;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class StorageNetworkGameTest implements CustomTestMethodInvoker {
    private static final BlockPos FIRST_CONTROLLER = new BlockPos(1, 1, 1);
    private static final BlockPos DRIVE_BAY = new BlockPos(4, 1, 1);
    private static final BlockPos SECOND_CONTROLLER = new BlockPos(3, 1, 1);
    private static final BlockPos CRAFTING_TERMINAL = new BlockPos(2, 1, 1);

    @GameTest
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

    @GameTest
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

    @GameTest
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

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }
}
