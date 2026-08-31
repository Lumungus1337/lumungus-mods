package dev.lumungus.integration.toms;

public enum TomsMigrationDisposition {
    CONVERTIBLE(true, true),
    READ_ONLY_SUPPORTED(false, true),
    BLOCKED_INVENTORY_CONTENTS(false, false),
    BLOCKED_UNSUPPORTED(false, false);

    private final boolean convertible;
    private final boolean safeForInventorySnapshot;

    TomsMigrationDisposition(boolean convertible, boolean safeForInventorySnapshot) {
        this.convertible = convertible;
        this.safeForInventorySnapshot = safeForInventorySnapshot;
    }

    public boolean convertible() {
        return convertible;
    }

    public boolean safeForInventorySnapshot() {
        return safeForInventorySnapshot;
    }
}
