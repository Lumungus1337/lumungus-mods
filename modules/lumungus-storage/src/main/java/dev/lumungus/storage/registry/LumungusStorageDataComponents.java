package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.data.StorageCellData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class LumungusStorageDataComponents {
    public static final DataComponentType<StorageCellData> STORAGE_CELL_DATA = register(
            "storage_cell_data",
            DataComponentType.<StorageCellData>builder()
                    .persistent(StorageCellData.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(StorageCellData.CODEC))
                    .cacheEncoding()
                    .build()
    );

    public static final DataComponentType<BoundStorageController> BOUND_STORAGE_CONTROLLER = register(
            "bound_storage_controller",
            DataComponentType.<BoundStorageController>builder()
                    .persistent(BoundStorageController.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(BoundStorageController.CODEC))
                    .cacheEncoding()
                    .build()
    );

    private LumungusStorageDataComponents() {
    }

    public static void register() {
        LumungusStorage.LOGGER.info("Registered Lumungus Storage data components");
    }

    private static <T> DataComponentType<T> register(String path, DataComponentType<T> componentType) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, path);
        ResourceKey<DataComponentType<?>> key = ResourceKey.create(Registries.DATA_COMPONENT_TYPE, id);
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, key, componentType);
    }
}
