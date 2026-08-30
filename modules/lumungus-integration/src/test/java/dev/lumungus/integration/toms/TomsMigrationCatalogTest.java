package dev.lumungus.integration.toms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

final class TomsMigrationCatalogTest {
    private static final Set<String> REGISTERED_BLOCKS_2_11_3 = Set.of(
            "inventory_connector", "storage_terminal", "crafting_terminal", "open_crate", "trim",
            "painted_trim", "level_emitter", "inventory_cable", "inventory_cable_framed",
            "basic_inventory_hopper", "inventory_interface", "filing_cabinet", "inventory_cable_connector",
            "inventory_cable_connector_framed", "inventory_proxy"
    );

    @Test
    void categorizesEveryBlockRegisteredByToms2113() {
        assertEquals(REGISTERED_BLOCKS_2_11_3.size(), TomsMigrationCatalog.knownPlans().size());
        for (String path : REGISTERED_BLOCKS_2_11_3) {
            assertTrue(TomsMigrationCatalog.planFor(toms(path)).isPresent(), path);
        }
    }

    @Test
    void mapsCentralConnectorAndTrimToTransitionSafeLumungusBlocks() {
        TomsBlockPlan connector = TomsMigrationCatalog.planFor(toms("inventory_connector")).orElseThrow();
        TomsBlockPlan trim = TomsMigrationCatalog.planFor(toms("trim")).orElseThrow();

        assertEquals(TomsMigrationDisposition.CONVERTIBLE, connector.disposition());
        assertEquals(lumungus("storage_controller"), connector.replacementId().orElseThrow());
        assertEquals(lumungus("inventory_trim"), trim.replacementId().orElseThrow());
    }

    @Test
    void blocksUnknownTomsBlocksAndFilingCabinets() {
        assertEquals(
                TomsMigrationDisposition.BLOCKED_UNSUPPORTED,
                TomsMigrationCatalog.planFor(toms("future_block")).orElseThrow().disposition()
        );
        assertEquals(
                TomsMigrationDisposition.BLOCKED_INVENTORY_CONTENTS,
                TomsMigrationCatalog.planFor(toms("filing_cabinet")).orElseThrow().disposition()
        );
    }

    @Test
    void ignoresBlocksFromOtherMods() {
        assertFalse(TomsMigrationCatalog.planFor(Identifier.parse("minecraft:chest")).isPresent());
    }

    private static Identifier toms(String path) {
        return Identifier.fromNamespaceAndPath("toms_storage", path);
    }

    private static Identifier lumungus(String path) {
        return Identifier.fromNamespaceAndPath("lumungus_storage", path);
    }
}
