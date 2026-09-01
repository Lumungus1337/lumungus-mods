package dev.lumungus.machines.production;

public enum AutocrafterState {
    IDLE,
    PAUSED,
    NO_MODULE,
    NO_CONTROLLER,
    NO_RECIPE,
    MISSING_INGREDIENTS,
    OUTPUT_BLOCKED,
    INVALID_TARGET_AMOUNT,
    WORKING,
    COMPLETE;

    public String translationKey() {
        return "message.lumungus_machines.autocrafter.state." + name().toLowerCase(java.util.Locale.ROOT);
    }
}
