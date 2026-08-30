package dev.lumungus.core.api.storage;

import dev.lumungus.core.api.inventory.ItemTransferAccess;

/** Transfer access with capacity and occupancy information. */
public interface StorageAccess extends ItemTransferAccess {
    StorageCapacity capacity();

    StorageSnapshot snapshot();
}
