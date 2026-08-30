package dev.lumungus.integration.toms;

import java.util.Objects;
import net.minecraft.core.BlockPos;

public record TomsDryRunBlock(BlockPos position, TomsBlockPlan plan) {
    public TomsDryRunBlock {
        position = Objects.requireNonNull(position, "position").immutable();
        plan = Objects.requireNonNull(plan, "plan");
    }
}
