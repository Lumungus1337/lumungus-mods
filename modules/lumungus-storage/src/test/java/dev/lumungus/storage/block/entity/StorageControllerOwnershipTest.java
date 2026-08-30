package dev.lumungus.storage.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StorageControllerOwnershipTest {
    @Test
    void assignsDriveBayToTheNearestController() {
        BlockPos driveBay = new BlockPos(5, 0, 0);
        BlockPos near = new BlockPos(3, 0, 0);
        BlockPos far = new BlockPos(-2, 0, 0);

        assertEquals(near, StorageControllerOwnership.ownerOf(
                driveBay,
                List.of(far, near),
                StorageControllerBlockEntity.SCAN_RADIUS
        ).orElseThrow());
    }

    @Test
    void resolvesEqualDistanceOwnershipDeterministically() {
        BlockPos driveBay = new BlockPos(0, 0, 0);
        BlockPos lowerX = new BlockPos(-2, 0, 0);
        BlockPos higherX = new BlockPos(2, 0, 0);

        assertEquals(lowerX, StorageControllerOwnership.ownerOf(
                driveBay,
                List.of(higherX, lowerX),
                StorageControllerBlockEntity.SCAN_RADIUS
        ).orElseThrow());
    }

    @Test
    void ignoresControllersOutsideTheScanCube() {
        assertTrue(StorageControllerOwnership.ownerOf(
                BlockPos.ZERO,
                List.of(new BlockPos(StorageControllerBlockEntity.SCAN_RADIUS + 1, 0, 0)),
                StorageControllerBlockEntity.SCAN_RADIUS
        ).isEmpty());
    }
}

