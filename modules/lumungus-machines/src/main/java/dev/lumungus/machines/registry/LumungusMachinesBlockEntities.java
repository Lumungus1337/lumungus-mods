package dev.lumungus.machines.registry;

import dev.lumungus.machines.LumungusMachines;
import dev.lumungus.machines.block.entity.AutocrafterBlockEntity;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class LumungusMachinesBlockEntities {
    public static final BlockEntityType<AutocrafterBlockEntity> AUTOCRAFTER = registerAutocrafter();

    private LumungusMachinesBlockEntities() {
    }

    public static void register() {
        LumungusMachines.LOGGER.info("Registered Lumungus Machines block entities");
    }

    private static BlockEntityType<AutocrafterBlockEntity> registerAutocrafter() {
        Identifier id = Identifier.fromNamespaceAndPath(LumungusMachines.MOD_ID, "autocrafter");
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id);
        BlockEntityType<AutocrafterBlockEntity> type = new BlockEntityType<>(
                AutocrafterBlockEntity::new,
                Set.of(LumungusMachinesBlocks.AUTOCRAFTER)
        );
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
    }
}
