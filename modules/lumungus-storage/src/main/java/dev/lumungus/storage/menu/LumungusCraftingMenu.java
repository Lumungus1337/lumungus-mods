package dev.lumungus.storage.menu;

import dev.lumungus.storage.registry.LumungusStorageBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public final class LumungusCraftingMenu extends CraftingMenu {
    private final ContainerLevelAccess access;

    public LumungusCraftingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(containerId, inventory, access);
        this.access = access;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, LumungusStorageBlocks.CRAFTING_TERMINAL);
    }
}
