package dev.lumungus.core.api.upgrade;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public final class UpgradeType {
    private final Identifier id;
    private final int maxCount;

    public UpgradeType(Identifier id, int maxCount) {
        if (maxCount < 1) {
            throw new IllegalArgumentException("Upgrade max count must be at least 1");
        }

        this.id = Objects.requireNonNull(id, "id");
        this.maxCount = maxCount;
    }

    public Identifier id() {
        return id;
    }

    public int maxCount() {
        return maxCount;
    }
}
