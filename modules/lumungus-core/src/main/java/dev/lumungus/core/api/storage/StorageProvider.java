package dev.lumungus.core.api.storage;

/** Exposes storage without tying discovery to an implementation. */
@FunctionalInterface
public interface StorageProvider {
    StorageAccess storageAccess();
}
