package dev.lumungus.core.api.upgrade;

import java.util.Objects;
import java.util.function.Predicate;

public final class UpgradeSlot {
    private final String key;
    private final Predicate<UpgradeType> validator;

    public UpgradeSlot(String key, Predicate<UpgradeType> validator) {
        if (key.isBlank()) {
            throw new IllegalArgumentException("Upgrade slot key must not be blank");
        }

        this.key = key;
        this.validator = Objects.requireNonNull(validator, "validator");
    }

    public String key() {
        return key;
    }

    public boolean accepts(UpgradeType type) {
        return validator.test(type);
    }
}
