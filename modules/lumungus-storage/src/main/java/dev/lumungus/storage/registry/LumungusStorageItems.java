package dev.lumungus.storage.registry;

import dev.lumungus.storage.block.WirelessStorageControllerBlock;
import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.data.StorageCellData;
import dev.lumungus.storage.item.CopperWrenchItem;
import dev.lumungus.storage.item.PortableStorageInterfaceItem;
import dev.lumungus.storage.item.StorageCellItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class LumungusStorageItems {
    public static final Item COPPER_WRENCH = register(
            "copper_wrench",
            properties -> new CopperWrenchItem(properties.stacksTo(1).durability(256))
    );

    public static final Item STORAGE_CELL_16K = register(
            "storage_cell_16k",
            properties -> new StorageCellItem(properties
                    .stacksTo(1)
                    .component(LumungusStorageDataComponents.STORAGE_CELL_DATA, StorageCellData.EMPTY))
    );

    public static final Item PORTABLE_STORAGE_INTERFACE_SHORT = register(
            "portable_storage_interface_short",
            properties -> new PortableStorageInterfaceItem(
                    properties.stacksTo(1),
                    WirelessStorageControllerBlock.WirelessTier.SHORT_RANGE
            )
    );

    public static final Item PORTABLE_STORAGE_INTERFACE_DIMENSION = register(
            "portable_storage_interface_dimension",
            properties -> new PortableStorageInterfaceItem(
                    properties.stacksTo(1),
                    WirelessStorageControllerBlock.WirelessTier.SAME_DIMENSION
            )
    );

    public static final Item PORTABLE_STORAGE_INTERFACE_MULTIDIMENSIONAL = register(
            "portable_storage_interface_multidimensional",
            properties -> new PortableStorageInterfaceItem(
                    properties.stacksTo(1),
                    WirelessStorageControllerBlock.WirelessTier.MULTIDIMENSIONAL
            )
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
