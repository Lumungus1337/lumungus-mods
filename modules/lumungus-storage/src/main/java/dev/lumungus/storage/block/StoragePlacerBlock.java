package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.storage.block.entity.StoragePlacerBlockEntity;
import dev.lumungus.storage.item.CopperWrenchItem;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.wireless.WirelessModuleInteractions;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class StoragePlacerBlock extends BaseEntityBlock {
    public static final MapCodec<StoragePlacerBlock> CODEC = simpleCodec(StoragePlacerBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public StoragePlacerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoragePlacerBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
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
                        dev.lumungus.storage.registry.LumungusStorageBlockEntities.STORAGE_PLACER,
                        StoragePlacerBlockEntity::serverTick
                );
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
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
                    Direction nextFacing = WorkBlockFacing.next(state.getValue(FACING));
                    level.setBlock(pos, state.setValue(FACING, nextFacing), 3);
                    player.sendSystemMessage(Component.translatable(
                            "message.lumungus_storage.work_block.facing",
                            WorkBlockFacing.displayName(nextFacing)
                    ));
                }
                return InteractionResult.SUCCESS;
            }
            return CopperWrenchItem.dismantle(heldStack, level, pos, player);
        }
        if (heldStack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StoragePlacerBlockEntity placer) {
                WirelessModuleInteractions.tryInstall(placer, heldStack, player);
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StoragePlacerBlockEntity placer) {
            placer.setFilter(heldStack);
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.placer.filter_set",
                    heldStack.getHoverName()
            ));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
        BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StoragePlacerBlockEntity placer) {
            if (player.isSecondaryUseActive()) {
                if (WirelessModuleInteractions.tryRemove(placer, player)) {
                    return InteractionResult.SUCCESS;
                }
                placer.clearFilter();
                player.sendSystemMessage(Component.translatable("message.lumungus_storage.placer.filter_cleared"));
                return InteractionResult.SUCCESS;
            }

            boolean linked = placer.refreshControllerLink();
            ItemStack filter = placer.filter();
            Direction facing = state.getValue(FACING);
            BlockState targetState = level.getBlockState(pos.relative(facing));
            Component target = targetState.isAir()
                    ? Component.translatable("message.lumungus_storage.work_block.target_air")
                    : targetState.getBlock().getName();
            Component status = WorkBlockStatus.describe(level, pos, facing.getOpposite(), linked);
            player.sendSystemMessage(Component.translatable(
                    filter.isEmpty()
                            ? "message.lumungus_storage.placer.active_unfiltered"
                            : "message.lumungus_storage.placer.active_filtered",
                    filter.getHoverName(),
                    WorkBlockFacing.displayName(facing),
                    status,
                    target
            ));
        }
        return InteractionResult.SUCCESS;
    }
}
