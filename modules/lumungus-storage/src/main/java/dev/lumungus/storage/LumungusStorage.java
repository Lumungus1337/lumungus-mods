package dev.lumungus.storage;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LumungusStorage implements ModInitializer {
    public static final String MOD_ID = "lumungus_storage";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Lumungus Storage");
    }
}
