package dev.lumungus.storage.network;

import dev.lumungus.storage.block.entity.WirelessStorageControllerBlockEntity;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class WirelessStorageControllerRegistry {
    private static final java.util.Map<Level, Set<BlockPos>> WIRELESS_CONTROLLERS = new WeakHashMap<>();

    private WirelessStorageControllerRegistry() {
    }

    public static void register(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        synchronized (WIRELESS_CONTROLLERS) {
            WIRELESS_CONTROLLERS.computeIfAbsent(level, ignored -> new LinkedHashSet<>()).add(pos.immutable());
        }
    }

    public static void unregister(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        synchronized (WIRELESS_CONTROLLERS) {
            Set<BlockPos> positions = WIRELESS_CONTROLLERS.get(level);
            if (positions != null) {
                positions.remove(pos);
                if (positions.isEmpty()) {
                    WIRELESS_CONTROLLERS.remove(level);
                }
            }
        }
    }

    public static Set<WirelessStorageControllerBlockEntity> loadedControllers(MinecraftServer server) {
        if (server == null) {
            return Set.of();
        }

        LinkedHashSet<WirelessStorageControllerBlockEntity> controllers = new LinkedHashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (BlockPos pos : registeredPositions(level)) {
                if (level.isLoaded(pos)
                        && level.getBlockEntity(pos) instanceof WirelessStorageControllerBlockEntity wireless) {
                    controllers.add(wireless);
                }
            }
        }
        return Collections.unmodifiableSet(controllers);
    }

    private static Set<BlockPos> registeredPositions(Level level) {
        synchronized (WIRELESS_CONTROLLERS) {
            Set<BlockPos> positions = WIRELESS_CONTROLLERS.get(level);
            return positions == null ? Set.of() : Set.copyOf(positions);
        }
    }
}
