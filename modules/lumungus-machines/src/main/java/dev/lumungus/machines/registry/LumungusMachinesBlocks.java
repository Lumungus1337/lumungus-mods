package dev.lumungus.machines.registry;

import dev.lumungus.machines.LumungusMachines;
import dev.lumungus.machines.block.AutocrafterBlock;
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

public final class LumungusMachinesBlocks {
    public static final Block AUTOCRAFTER = registerAutocrafter();

    private LumungusMachinesBlocks() {
    }

    public static void register() {
        LumungusMachines.LOGGER.info("Registered Lumungus Machines blocks");
    }

    private static Block registerAutocrafter() {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusMachines.MOD_ID, "autocrafter");
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Block block = new AutocrafterBlock(BlockBehaviour.Properties.of()
                .setId(blockKey)
                .mapColor(MapColor.METAL)
                .strength(3.0F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.COPPER));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        Registry.register(BuiltInRegistries.ITEM, itemKey, new BlockItem(block, new Item.Properties().setId(itemKey)));
        return block;
    }
}
