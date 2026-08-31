package dev.lumungus.storage;

import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import dev.lumungus.storage.registry.LumungusStorageCreativeTabs;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.network.TerminalNetworking;
import dev.lumungus.storage.registry.LumungusStorageMenus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LumungusStorage implements ModInitializer {
    public static final String MOD_ID = "lumungus_storage";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LumungusStorageDataComponents.register();
        LumungusStorageItems.register();
        LumungusStorageBlocks.register();
        LumungusStorageBlockEntities.register();
        LumungusStorageMenus.register();
        LumungusStorageCreativeTabs.register();
        TerminalNetworking.register();
        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> StorageNetworkTopology.invalidate(level));
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> StorageNetworkTopology.invalidate(level));
        LOGGER.info("Initializing Lumungus Storage");
    }
}
