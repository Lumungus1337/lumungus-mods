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
                player.getInventory().setItem(0, new ItemStack(LumungusStorageItems.COPPER_WRENCH));
                player.getInventory().setItem(1, new ItemStack(LumungusStorageItems.WIRELESS_NETWORK_MODULE));
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
            context.getInput().pressKey(GLFW.GLFW_KEY_ESCAPE);
            context.waitForScreen(null);
            singleplayer.getServer().runOnServer(server -> {
                ServerPlayer player = connection.getServerPlayer();
                var blocks = List.of(LumungusStorageBlocks.STORAGE_CONTROLLER, LumungusStorageBlocks.CRAFTING_TERMINAL,
                        LumungusStorageBlocks.DRIVE_BAY, LumungusStorageBlocks.INVENTORY_CONNECTOR,
                        LumungusStorageBlocks.INVENTORY_TRIM, LumungusStorageBlocks.STORAGE_OUTPUT,
                        LumungusStorageBlocks.STORAGE_BREAKER, LumungusStorageBlocks.STORAGE_PLACER,
                        LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT,
                        LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_DIMENSION,
                        LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL,
                        LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_SHORT,
                        LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_DIMENSION,
                        LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_MULTIDIMENSIONAL,
                        LumungusStorageBlocks.PNEUMATIC_PIPE);
                for (int i = 0; i < blocks.size(); i++) {
                    var state = blocks.get(i).defaultBlockState();
                    if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
                        state = state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING,
                                net.minecraft.core.Direction.NORTH);
                    }
                    BlockPos position = i == 14 ? player.blockPosition().offset(2, 2, 10)
                            : player.blockPosition().offset((i % 5 - 2) * 2, 5 - (i / 5) * 2, 10);
                    player.level().setBlockAndUpdate(position, state);
                }
            });
            connection.waitForClientboundPackets();
            context.getInput().lookAt(context.computeOnClient(client -> client.player.blockPosition().offset(0, 3, 10)));
            context.getInput().pressKey(GLFW.GLFW_KEY_F1);
            context.waitTicks(20);
            context.takeScreenshot("lumungus-block-relief-gallery-uat60");
            context.runOnClient(client -> client.options.fov().set(35));
            for (int row = 0; row < 3; row++) {
                int height = 5 - row * 2;
                context.getInput().lookAt(context.computeOnClient(client -> client.player.blockPosition().offset(0, height, 10)));
                context.waitTicks(10);
                context.takeScreenshot("lumungus-block-relief-detail-" + row + "-uat60");
            }
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
