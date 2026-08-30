package dev.lumungus.storage.block.entity;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;

final class StorageControllerOwnership {
    private StorageControllerOwnership() {
    }

    static Optional<BlockPos> ownerOf(
            BlockPos driveBayPos,
            Collection<BlockPos> controllerPositions,
            int scanRadius
    ) {
        return controllerPositions.stream()
                .filter(controllerPos -> insideScanCube(driveBayPos, controllerPos, scanRadius))
                .min(Comparator
                        .comparingLong((BlockPos controllerPos) -> distanceSquared(driveBayPos, controllerPos))
                        .thenComparingInt(BlockPos::getY)
                        .thenComparingInt(BlockPos::getZ)
                        .thenComparingInt(BlockPos::getX));
    }

    private static boolean insideScanCube(BlockPos first, BlockPos second, int scanRadius) {
        return Math.abs(first.getX() - (long) second.getX()) <= scanRadius
                && Math.abs(first.getY() - (long) second.getY()) <= scanRadius
                && Math.abs(first.getZ() - (long) second.getZ()) <= scanRadius;
    }

    private static long distanceSquared(BlockPos first, BlockPos second) {
        long x = first.getX() - (long) second.getX();
        long y = first.getY() - (long) second.getY();
        long z = first.getZ() - (long) second.getZ();
        return x * x + y * y + z * z;
    }
}
