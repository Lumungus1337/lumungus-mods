package dev.lumungus.storage.network;

import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class StorageControllerRegistry {
    private static final java.util.Map<Level, Set<BlockPos>> CONTROLLERS = new WeakHashMap<>();

    private StorageControllerRegistry() {
    }

    public static void register(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        synchronized (CONTROLLERS) {
            CONTROLLERS.computeIfAbsent(level, ignored -> new LinkedHashSet<>()).add(pos.immutable());
        }
    }

    public static void unregister(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        synchronized (CONTROLLERS) {
            Set<BlockPos> positions = CONTROLLERS.get(level);
            if (positions != null) {
                positions.remove(pos);
                if (positions.isEmpty()) {
                    CONTROLLERS.remove(level);
                }
            }
        }
    }

    public static Set<StorageControllerBlockEntity> loadedControllers(MinecraftServer server) {
        if (server == null) {
            return Set.of();
        }

        LinkedHashSet<StorageControllerBlockEntity> controllers = new LinkedHashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (BlockPos pos : registeredPositions(level)) {
                if (level.isLoaded(pos)
                        && level.getBlockEntity(pos) instanceof StorageControllerBlockEntity controller) {
                    controllers.add(controller);
                }
            }
        }
        return Collections.unmodifiableSet(controllers);
    }

    private static Set<BlockPos> registeredPositions(Level level) {
        synchronized (CONTROLLERS) {
            Set<BlockPos> positions = CONTROLLERS.get(level);
            return positions == null ? Set.of() : Set.copyOf(positions);
        }
    }
}
