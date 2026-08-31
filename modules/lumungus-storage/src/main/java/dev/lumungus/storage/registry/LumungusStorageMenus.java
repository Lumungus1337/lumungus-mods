package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.menu.DriveBayMenu;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;

public final class LumungusStorageMenus {
    public static final ExtendedMenuType<LumungusCraftingMenu, BlockPos> CRAFTING_TERMINAL = register(
            "crafting_terminal",
            new ExtendedMenuType<>(LumungusCraftingMenu::new, BlockPos.STREAM_CODEC)
    );
    public static final ExtendedMenuType<DriveBayMenu, BlockPos> DRIVE_BAY = register(
            "drive_bay",
            new ExtendedMenuType<>(DriveBayMenu::new, BlockPos.STREAM_CODEC)
    );

    private LumungusStorageMenus() {
    }

    public static void register() {
        LumungusStorage.LOGGER.info("Registered Lumungus Storage menus");
    }

    private static <T extends MenuType<?>> T register(String path, T menuType) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, path);
        ResourceKey<MenuType<?>> key = ResourceKey.create(Registries.MENU, id);
        return Registry.register(BuiltInRegistries.MENU, key, menuType);
    }
}
