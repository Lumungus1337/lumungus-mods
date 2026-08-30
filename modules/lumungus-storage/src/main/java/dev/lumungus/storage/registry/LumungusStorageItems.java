package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.data.StorageCellData;
import dev.lumungus.storage.item.StorageCellItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class LumungusStorageItems {
    public static final Item STORAGE_CELL_16K = register(
            "storage_cell_16k",
            properties -> new StorageCellItem(properties
                    .stacksTo(1)
                    .component(LumungusStorageDataComponents.STORAGE_CELL_DATA, StorageCellData.EMPTY))
    );

    private LumungusStorageItems() {
    }

    public static void register() {
        LumungusStorage.LOGGER.info("Registered Lumungus Storage items");
    }

    private static Item register(String path, Function<Item.Properties, Item> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, path);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Item item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }
}
