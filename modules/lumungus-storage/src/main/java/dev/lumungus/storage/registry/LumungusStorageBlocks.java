package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.block.CraftingTerminalBlock;
import dev.lumungus.storage.block.DriveBayBlock;
import dev.lumungus.storage.block.InventoryConnectorBlock;
import dev.lumungus.storage.block.InventoryCableBlock;
import dev.lumungus.storage.block.StorageControllerBlock;
import java.util.function.Function;
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
            StorageControllerBlock::new
    );

    public static final Block CRAFTING_TERMINAL = registerBlockWithItem(
            "crafting_terminal",
            CraftingTerminalBlock::new
    );

    public static final Block DRIVE_BAY = registerBlockWithItem(
            "drive_bay",
            DriveBayBlock::new
    );

    public static final Block INVENTORY_CONNECTOR = registerBlockWithItem(
            "inventory_connector",
            InventoryConnectorBlock::new
    );

    public static final Block INVENTORY_TRIM = registerBlockWithItem(
            "inventory_trim",
            InventoryConnectorBlock::new
    );

    public static final Block INVENTORY_CABLE = registerBlockWithItem(
            "inventory_cable",
            InventoryCableBlock::new
    );

    private LumungusStorageBlocks() {
    }

    public static void register() {
        LumungusStorage.LOGGER.info("Registered Lumungus Storage blocks");
    }

    private static Block registerBlockWithItem(String path, Function<BlockBehaviour.Properties, Block> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Block block = factory.apply(
                BlockBehaviour.Properties.of()
                        .setId(blockKey)
                        .mapColor(MapColor.METAL)
                        .strength(2.5F, 6.0F)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.METAL)
        );

        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));

        return block;
    }
}
