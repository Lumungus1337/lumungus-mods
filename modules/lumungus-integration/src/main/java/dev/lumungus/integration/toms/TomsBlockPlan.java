package dev.lumungus.integration.toms;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record TomsBlockPlan(
        Identifier sourceId,
        TomsMigrationDisposition disposition,
        Optional<Identifier> replacementId,
        String reason
) {
    public TomsBlockPlan {
        sourceId = Objects.requireNonNull(sourceId, "sourceId");
        disposition = Objects.requireNonNull(disposition, "disposition");
        replacementId = Objects.requireNonNull(replacementId, "replacementId");
        reason = Objects.requireNonNull(reason, "reason").trim();
        if (reason.isEmpty()) {
            throw new IllegalArgumentException("Migration reason must not be blank");
        }
        if ((disposition == TomsMigrationDisposition.CONVERTIBLE) != replacementId.isPresent()) {
            throw new IllegalArgumentException("Only convertible blocks must define a replacement");
        }
    }
}
