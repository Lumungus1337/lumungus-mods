package dev.lumungus.machines.gametest;

import dev.lumungus.machines.block.entity.AutocrafterBlockEntity;
import dev.lumungus.machines.client.screen.AutocrafterScreen;
import dev.lumungus.machines.menu.AutocrafterMenu;
import dev.lumungus.machines.registry.LumungusMachinesBlocks;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AutocrafterClientGameTest implements FabricClientGameTest {
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
                BlockPos autocrafterPos = player.blockPosition().offset(2, 0, 0);
                player.level().setBlockAndUpdate(
                        autocrafterPos,
                        LumungusMachinesBlocks.AUTOCRAFTER.defaultBlockState()
                );
                Object blockEntity = player.level().getBlockEntity(autocrafterPos);
                if (!(blockEntity instanceof AutocrafterBlockEntity autocrafter)) {
                    throw new IllegalStateException("Missing Autocrafter block entity");
                }
                autocrafter.setTarget(new ItemStack(Items.OAK_PLANKS), 120);
                autocrafter.setPaused(true);
                player.openMenu(autocrafter);
            });

            connection.waitForClientboundPackets();
            context.waitForScreen(AutocrafterScreen.class);
            context.waitFor(client -> client.player != null
                    && client.player.containerMenu instanceof AutocrafterMenu menu
                    && menu.targetAmount() == 120
                    && menu.paused());
            context.waitFor(client -> "120".equals(AutocrafterScreen.lastAmountValueForTests()));
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitTicks(2);
            context.waitForScreen(AutocrafterScreen.class);
            context.waitTicks(10);
            context.takeScreenshot("lumungus-autocrafter-menu-uat38");
            context.getInput().pressKey(org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE);
            context.waitForScreen(null);
            singleplayer.getServer().runOnServer(server -> {
                ServerPlayer player = connection.getServerPlayer();
                player.level().setBlockAndUpdate(player.blockPosition().offset(0, 1, 4),
                        LumungusMachinesBlocks.AUTOCRAFTER.defaultBlockState());
            });
            connection.waitForClientboundPackets();
            context.getInput().lookAt(context.computeOnClient(client -> client.player.blockPosition().offset(0, 1, 4)));
            context.getInput().pressKey(org.lwjgl.glfw.GLFW.GLFW_KEY_F1);
            context.runOnClient(client -> client.options.fov().set(35));
            context.waitTicks(20);
            context.takeScreenshot("lumungus-autocrafter-relief-uat60");
        }
    }
}
