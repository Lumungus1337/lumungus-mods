package dev.lumungus.storage.gametest;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.storage.block.entity.CraftingTerminalBlockEntity;
import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.client.screen.LumungusCraftingTerminalScreen;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.network.TerminalCraftingPlanPayload;
import java.util.List;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import dev.lumungus.storage.registry.LumungusStorageItems;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public final class StorageClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder()
                .setUseConsistentSettings(true)
                .create()) {
            TestServerConnection connection = singleplayer.getConnection();
            connection.waitForChunksDownload();
            connection.waitForChunksRender();

            singleplayer.getServer().runOnServer(server -> {
                ServerPlayer player = connection.getServerPlayer();
                BlockPos controllerPos = player.blockPosition().offset(2, 0, 0);
                BlockPos driveBayPos = controllerPos.offset(1, 0, 0);
                BlockPos terminalPos = controllerPos.offset(0, 0, 1);

                player.level().setBlockAndUpdate(
                        controllerPos,
                        LumungusStorageBlocks.STORAGE_CONTROLLER.defaultBlockState()
                );
                player.level().setBlockAndUpdate(
                        driveBayPos,
                        LumungusStorageBlocks.DRIVE_BAY.defaultBlockState()
                );
                player.level().setBlockAndUpdate(
                        terminalPos,
                        LumungusStorageBlocks.CRAFTING_TERMINAL.defaultBlockState()
                );

                DriveBayBlockEntity driveBay = requireBlockEntity(
                        player,
                        driveBayPos,
                        DriveBayBlockEntity.class
                );
                StorageControllerBlockEntity controller = requireBlockEntity(
                        player,
                        controllerPos,
                        StorageControllerBlockEntity.class
                );
                CraftingTerminalBlockEntity terminal = requireBlockEntity(
                        player,
                        terminalPos,
                        CraftingTerminalBlockEntity.class
                );

                requireEmptyRemainder(driveBay.insertCell(
                        new ItemStack(LumungusStorageItems.STORAGE_CELL_16K),
                        TransferMode.EXECUTE
                ));
                insert(controller, Items.COBBLESTONE, 4096);
                insert(controller, Items.OAK_LOG, 512);
                insert(controller, Items.IRON_INGOT, 192);
                insert(controller, Items.REDSTONE, 128);
                insert(controller, Items.DIAMOND, 24);

                if (!terminal.refreshControllerLink()) {
                    throw new IllegalStateException("Crafting Terminal did not link to the test controller");
                }
                player.openMenu(terminal);
            });

            connection.waitForClientboundPackets();
            context.waitForScreen(LumungusCraftingTerminalScreen.class);
            context.waitFor(client -> client.player != null
                    && client.player.containerMenu instanceof LumungusCraftingMenu menu
                    && menu.networkResources().size() == 5);
            context.getInput().typeChars("stein");
            context.waitFor(client -> "stein".equals(LumungusCraftingTerminalScreen.lastSearchValueForTests()));
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(2);
            context.waitForScreen(LumungusCraftingTerminalScreen.class);
            context.getInput().pressKey(GLFW.GLFW_KEY_TAB);
            selectAllText(context);
            context.getInput().typeChars("120");
            context.waitFor(client -> client.player.containerMenu instanceof LumungusCraftingMenu menu
                    && menu.requestedCraftResultAmount() == 120);
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(LumungusCraftingTerminalScreen.class);
            context.getInput().pressKey(GLFW.GLFW_KEY_TAB);
            context.runOnClient(client -> client.gui.screen().setFocused(null));
            context.getInput().pressKey(options -> options.keyInventory);
            context.getInput().typeChars("e");
            context.waitFor(client -> "steine".equals(LumungusCraftingTerminalScreen.lastSearchValueForTests()));
            selectAllText(context);
            context.getInput().pressKey(GLFW.GLFW_KEY_BACKSPACE);
            context.getInput().setCursorPos(0, 0);
            context.waitTicks(20);
            for (int frame = 1; frame <= 5; frame++) {
                context.takeScreenshot("lumungus-storage-terminal-uat3-0" + frame);
                context.waitTicks(5);
            }
            context.runOnClient(client -> {
                LumungusCraftingMenu menu = (LumungusCraftingMenu) client.player.containerMenu;
                menu.applyCraftingPlan(new TerminalCraftingPlanPayload(menu.containerId,
                        new ItemStack(Items.BARREL), 120, List.of(
                        new TerminalCraftingPlanPayload.Stage(List.of(
                                new TerminalCraftingPlanPayload.Ingredient(new ItemStack(Items.OAK_LOG), 210)),
                                new ItemStack(Items.OAK_PLANKS), 840),
                        new TerminalCraftingPlanPayload.Stage(List.of(
                                new TerminalCraftingPlanPayload.Ingredient(new ItemStack(Items.OAK_PLANKS), 120)),
                                new ItemStack(Items.OAK_SLAB), 240),
                        new TerminalCraftingPlanPayload.Stage(List.of(
                                new TerminalCraftingPlanPayload.Ingredient(new ItemStack(Items.OAK_PLANKS), 720),
                                new TerminalCraftingPlanPayload.Ingredient(new ItemStack(Items.OAK_SLAB), 240)),
                                new ItemStack(Items.BARREL), 120))));
            });
            context.waitTicks(5);
            context.takeScreenshot("lumungus-storage-crafting-plan-uat58");
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitForScreen(LumungusCraftingTerminalScreen.class);
        }
    }

    private static void selectAllText(ClientGameTestContext context) {
        // Fabric TestInput currently creates key events with zero modifiers.
        context.runOnClient(client -> client.gui.screen().keyPressed(
                new KeyEvent(GLFW.GLFW_KEY_A, 0, GLFW.GLFW_MOD_CONTROL)));
    }

    private static void insert(StorageControllerBlockEntity controller, net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        while (remaining > 0) {
            int batchSize = Math.min(remaining, item.getDefaultMaxStackSize());
            requireEmptyRemainder(controller.insert(new ItemStack(item, batchSize), TransferMode.EXECUTE));
            remaining -= batchSize;
        }
    }

    private static void requireEmptyRemainder(ItemStack remainder) {
        if (!remainder.isEmpty()) {
            throw new IllegalStateException("Test storage rejected " + remainder);
        }
    }

    private static <T> T requireBlockEntity(ServerPlayer player, BlockPos pos, Class<T> type) {
        Object blockEntity = player.level().getBlockEntity(pos);
        if (!type.isInstance(blockEntity)) {
            throw new IllegalStateException("Missing " + type.getSimpleName() + " at " + pos);
        }
        return type.cast(blockEntity);
    }
}
