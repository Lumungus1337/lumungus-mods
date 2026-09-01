package dev.lumungus.machines;

import dev.lumungus.machines.registry.LumungusMachinesBlockEntities;
import dev.lumungus.machines.registry.LumungusMachinesBlocks;
import dev.lumungus.machines.registry.LumungusMachinesCreativeTabs;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LumungusMachines implements ModInitializer {
    public static final String MOD_ID = "lumungus_machines";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LumungusMachinesBlocks.register();
        LumungusMachinesBlockEntities.register();
        LumungusMachinesCreativeTabs.register();
        LOGGER.info("Initializing Lumungus Machines");
    }
}
