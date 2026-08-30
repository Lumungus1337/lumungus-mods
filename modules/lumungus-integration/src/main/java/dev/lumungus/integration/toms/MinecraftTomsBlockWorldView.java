package dev.lumungus.integration.toms;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public final class MinecraftTomsBlockWorldView implements TomsBlockWorldView {
    private final Level level;

    public MinecraftTomsBlockWorldView(Level level) {
        this.level = Objects.requireNonNull(level, "level");
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return level.isLoaded(pos);
    }

    @Override
    public Identifier blockId(BlockPos pos) {
        if (!isLoaded(pos)) {
            throw new IllegalArgumentException("Cannot inspect an unloaded position");
        }
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
    }
}
