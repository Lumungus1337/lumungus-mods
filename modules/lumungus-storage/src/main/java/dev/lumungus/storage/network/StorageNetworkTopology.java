package dev.lumungus.storage.network;

import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Discovers loaded Lumungus network nodes without forcing chunks to load. */
public final class StorageNetworkTopology {
    private static final int MAX_VISITED_NODES = 65_536;

    private StorageNetworkTopology() {
    }

    public static Set<BlockPos> connectedNodes(Level level, BlockPos origin) {
        LinkedHashSet<BlockPos> visited = new LinkedHashSet<>();
        if (!level.isLoaded(origin) || !isNetworkNode(level, origin)) {
            return Set.of();
        }

        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        BlockPos start = origin.immutable();
        visited.add(start);
        pending.add(start);

        while (!pending.isEmpty() && visited.size() < MAX_VISITED_NODES) {
            BlockPos current = pending.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (level.isLoaded(neighbor) && isNetworkNode(level, neighbor)) {
                    BlockPos stableNeighbor = neighbor.immutable();
                    if (visited.add(stableNeighbor)) {
                        pending.addLast(stableNeighbor);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(visited);
    }

    public static Set<BlockPos> reachableNodes(Level level, BlockPos origin, int fallbackRadius) {
        LinkedHashSet<BlockPos> reachable = new LinkedHashSet<>(connectedNodes(level, origin));
        BlockPos min = origin.offset(-fallbackRadius, -fallbackRadius, -fallbackRadius);
        BlockPos max = origin.offset(fallbackRadius, fallbackRadius, fallbackRadius);
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.isLoaded(candidate)) {
                reachable.add(candidate.immutable());
            }
        }
        return Collections.unmodifiableSet(reachable);
    }

    public static Set<BlockPos> reachableControllers(Level level, BlockPos origin, int fallbackRadius) {
        LinkedHashSet<BlockPos> controllers = new LinkedHashSet<>();
        for (BlockPos candidate : reachableNodes(level, origin, fallbackRadius)) {
            if (level.getBlockEntity(candidate) instanceof StorageControllerBlockEntity) {
                controllers.add(candidate);
            }
        }
        return Collections.unmodifiableSet(controllers);
    }

    public static boolean canReach(Level level, BlockPos origin, BlockPos target, int fallbackRadius) {
        return insideCube(origin, target, fallbackRadius) || connectedNodes(level, origin).contains(target);
    }

    private static boolean isNetworkNode(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block == LumungusStorageBlocks.STORAGE_CONTROLLER
                || block == LumungusStorageBlocks.CRAFTING_TERMINAL
                || block == LumungusStorageBlocks.DRIVE_BAY
                || block == LumungusStorageBlocks.INVENTORY_CONNECTOR
                || block == LumungusStorageBlocks.INVENTORY_CABLE;
    }

    private static boolean insideCube(BlockPos first, BlockPos second, int radius) {
        return Math.abs(first.getX() - (long) second.getX()) <= radius
                && Math.abs(first.getY() - (long) second.getY()) <= radius
                && Math.abs(first.getZ() - (long) second.getZ()) <= radius;
    }
}
