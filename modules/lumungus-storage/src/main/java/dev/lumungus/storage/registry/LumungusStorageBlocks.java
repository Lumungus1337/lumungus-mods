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
import dev.lumungus.storage.block.WirelessInventoryConnectorBlock;
import dev.lumungus.storage.item.LumungusBlockItem;
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
            StorageControllerBlock::new,
            tooltip("storage_controller")
    );

    public static final Block CRAFTING_TERMINAL = registerBlockWithItem(
            "crafting_terminal",
            CraftingTerminalBlock::new,
            tooltip("crafting_terminal")
    );

    public static final Block DRIVE_BAY = registerBlockWithItem(
            "drive_bay",
            DriveBayBlock::new,
            tooltip("drive_bay")
    );

    public static final Block INVENTORY_CONNECTOR = registerBlockWithItem(
            "inventory_connector",
            InventoryConnectorBlock::new,
            tooltip("inventory_connector")
    );

    public static final Block INVENTORY_TRIM = registerBlockWithItem(
            "inventory_trim",
            InventoryConnectorBlock::new,
            tooltip("inventory_trim")
    );

    public static final Block PNEUMATIC_PIPE = registerBlockWithItem(
            "pneumatic_pipe",
            InventoryCableBlock::new,
            true,
            tooltip("pneumatic_pipe")
    );

    public static final Block INVENTORY_CABLE = registerBlockWithItem(
            "inventory_cable",
            InventoryCableBlock::new,
            true,
            tooltip("inventory_cable")
    );

    public static final Block WIRELESS_STORAGE_CONTROLLER_SHORT = registerBlockWithItem(
            "wireless_storage_controller_short",
            properties -> new WirelessStorageControllerBlock(
                    properties,
                    WirelessStorageControllerBlock.WirelessTier.SHORT_RANGE
            ),
            tooltip("wireless_storage_controller_short")
    );

    public static final Block WIRELESS_STORAGE_CONTROLLER_DIMENSION = registerBlockWithItem(
            "wireless_storage_controller_dimension",
            properties -> new WirelessStorageControllerBlock(
                    properties,
                    WirelessStorageControllerBlock.WirelessTier.SAME_DIMENSION
            ),
            tooltip("wireless_storage_controller_dimension")
    );

    public static final Block WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL = registerBlockWithItem(
            "wireless_storage_controller_multidimensional",
            properties -> new WirelessStorageControllerBlock(
                    properties,
                    WirelessStorageControllerBlock.WirelessTier.MULTIDIMENSIONAL
            ),
            tooltip("wireless_storage_controller_multidimensional")
    );

    public static final Block WIRELESS_INVENTORY_CONNECTOR_SHORT = registerBlockWithItem(
            "wireless_inventory_connector_short",
            properties -> new WirelessInventoryConnectorBlock(
                    properties,
                    WirelessInventoryConnectorBlock.WirelessConnectorTier.SHORT_RANGE
            ),
            tooltip("wireless_inventory_connector_short")
    );

    public static final Block WIRELESS_INVENTORY_CONNECTOR_DIMENSION = registerBlockWithItem(
            "wireless_inventory_connector_dimension",
            properties -> new WirelessInventoryConnectorBlock(
                    properties,
                    WirelessInventoryConnectorBlock.WirelessConnectorTier.SAME_DIMENSION
            ),
            tooltip("wireless_inventory_connector_dimension")
    );

    public static final Block WIRELESS_INVENTORY_CONNECTOR_MULTIDIMENSIONAL = registerBlockWithItem(
            "wireless_inventory_connector_multidimensional",
            properties -> new WirelessInventoryConnectorBlock(
                    properties,
                    WirelessInventoryConnectorBlock.WirelessConnectorTier.MULTIDIMENSIONAL
            ),
            tooltip("wireless_inventory_connector_multidimensional")
    );

    public static final Block STORAGE_OUTPUT = registerBlockWithItem(
            "storage_output",
            StorageOutputBlock::new,
            tooltip("storage_output")
    );

    public static final Block STORAGE_BREAKER = registerBlockWithItem(
            "storage_breaker",
            StorageBreakerBlock::new,
            tooltip("storage_breaker")
    );

    public static final Block STORAGE_PLACER = registerBlockWithItem(
            "storage_placer",
            StoragePlacerBlock::new,
            tooltip("storage_placer")
    );

    private LumungusStorageBlocks() {
    }

    public static void register() {
        LumungusStorage.LOGGER.info("Registered Lumungus Storage blocks");
    }

    private static Block registerBlockWithItem(String path, Function<BlockBehaviour.Properties, Block> factory) {
        return registerBlockWithItem(path, factory, false, null);
    }

    private static Block registerBlockWithItem(
            String path,
            Function<BlockBehaviour.Properties, Block> factory,
            String tooltipKey
    ) {
        return registerBlockWithItem(path, factory, false, tooltipKey);
    }

    private static Block registerBlockWithItem(
            String path,
            Function<BlockBehaviour.Properties, Block> factory,
            boolean noOcclusion
    ) {
        return registerBlockWithItem(path, factory, noOcclusion, null);
    }

    private static Block registerBlockWithItem(
            String path,
            Function<BlockBehaviour.Properties, Block> factory,
            boolean noOcclusion,
            String tooltipKey
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, path);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .setId(blockKey)
                .mapColor(MapColor.METAL)
                .strength(2.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
        if (noOcclusion) {
            properties = properties.noOcclusion();
        }

        Block block = factory.apply(properties);

        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                tooltipKey == null
                        ? new BlockItem(block, new Item.Properties().setId(itemKey))
                        : new LumungusBlockItem(block, new Item.Properties().setId(itemKey), tooltipKey)
        );

        return block;
    }

    private static String tooltip(String path) {
        return "tooltip.lumungus_storage." + path;
    }
}
