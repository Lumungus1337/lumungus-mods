package dev.lumungus.integration.toms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

/** Discovers one physically connected Tom's block component without loading chunks or mutating the world. */
public final class TomsReadOnlyNetworkScanner {
    private TomsReadOnlyNetworkScanner() {
    }

    public static TomsDryRunReport scan(TomsBlockWorldView world, BlockPos start, int maxNodes) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(start, "start");
        if (maxNodes <= 0) {
            throw new IllegalArgumentException("Node limit must be positive");
        }
        if (!world.isLoaded(start)) {
            return new TomsDryRunReport(start, List.of(), true, false);
        }
        if (TomsMigrationCatalog.planFor(world.blockId(start)).isEmpty()) {
            return new TomsDryRunReport(start, List.of(), false, false);
        }

        Queue<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        List<TomsDryRunBlock> discovered = new ArrayList<>();
        boolean unloadedBoundary = false;
        boolean nodeLimitReached = false;
        pending.add(start.immutable());

        while (!pending.isEmpty()) {
            if (discovered.size() >= maxNodes) {
                nodeLimitReached = true;
                break;
            }
            BlockPos current = pending.remove();
            if (!visited.add(current)) {
                continue;
            }
            if (!world.isLoaded(current)) {
                unloadedBoundary = true;
                continue;
            }
            Identifier blockId = world.blockId(current);
            TomsBlockPlan plan = TomsMigrationCatalog.planFor(blockId).orElse(null);
            if (plan == null) {
                continue;
            }
            discovered.add(new TomsDryRunBlock(current, plan));

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction).immutable();
                if (visited.contains(neighbor)) {
                    continue;
                }
                if (!world.isLoaded(neighbor)) {
                    unloadedBoundary = true;
                } else if (TomsMigrationCatalog.planFor(world.blockId(neighbor)).isPresent()) {
                    pending.add(neighbor);
                }
            }
        }

        return new TomsDryRunReport(start, discovered, unloadedBoundary, nodeLimitReached);
    }
}
