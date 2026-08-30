package dev.lumungus.integration.toms;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Version-independent dry-run catalog based only on stable registry identifiers. */
public final class TomsMigrationCatalog {
    public static final String TOMS_NAMESPACE = "toms_storage";

    private static final Map<Identifier, TomsBlockPlan> KNOWN_PLANS = createKnownPlans();

    private TomsMigrationCatalog() {
    }

    public static Optional<TomsBlockPlan> planFor(Identifier blockId) {
        if (!TOMS_NAMESPACE.equals(blockId.getNamespace())) {
            return Optional.empty();
        }
        return Optional.of(KNOWN_PLANS.getOrDefault(
                blockId,
                blocked(blockId, TomsMigrationDisposition.BLOCKED_UNSUPPORTED, "Unknown Tom's block ID")
        ));
    }

    public static Map<Identifier, TomsBlockPlan> knownPlans() {
        return KNOWN_PLANS;
    }

    private static Map<Identifier, TomsBlockPlan> createKnownPlans() {
        Map<Identifier, TomsBlockPlan> plans = new LinkedHashMap<>();
        convertible(plans, "inventory_connector", "storage_controller", "Central network entry point");
        convertible(plans, "storage_terminal", "crafting_terminal", "Lumungus terminal includes crafting");
        convertible(plans, "crafting_terminal", "crafting_terminal", "Crafting terminal replacement");
        convertible(plans, "trim", "inventory_trim", "Trim keeps adjacent inventories connected");
        convertible(plans, "painted_trim", "inventory_trim", "Paint data requires journal preservation");
        convertible(plans, "inventory_cable", "inventory_cable", "Network cable replacement");
        convertible(plans, "inventory_cable_framed", "inventory_cable", "Frame data requires journal preservation");
        convertible(plans, "inventory_cable_connector", "inventory_connector", "Single-inventory connector");
        convertible(plans, "inventory_cable_connector_framed", "inventory_connector", "Framed connector");

        block(plans, "filing_cabinet", TomsMigrationDisposition.BLOCKED_INVENTORY_CONTENTS,
                "Filing Cabinet can contain items and needs a dedicated content migration");
        block(plans, "open_crate", TomsMigrationDisposition.BLOCKED_UNSUPPORTED, "No Lumungus equivalent yet");
        block(plans, "level_emitter", TomsMigrationDisposition.BLOCKED_UNSUPPORTED, "No Lumungus equivalent yet");
        block(plans, "basic_inventory_hopper", TomsMigrationDisposition.BLOCKED_UNSUPPORTED,
                "No Lumungus equivalent yet");
        block(plans, "inventory_interface", TomsMigrationDisposition.BLOCKED_UNSUPPORTED,
                "Automation behavior needs a dedicated adapter");
        block(plans, "inventory_proxy", TomsMigrationDisposition.BLOCKED_UNSUPPORTED,
                "Proxy direction and target must be preserved explicitly");
        return Map.copyOf(plans);
    }

    private static void convertible(Map<Identifier, TomsBlockPlan> plans, String source, String target, String reason) {
        Identifier sourceId = toms(source);
        plans.put(sourceId, new TomsBlockPlan(
                sourceId,
                TomsMigrationDisposition.CONVERTIBLE,
                Optional.of(lumungus(target)),
                reason
        ));
    }

    private static void block(
            Map<Identifier, TomsBlockPlan> plans,
            String source,
            TomsMigrationDisposition disposition,
            String reason
    ) {
        Identifier sourceId = toms(source);
        plans.put(sourceId, blocked(sourceId, disposition, reason));
    }

    private static TomsBlockPlan blocked(
            Identifier sourceId,
            TomsMigrationDisposition disposition,
            String reason
    ) {
        return new TomsBlockPlan(sourceId, disposition, Optional.empty(), reason);
    }

    private static Identifier toms(String path) {
        return Identifier.fromNamespaceAndPath(TOMS_NAMESPACE, path);
    }

    private static Identifier lumungus(String path) {
        return Identifier.fromNamespaceAndPath("lumungus_storage", path);
    }
}
