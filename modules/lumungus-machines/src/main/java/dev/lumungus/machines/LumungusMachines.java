package dev.lumungus.machines;

import dev.lumungus.machines.registry.LumungusMachinesBlockEntities;
import dev.lumungus.machines.registry.LumungusMachinesBlocks;
import dev.lumungus.machines.registry.LumungusMachinesCreativeTabs;
import dev.lumungus.machines.registry.LumungusMachinesMenus;
import dev.lumungus.machines.network.AutocrafterNetworking;
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
        LumungusMachinesMenus.register();
        LumungusMachinesCreativeTabs.register();
        AutocrafterNetworking.register();
        LOGGER.info("Initializing Lumungus Machines");
    }
}
