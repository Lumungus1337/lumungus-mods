package de.djdaddy.railquarry.client;

import de.djdaddy.railquarry.menu.QuarryMenu;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class RailQuarryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(QuarryMenu.TYPE, QuarryScreen::new);
    }
}
