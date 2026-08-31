package dev.lumungus.storage.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

final class WorkBlockFacingTest {
    @Test
    void nextCyclesThroughEveryDirection() {
        Direction direction = Direction.DOWN;
        direction = WorkBlockFacing.next(direction);
        assertEquals(Direction.NORTH, direction);
        direction = WorkBlockFacing.next(direction);
        assertEquals(Direction.EAST, direction);
        direction = WorkBlockFacing.next(direction);
        assertEquals(Direction.SOUTH, direction);
        direction = WorkBlockFacing.next(direction);
        assertEquals(Direction.WEST, direction);
        direction = WorkBlockFacing.next(direction);
        assertEquals(Direction.UP, direction);
        direction = WorkBlockFacing.next(direction);
        assertEquals(Direction.DOWN, direction);
    }
}
