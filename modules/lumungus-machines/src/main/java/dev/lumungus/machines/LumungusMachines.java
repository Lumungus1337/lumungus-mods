package dev.lumungus.machines;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LumungusMachines implements ModInitializer {
    public static final String MOD_ID = "lumungus_machines";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Lumungus Machines");
    }
}
