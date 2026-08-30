package dev.lumungus.storage.client;

import dev.lumungus.storage.client.screen.LumungusCraftingTerminalScreen;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.network.TerminalSnapshotPayload;
import dev.lumungus.storage.registry.LumungusStorageMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;

public final class LumungusStorageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(
                LumungusStorageMenus.CRAFTING_TERMINAL,
                LumungusCraftingTerminalScreen::new
        );
        ClientPlayNetworking.registerGlobalReceiver(TerminalSnapshotPayload.TYPE, (payload, context) -> {
            if (context.player().containerMenu instanceof LumungusCraftingMenu menu) {
                menu.applySnapshot(payload);
            }
        });
    }
}
