package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.item.CopperWrenchItem;
import dev.lumungus.storage.network.StorageControllerRegistry;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class StorageControllerBlock extends BaseEntityBlock {
    public static final MapCodec<StorageControllerBlock> CODEC = simpleCodec(StorageControllerBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public StorageControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageControllerBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> blockEntityType
    ) {
        return level.isClientSide()
                ? null
                : createTickerHelper(
                        blockEntityType,
                        dev.lumungus.storage.registry.LumungusStorageBlockEntities.STORAGE_CONTROLLER,
                        StorageControllerBlockEntity::serverTick
                );
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        StorageControllerRegistry.register(level, pos);
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
        StorageControllerRegistry.unregister(level, pos);
        StorageNetworkTopology.invalidateAround(level, pos);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (heldStack.is(LumungusStorageItems.COPPER_WRENCH)) {
            if (player.isSecondaryUseActive()) {
                if (!level.isClientSide()) {
                    Direction nextFacing = state.getValue(FACING).getClockWise();
                    level.setBlock(pos, state.setValue(FACING, nextFacing), 3);
                    player.sendSystemMessage(Component.translatable(
                            "message.lumungus_storage.work_block.facing",
                            Component.translatable("direction.minecraft." + nextFacing.getName())
                    ));
                }
                return InteractionResult.SUCCESS;
            }
            return CopperWrenchItem.dismantle(heldStack, level, pos, player);
        }
        if (heldStack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)) {
            if (!level.isClientSide()
                    && level.getBlockEntity(pos) instanceof StorageControllerBlockEntity controller) {
                heldStack.set(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER, new BoundStorageController(
                        level.dimension().identifier(),
                        pos.immutable(),
                        controller.getNetworkId()
                ));
                player.sendSystemMessage(Component.translatable(
                        "message.lumungus_storage.wireless_module.primed",
                        controller.getNetworkLabel()
                ));
            }
            return InteractionResult.SUCCESS;
        }

        return useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StorageControllerBlockEntity controller) {
            StorageControllerRegistry.register(level, pos);
            StorageControllerBlockEntity.NetworkStatus status = controller.refreshNetwork();
            long used = controller.snapshot().storedTotalAmount();
            long capacity = controller.capacity().maxTotalAmount();
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.storage_controller.status",
                    controller.getNetworkLabel(),
                    status.linkedTerminals(),
                    status.linkedDriveBays(),
                    status.linkedInventoryConnectors(),
                    used,
                    capacity
            ));
        }

        return InteractionResult.SUCCESS;
    }
}
