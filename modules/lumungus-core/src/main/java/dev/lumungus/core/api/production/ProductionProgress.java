package dev.lumungus.core.api.production;

import dev.lumungus.core.api.resource.ResourceAmount;
import java.util.List;
import java.util.Objects;

public final class ProductionProgress {
    private final ProductionRequest request;
    private final long completedAmount;
    private final ProductionStatus status;
    private final List<ResourceAmount> missingResources;

    public ProductionProgress(
            ProductionRequest request,
            long completedAmount,
            ProductionStatus status,
            List<ResourceAmount> missingResources
    ) {
        this.request = Objects.requireNonNull(request, "request");
        if (completedAmount < 0 || completedAmount > request.requestedAmount()) {
            throw new IllegalArgumentException("Completed amount must be within the requested amount");
        }
        this.status = Objects.requireNonNull(status, "status");
        if (status == ProductionStatus.COMPLETE && completedAmount != request.requestedAmount()) {
            throw new IllegalArgumentException("A complete production job must satisfy its requested amount");
        }
        this.completedAmount = completedAmount;
        this.missingResources = List.copyOf(Objects.requireNonNull(missingResources, "missingResources"));
    }

    public static ProductionProgress planned(ProductionRequest request) {
        return new ProductionProgress(request, 0, ProductionStatus.PLANNED, List.of());
    }

    public ProductionRequest request() {
        return request;
    }

    public long completedAmount() {
        return completedAmount;
    }

    public long remainingAmount() {
        return request.requestedAmount() - completedAmount;
    }

    public ProductionStatus status() {
        return status;
    }

    public List<ResourceAmount> missingResources() {
        return missingResources;
    }
}
