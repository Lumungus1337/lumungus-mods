package dev.lumungus.storage;

import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.network.TerminalNetworking;
import dev.lumungus.storage.registry.LumungusStorageMenus;
import net.fabricmc.api.ModInitializer;
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
        TerminalNetworking.register();
        LOGGER.info("Initializing Lumungus Storage");
    }
}
