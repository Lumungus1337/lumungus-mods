package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import dev.lumungus.storage.block.CraftingTerminalBlock;
import dev.lumungus.storage.block.DriveBayBlock;
import dev.lumungus.storage.block.InventoryConnectorBlock;
import dev.lumungus.storage.block.InventoryCableBlock;
import dev.lumungus.storage.block.StorageBreakerBlock;
import dev.lumungus.storage.block.StorageControllerBlock;
import dev.lumungus.storage.block.StorageOutputBlock;
import dev.lumungus.storage.block.StoragePlacerBlock;
import dev.lumungus.storage.block.WirelessStorageControllerBlock;
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

    public static final Block WIRELESS_STORAGE_CONTROLLER_SHORT = registerBlockWithItem(
            "wireless_storage_controller_short",
            properties -> new WirelessStorageControllerBlock(
                    properties,
                    WirelessStorageControllerBlock.WirelessTier.SHORT_RANGE
            )
    );

    public static final Block WIRELESS_STORAGE_CONTROLLER_DIMENSION = registerBlockWithItem(
            "wireless_storage_controller_dimension",
            properties -> new WirelessStorageControllerBlock(
                    properties,
                    WirelessStorageControllerBlock.WirelessTier.SAME_DIMENSION
            )
    );

    public static final Block WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL = registerBlockWithItem(
            "wireless_storage_controller_multidimensional",
            properties -> new WirelessStorageControllerBlock(
                    properties,
                    WirelessStorageControllerBlock.WirelessTier.MULTIDIMENSIONAL
            )
    );

    public static final Block STORAGE_OUTPUT = registerBlockWithItem(
            "storage_output",
            StorageOutputBlock::new
    );

    public static final Block STORAGE_BREAKER = registerBlockWithItem(
            "storage_breaker",
            StorageBreakerBlock::new
    );

    public static final Block STORAGE_PLACER = registerBlockWithItem(
            "storage_placer",
            StoragePlacerBlock::new
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
