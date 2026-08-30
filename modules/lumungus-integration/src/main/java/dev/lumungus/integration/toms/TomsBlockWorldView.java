package dev.lumungus.integration.toms;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Minimal read-only world view used by Tom's network discovery. */
public interface TomsBlockWorldView {
    boolean isLoaded(BlockPos pos);

    Identifier blockId(BlockPos pos);
}
