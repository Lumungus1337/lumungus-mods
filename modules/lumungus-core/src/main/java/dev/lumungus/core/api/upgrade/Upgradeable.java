package dev.lumungus.core.api.upgrade;

import java.util.List;

public interface Upgradeable {
    List<UpgradeSlot> upgradeSlots();

    int installedCount(UpgradeType type);

    default boolean canInstall(UpgradeType type, UpgradeSlot slot) {
        return slot.accepts(type) && installedCount(type) < type.maxCount();
    }
}
