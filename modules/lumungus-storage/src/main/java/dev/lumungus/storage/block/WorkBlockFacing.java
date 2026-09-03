package dev.lumungus.storage.block;

import net.minecraft.core.Direction;

public final class WorkBlockFacing {
    private WorkBlockFacing() {
    }

    public static Direction next(Direction direction) {
        return switch (direction) {
            case DOWN -> Direction.NORTH;
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.UP;
            case UP -> Direction.DOWN;
        };
    }

    public static Direction towardPlacementSupport(Direction clickedFace) {
        return clickedFace.getOpposite();
    }
}
