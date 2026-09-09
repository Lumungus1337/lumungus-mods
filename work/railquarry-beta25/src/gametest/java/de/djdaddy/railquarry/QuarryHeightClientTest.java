package de.djdaddy.railquarry;

import de.djdaddy.railquarry.block.entity.QuarryBlockEntity;
import de.djdaddy.railquarry.client.QuarryScreen;
import de.djdaddy.railquarry.menu.QuarryMenu;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public final class QuarryHeightClientTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().setUseConsistentSettings(true).create()) {
            var connection = world.getConnection();
            connection.waitForChunksDownload();
            connection.waitForChunksRender();
            world.getServer().runOnServer(server -> {
                var player = connection.getServerPlayer();
                var pos = player.blockPosition().offset(2, 0, 0);
                player.level().setBlockAndUpdate(pos, RailQuarryMod.QUARRY.defaultBlockState());
                var quarry = (QuarryBlockEntity) player.level().getBlockEntity(pos);
                quarry.setItem(0, new ItemStack(Items.COAL, 32));
                quarry.setItem(9, new ItemStack(Items.SHULKER_BOX));
                player.openMenu(quarry);
            });
            connection.waitForClientboundPackets();
            context.waitForScreen(QuarryScreen.class);
            context.runOnClient(client -> {
                var screen = (QuarryScreen) client.gui.screen();
                var box = (EditBox) screen.children().stream().filter(EditBox.class::isInstance).findFirst().orElseThrow();
                screen.setFocused(box);
                box.setValue("32");
            });
            context.getInput().pressKey(options -> options.keyInventory);
            context.waitForScreen(QuarryScreen.class);
            context.getInput().pressKey(GLFW.GLFW_KEY_ENTER);
            context.waitFor(client -> ((QuarryMenu) client.player.containerMenu).miningHeight() == 32);
            world.getServer().runOnServer(server -> {
                var player = connection.getServerPlayer();
                var quarry = (QuarryBlockEntity) player.level().getBlockEntity(player.blockPosition().offset(2, 0, 0));
                if (quarry.getMiningHeight() != 32) throw new AssertionError("Client height was not applied on server");
            });
            context.waitTicks(5);
            context.takeScreenshot("quarry-height-32-beta28");
        }
    }
}
