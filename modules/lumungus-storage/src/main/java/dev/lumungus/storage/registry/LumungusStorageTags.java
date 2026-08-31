package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class LumungusStorageTags {
    public static final TagKey<Block> WRENCH_REMOVABLE = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, "wrench_removable")
    );

    private LumungusStorageTags() {
    }
}
