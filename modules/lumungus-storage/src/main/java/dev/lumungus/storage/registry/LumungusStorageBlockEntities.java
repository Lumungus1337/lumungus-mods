package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.block.entity.CraftingTerminalBlockEntity;
import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
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
