package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.block.RetroStorageBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class LumungusStorageBlocks {
    public static final Block STORAGE_CONTROLLER = registerBlockWithItem(
            "storage_controller",
            "message.lumungus_storage.storage_controller.pending"
    );

    public static final Block CRAFTING_TERMINAL = registerBlockWithItem(
            "crafting_terminal",
            "message.lumungus_storage.crafting_terminal.pending"
    );

    private LumungusStorageBlocks() {
    }

    public static void register() {
        LumungusStorage.LOGGER.info("Registered Lumungus Storage blocks");
    }

    private static Block registerBlockWithItem(String path, String statusTranslationKey) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Block block = new RetroStorageBlock(
                BlockBehaviour.Properties.of()
                        .setId(blockKey)
                        .mapColor(MapColor.METAL)
                        .strength(2.5F, 6.0F)
                        .sound(SoundType.METAL),
                statusTranslationKey
        );

        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));

        return block;
    }
}
