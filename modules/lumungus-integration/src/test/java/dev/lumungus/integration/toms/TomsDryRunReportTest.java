package dev.lumungus.integration.toms;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

final class TomsDryRunReportTest {
    @Test
    void resolvingOrdinaryCableConnectorsUnlocksAnOtherwiseSafeReport() {
        Identifier connectorId = Identifier.parse("toms_storage:inventory_cable_connector");
        TomsBlockPlan plan = TomsMigrationCatalog.planFor(connectorId).orElseThrow();
        TomsDryRunReport unresolved = new TomsDryRunReport(
                BlockPos.ZERO,
                List.of(new TomsDryRunBlock(BlockPos.ZERO, plan)),
                false,
                false,
                true
        );

        assertFalse(unresolved.safeForInventorySnapshot());
        assertTrue(unresolved.withRemoteConnectionsResolved().safeForInventorySnapshot());
    }
}
