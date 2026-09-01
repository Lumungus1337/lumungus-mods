package dev.lumungus.machines.production;

import dev.lumungus.core.api.production.ProductionMachine;
import dev.lumungus.core.api.production.ProductionProgress;
import dev.lumungus.core.api.production.ProductionRequest;
import dev.lumungus.core.api.production.ProductionStatus;
import dev.lumungus.core.api.resource.ResourceAmount;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ProductionQueue implements ProductionMachine {
    private final int capacity;
    private final Map<UUID, ProductionProgress> jobs = new LinkedHashMap<>();

    public ProductionQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Production queue capacity must be positive");
        }
        this.capacity = capacity;
    }

    @Override
    public synchronized boolean submit(ProductionRequest request) {
        Objects.requireNonNull(request, "request");
        if (jobs.containsKey(request.id()) || activeJobCount() >= capacity) {
            return false;
        }
        jobs.put(request.id(), ProductionProgress.planned(request));
        return true;
    }

    @Override
    public synchronized List<ProductionProgress> productionJobs() {
        return List.copyOf(jobs.values());
    }

    public synchronized boolean update(
            UUID jobId,
            long completedAmount,
            ProductionStatus status,
            List<ResourceAmount> missingResources
    ) {
        ProductionProgress current = jobs.get(jobId);
        if (current == null
                || completedAmount < current.completedAmount()
                || !current.status().canTransitionTo(status)) {
            return false;
        }

        ProductionProgress updated;
        try {
            updated = new ProductionProgress(
                    current.request(),
                    completedAmount,
                    status,
                    missingResources
            );
        } catch (IllegalArgumentException exception) {
            return false;
        }
        jobs.put(jobId, updated);
        return true;
    }

    public synchronized boolean removeTerminal(UUID jobId) {
        ProductionProgress progress = jobs.get(jobId);
        if (progress == null || !progress.status().isTerminal()) {
            return false;
        }
        jobs.remove(jobId);
        return true;
    }

    public synchronized int activeJobCount() {
        return (int) jobs.values().stream()
                .filter(progress -> !progress.status().isTerminal())
                .count();
    }
}
