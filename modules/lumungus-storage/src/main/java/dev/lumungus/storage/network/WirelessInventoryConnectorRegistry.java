package dev.lumungus.storage.network;

import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.block.entity.WirelessInventoryConnectorBlockEntity;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class WirelessInventoryConnectorRegistry {
    private static final java.util.Map<Level, Set<BlockPos>> CONNECTORS = new WeakHashMap<>();

    private WirelessInventoryConnectorRegistry() {
    }

    public static void register(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        synchronized (CONNECTORS) {
            CONNECTORS.computeIfAbsent(level, ignored -> new LinkedHashSet<>()).add(pos.immutable());
        }
    }

    public static void unregister(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        synchronized (CONNECTORS) {
            Set<BlockPos> positions = CONNECTORS.get(level);
            if (positions != null) {
                positions.remove(pos);
                if (positions.isEmpty()) {
                    CONNECTORS.remove(level);
                }
            }
        }
    }

    public static Set<WirelessInventoryConnectorBlockEntity> linkedTo(StorageControllerBlockEntity controller) {
        if (controller.getLevel() == null || controller.getLevel().isClientSide()) {
            return Set.of();
        }

        MinecraftServer server = controller.getLevel().getServer();
        if (server == null) {
            return Set.of();
        }

        LinkedHashSet<WirelessInventoryConnectorBlockEntity> linked = new LinkedHashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (BlockPos pos : registeredPositions(level)) {
                if (level.isLoaded(pos)
                        && level.getBlockEntity(pos) instanceof WirelessInventoryConnectorBlockEntity connector
                        && connector.refreshControllerLink()
                        && connector.isLinkedTo(controller)) {
                    linked.add(connector);
                }
            }
        }
        return Collections.unmodifiableSet(linked);
    }

    private static Set<BlockPos> registeredPositions(Level level) {
        synchronized (CONNECTORS) {
            Set<BlockPos> positions = CONNECTORS.get(level);
            return positions == null ? Set.of() : Set.copyOf(positions);
        }
    }
}
