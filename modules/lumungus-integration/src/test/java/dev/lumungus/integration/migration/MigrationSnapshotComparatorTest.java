package dev.lumungus.integration.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.integration.test.MinecraftTestBootstrap;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class MigrationSnapshotComparatorTest {
    @BeforeAll
    static void initializeMinecraft() {
        MinecraftTestBootstrap.initialize();
    }

    @Test
    void reportsAnExactReadOnlyMatch() {
        MigrationInventorySnapshot before = snapshot(
                "Tom's before",
                42,
                1_134,
                new ResourceAmount(stack(Items.COBBLESTONE), 7_000_000)
        );
        MigrationInventorySnapshot after = snapshot(
                "Lumungus after",
                42,
                1_134,
                new ResourceAmount(stack(Items.COBBLESTONE), 7_000_000)
        );

        MigrationComparisonReport report = MigrationSnapshotComparator.compare(before, after);

        assertTrue(report.exactMatch());
        assertEquals(0, report.missingTotal());
        assertEquals(0, report.excessTotal());
        assertTrue(report.itemDeltas().isEmpty());
    }

    @Test
    void reportsMissingExcessAndMetadataDifferences() {
        MigrationInventorySnapshot before = snapshot(
                "Tom's before",
                3,
                81,
                new ResourceAmount(stack(Items.COBBLESTONE), 100),
                new ResourceAmount(stack(Items.OAK_LOG), 20)
        );
        MigrationInventorySnapshot after = snapshot(
                "Lumungus after",
                2,
                54,
                new ResourceAmount(stack(Items.COBBLESTONE), 90),
                new ResourceAmount(stack(Items.IRON_INGOT), 7)
        );

        MigrationComparisonReport report = MigrationSnapshotComparator.compare(before, after);

        assertFalse(report.exactMatch());
        assertFalse(report.metadataMatches());
        assertFalse(report.contentsMatch());
        assertEquals(30, report.missingTotal());
        assertEquals(7, report.excessTotal());
        assertEquals(3, report.itemDeltas().size());
    }

    @Test
    void keepsDifferentItemComponentsSeparate() {
        ItemStack namedCobblestone = stack(Items.COBBLESTONE);
        namedCobblestone.set(DataComponents.CUSTOM_NAME, Component.literal("Reserved"));
        MigrationInventorySnapshot before = snapshot(
                "Tom's before",
                1,
                27,
                new ResourceAmount(stack(Items.COBBLESTONE), 64),
                new ResourceAmount(namedCobblestone, 5)
        );
        MigrationInventorySnapshot after = snapshot(
                "Lumungus after",
                1,
                27,
                new ResourceAmount(stack(Items.COBBLESTONE), 69)
        );

        MigrationComparisonReport report = MigrationSnapshotComparator.compare(before, after);

        assertEquals(2, report.itemDeltas().size());
        assertEquals(5, report.missingTotal());
        assertEquals(5, report.excessTotal());
    }

    private static MigrationInventorySnapshot snapshot(
            String source,
            int endpoints,
            long slots,
            ResourceAmount... resources
    ) {
        return ReadOnlyInventorySnapshotter.capture(source, endpoints, slots, List.of(resources));
    }

    private static ItemStack stack(net.minecraft.world.item.Item item) {
        return MinecraftTestBootstrap.stack(item);
    }
}
