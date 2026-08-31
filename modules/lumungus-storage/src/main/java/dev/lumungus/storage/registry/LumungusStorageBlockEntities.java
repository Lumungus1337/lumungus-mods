package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.block.entity.CraftingTerminalBlockEntity;
import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.block.entity.InventoryConnectorBlockEntity;
import dev.lumungus.storage.block.entity.StorageBreakerBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.block.entity.StorageOutputBlockEntity;
import dev.lumungus.storage.block.entity.StoragePlacerBlockEntity;
import dev.lumungus.storage.block.entity.WirelessStorageControllerBlockEntity;
import dev.lumungus.storage.block.entity.WirelessInventoryConnectorBlockEntity;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class LumungusStorageBlockEntities {
    public static final BlockEntityType<StorageControllerBlockEntity> STORAGE_CONTROLLER = register(
            "storage_controller",
            StorageControllerBlockEntity::new,
            LumungusStorageBlocks.STORAGE_CONTROLLER
    );

    public static final BlockEntityType<CraftingTerminalBlockEntity> CRAFTING_TERMINAL = register(
            "crafting_terminal",
            CraftingTerminalBlockEntity::new,
            LumungusStorageBlocks.CRAFTING_TERMINAL
    );

    public static final BlockEntityType<DriveBayBlockEntity> DRIVE_BAY = register(
            "drive_bay",
            DriveBayBlockEntity::new,
            LumungusStorageBlocks.DRIVE_BAY
    );

    public static final BlockEntityType<InventoryConnectorBlockEntity> INVENTORY_CONNECTOR = register(
            "inventory_connector",
            InventoryConnectorBlockEntity::new,
            LumungusStorageBlocks.INVENTORY_CONNECTOR,
            LumungusStorageBlocks.INVENTORY_TRIM
    );

    public static final BlockEntityType<WirelessStorageControllerBlockEntity> WIRELESS_STORAGE_CONTROLLER = register(
            "wireless_storage_controller",
            WirelessStorageControllerBlockEntity::new,
            LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT,
            LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_DIMENSION,
            LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL
    );

    public static final BlockEntityType<WirelessInventoryConnectorBlockEntity> WIRELESS_INVENTORY_CONNECTOR = register(
            "wireless_inventory_connector",
            WirelessInventoryConnectorBlockEntity::new,
            LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_SHORT,
            LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_DIMENSION,
            LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_MULTIDIMENSIONAL
    );

    public static final BlockEntityType<StorageOutputBlockEntity> STORAGE_OUTPUT = register(
            "storage_output",
            StorageOutputBlockEntity::new,
            LumungusStorageBlocks.STORAGE_OUTPUT
    );

    public static final BlockEntityType<StorageBreakerBlockEntity> STORAGE_BREAKER = register(
            "storage_breaker",
            StorageBreakerBlockEntity::new,
            LumungusStorageBlocks.STORAGE_BREAKER
    );

    public static final BlockEntityType<StoragePlacerBlockEntity> STORAGE_PLACER = register(
            "storage_placer",
            StoragePlacerBlockEntity::new,
            LumungusStorageBlocks.STORAGE_PLACER
    );

    private LumungusStorageBlockEntities() {
    }

    public static void register() {
        LumungusStorage.LOGGER.info("Registered Lumungus Storage block entities");
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String path,
            BlockEntityType.BlockEntitySupplier<? extends T> factory,
            Block... validBlocks
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, path);
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id);
        BlockEntityType<T> type = new BlockEntityType<>(factory, Set.of(validBlocks));
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
    }
}
