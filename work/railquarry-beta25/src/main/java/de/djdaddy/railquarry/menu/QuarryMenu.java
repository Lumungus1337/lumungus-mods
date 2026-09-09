package de.djdaddy.railquarry.menu;

import de.djdaddy.railquarry.RailQuarryMod;
import de.djdaddy.railquarry.block.entity.QuarryBlockEntity;
import de.djdaddy.railquarry.block.entity.QuarrySettingsContainer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.DataSlot;

public final class QuarryMenu extends ChestMenu {
    public static final ExtendedMenuType<QuarryMenu, BlockPos> TYPE = Registry.register(
            BuiltInRegistries.MENU, ResourceKey.create(Registries.MENU, RailQuarryMod.id("quarry")),
            new ExtendedMenuType<>(QuarryMenu::new, BlockPos.STREAM_CODEC));
    private final QuarryBlockEntity quarry;
    private final DataSlot height;

    public static void register() { }

    public QuarryMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, new SimpleContainer(54), null);
    }

    public QuarryMenu(int id, Inventory inventory, QuarryBlockEntity quarry) {
        this(id, inventory, new QuarrySettingsContainer(quarry), quarry);
    }

    private QuarryMenu(int id, Inventory inventory, Container container, QuarryBlockEntity quarry) {
        super(TYPE, id, inventory, container, 6);
        this.quarry = quarry;
        height = addDataSlot(new DataSlot() {
            private int value = QuarryBlockEntity.HEIGHT;
            @Override public int get() { return quarry == null ? value : quarry.getMiningHeight(); }
            @Override public void set(int value) { this.value = value; }
        });
    }

    public int miningHeight() { return height.get(); }

    @Override
    public boolean clickMenuButton(Player player, int value) {
        if (quarry == null || !stillValid(player) || value < 1 || value > QuarryBlockEntity.MAX_HEIGHT) {
            return false;
        }
        quarry.setMiningHeight(value);
        broadcastChanges();
        return true;
    }
}
