package dev.lumungus.machines.production;

import dev.lumungus.core.api.production.ProductionProgress;
import dev.lumungus.core.api.production.ProductionRequest;
import dev.lumungus.core.api.production.ProductionStatus;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProductionQueueTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void processesAQuantityJobThroughItsValidLifecycle() {
        ProductionQueue queue = new ProductionQueue(4);
        ProductionRequest request = request(Items.BARREL, 120);

        assertTrue(queue.submit(request));
        assertTrue(queue.update(request.id(), 0, ProductionStatus.WAITING_FOR_MATERIAL, List.of()));
        assertTrue(queue.update(request.id(), 48, ProductionStatus.IN_PROGRESS, List.of()));
        assertTrue(queue.update(request.id(), 120, ProductionStatus.COMPLETE, List.of()));

        ProductionProgress progress = queue.productionJobs().getFirst();
        assertEquals(120, progress.completedAmount());
        assertEquals(0, progress.remainingAmount());
        assertTrue(queue.removeTerminal(request.id()));
        assertTrue(queue.productionJobs().isEmpty());
    }

    @Test
    void rejectsDuplicatesCapacityOverflowAndBackwardProgress() {
        ProductionQueue queue = new ProductionQueue(1);
        ProductionRequest barrels = request(Items.BARREL, 120);
        ProductionRequest chests = request(Items.CHEST, 20);

        assertTrue(queue.submit(barrels));
        assertFalse(queue.submit(barrels));
        assertFalse(queue.submit(chests));
        assertTrue(queue.update(barrels.id(), 40, ProductionStatus.IN_PROGRESS, List.of()));
        assertFalse(queue.update(barrels.id(), 39, ProductionStatus.IN_PROGRESS, List.of()));
        assertFalse(queue.update(barrels.id(), 40, ProductionStatus.PLANNED, List.of()));
        assertFalse(queue.update(barrels.id(), 119, ProductionStatus.COMPLETE, List.of()));
    }

    @Test
    void terminalJobsFreeCapacityButRemainVisibleUntilAcknowledged() {
        ProductionQueue queue = new ProductionQueue(1);
        ProductionRequest first = request(Items.BARREL, 1);
        ProductionRequest second = request(Items.CHEST, 1);

        assertTrue(queue.submit(first));
        assertTrue(queue.update(first.id(), 0, ProductionStatus.BLOCKED, List.of()));
        assertEquals(0, queue.activeJobCount());
        assertTrue(queue.submit(second));
        assertEquals(List.of(first.id(), second.id()), queue.productionJobs().stream()
                .map(progress -> progress.request().id())
                .toList());
    }

    private static ProductionRequest request(Item item, long amount) {
        Holder.Reference<Item> holder = item.builtInRegistryHolder();
        if (!holder.areComponentsBound()) {
            holder.bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 64)
                    .build());
        }
        return new ProductionRequest(UUID.randomUUID(), new ItemStack(holder, 1), amount, Set.of());
    }
}
