package dev.lumungus.storage.inventory;

import dev.lumungus.core.api.storage.StorageAccess;
import java.util.Objects;
import net.minecraft.core.BlockPos;

/** One physical inventory with a stable key used to prevent duplicate network views. */
public record PhysicalInventoryEndpoint(BlockPos key, StorageAccess access) {
    public PhysicalInventoryEndpoint {
        key = Objects.requireNonNull(key, "key").immutable();
        access = Objects.requireNonNull(access, "access");
    }
}
