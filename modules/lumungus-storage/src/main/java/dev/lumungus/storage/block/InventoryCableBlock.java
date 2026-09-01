package dev.lumungus.storage.block;

import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class InventoryCableBlock extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

    private static final VoxelShape CENTER_SHAPE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 6.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(6.0D, 6.0D, 10.0D, 10.0D, 10.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 6.0D, 6.0D, 6.0D, 10.0D, 10.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(10.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    private static final VoxelShape UP_SHAPE = Block.box(6.0D, 10.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape DOWN_SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D);
    private static final int MAX_RENDER_SEARCH_NODES = 2_048;

    public InventoryCableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        for (Direction direction : Direction.values()) {
            state = state.setValue(propertyFor(direction), connectsTo(level, pos, direction));
        }
        return state;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        VoxelShape shape = CENTER_SHAPE;
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_SHAPE);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_SHAPE);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_SHAPE);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_SHAPE);
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_SHAPE);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_SHAPE);
        }
        return shape;
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        return state.setValue(propertyFor(direction), connectsTo(level, pos, direction));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        BlockState connectedState = withConnections(state, level, pos);
        if (connectedState != state) {
            level.setBlock(pos, connectedState, Block.UPDATE_CLIENTS);
        }
        StorageNetworkTopology.invalidateAround(level, pos);
    }

    @Override
    protected void affectNeighborsAfterRemoval(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            boolean movedByPiston
    ) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        StorageNetworkTopology.invalidateAround(level, pos);
    }

    public static boolean connects(BlockState state, Direction direction) {
        return state.getBlock() instanceof InventoryCableBlock && state.getValue(propertyFor(direction));
    }

    public static boolean isPipe(BlockState state) {
        return state.getBlock() == LumungusStorageBlocks.PNEUMATIC_PIPE
                || state.getBlock() == LumungusStorageBlocks.INVENTORY_CABLE;
    }

    public static boolean connectsTo(BlockGetter level, BlockPos pos) {
        return connectsTo(level.getBlockState(pos));
    }

    private static boolean connectsTo(BlockState state) {
        return isPipe(state) || StorageNetworkTopology.isDeviceNode(state.getBlock());
    }

    private static boolean connectsTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos neighbor = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighbor);
        if (StorageNetworkTopology.isDeviceNode(neighborState.getBlock())) {
            return true;
        }
        if (!isPipe(neighborState)) {
            return false;
        }
        return pipeRouteReachesDevice(level, neighbor, pos);
    }

    private static boolean pipeRouteReachesDevice(BlockGetter level, BlockPos start, BlockPos blocked) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        pending.add(start.immutable());
        visited.add(blocked.immutable());

        while (!pending.isEmpty() && visited.size() < MAX_RENDER_SEARCH_NODES) {
            BlockPos current = pending.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor)) {
                    continue;
                }
                BlockState neighborState = level.getBlockState(neighbor);
                if (StorageNetworkTopology.isDeviceNode(neighborState.getBlock())) {
                    return true;
                }
                if (isPipe(neighborState)) {
                    pending.addLast(neighbor.immutable());
                }
            }
        }
        return false;
    }

    private static BooleanProperty propertyFor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    private static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState connectedState = state;
        for (Direction direction : Direction.values()) {
            connectedState = connectedState.setValue(
                    propertyFor(direction),
                    connectsTo(level, pos, direction)
            );
        }
        return connectedState;
    }
}
