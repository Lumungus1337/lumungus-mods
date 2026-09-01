package dev.lumungus.machines.client;

import dev.lumungus.machines.client.screen.AutocrafterScreen;
import dev.lumungus.machines.registry.LumungusMachinesMenus;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class LumungusMachinesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(LumungusMachinesMenus.AUTOCRAFTER, AutocrafterScreen::new);
    }
}
