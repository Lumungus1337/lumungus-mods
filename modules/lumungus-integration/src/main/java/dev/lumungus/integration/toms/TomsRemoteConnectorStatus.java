package dev.lumungus.integration.toms;

public enum TomsRemoteConnectorStatus {
    NONE_CONFIGURED("none_configured"),
    CONFIGURED_RESOLVED("configured_resolved"),
    CONFIGURED_NOT_READY("configured_not_ready"),
    RUNTIME_UNAVAILABLE("runtime_unavailable");

    private final String translationSuffix;

    TomsRemoteConnectorStatus(String translationSuffix) {
        this.translationSuffix = translationSuffix;
    }

    public String messageKey() {
        return "command.lumungus_integration.migration.remote_pending." + translationSuffix;
    }
}
