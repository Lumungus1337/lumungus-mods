package dev.lumungus.core.api.production;

import java.util.List;

public interface ProductionMachine {
    boolean submit(ProductionRequest request);

    List<ProductionProgress> productionJobs();
}
