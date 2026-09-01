package dev.lumungus.machines.registry;

import dev.lumungus.machines.LumungusMachines;
import dev.lumungus.machines.menu.AutocrafterMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;

public final class LumungusMachinesMenus {
    public static final ExtendedMenuType<AutocrafterMenu, BlockPos> AUTOCRAFTER = register(
            "autocrafter",
            new ExtendedMenuType<>(AutocrafterMenu::new, BlockPos.STREAM_CODEC)
    );

    private LumungusMachinesMenus() {
    }

    public static void register() {
        LumungusMachines.LOGGER.info("Registered Lumungus Machines menus");
    }

    private static <T extends MenuType<?>> T register(String path, T menuType) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusMachines.MOD_ID, path);
        ResourceKey<MenuType<?>> key = ResourceKey.create(Registries.MENU, id);
        return Registry.register(BuiltInRegistries.MENU, key, menuType);
    }
}
