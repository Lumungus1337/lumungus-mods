package dev.lumungus.integration.toms;

import dev.lumungus.core.api.resource.ResourceAmount;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;

public record TomsReadOnlyInventoryEndpoint(BlockPos key, long slotCount, List<ResourceAmount> resources) {
    public TomsReadOnlyInventoryEndpoint {
        key = Objects.requireNonNull(key, "key").immutable();
        if (slotCount < 0) {
            throw new IllegalArgumentException("Slot count must not be negative");
        }
        resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
    }
}
