package dev.lumungus.storage.menu;

import dev.lumungus.storage.registry.LumungusStorageBlocks;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public final class LumungusCraftingMenu extends CraftingMenu {
    // Keep the vanilla crafting slot order: JEI's built-in transfer handler targets these ranges.
    public static final int RECIPE_SLOT_START = 1;
    public static final int RECIPE_SLOT_COUNT = 9;
    public static final int PLAYER_INVENTORY_SLOT_START = 10;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = 36;

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
