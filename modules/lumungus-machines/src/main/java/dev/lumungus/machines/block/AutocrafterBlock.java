package dev.lumungus.machines.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.machines.block.entity.AutocrafterBlockEntity;
import dev.lumungus.storage.item.CopperWrenchItem;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.wireless.WirelessModuleInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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

public final class AutocrafterBlock extends BaseEntityBlock {
    public static final MapCodec<AutocrafterBlock> CODEC = simpleCodec(AutocrafterBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AutocrafterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutocrafterBlockEntity(pos, state);
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
                        dev.lumungus.machines.registry.LumungusMachinesBlockEntities.AUTOCRAFTER,
                        AutocrafterBlockEntity::serverTick
                );
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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof AutocrafterBlockEntity autocrafter)) {
            return InteractionResult.PASS;
        }

        if (heldStack.is(LumungusStorageItems.COPPER_WRENCH)) {
            if (player.isSecondaryUseActive()) {
                Direction nextFacing = state.getValue(FACING).getClockWise();
                level.setBlock(pos, state.setValue(FACING, nextFacing), 3);
                player.sendSystemMessage(Component.translatable(
                        "message.lumungus_storage.work_block.facing",
                        Component.translatable("direction.minecraft." + nextFacing.getName())
                ));
                return InteractionResult.SUCCESS;
            }
            return CopperWrenchItem.dismantle(heldStack, level, pos, player);
        }

        if (heldStack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)) {
            WirelessModuleInteractions.tryInstall(autocrafter, heldStack, player);
            return InteractionResult.SUCCESS;
        }

        autocrafter.setTarget(heldStack, Math.max(1, heldStack.getCount()));
        player.sendSystemMessage(Component.translatable(
                "message.lumungus_machines.autocrafter.target_set",
                heldStack.getHoverName(),
                autocrafter.targetAmount()
        ));
        player.openMenu(autocrafter);
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
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AutocrafterBlockEntity autocrafter) {
            if (player.isSecondaryUseActive() && WirelessModuleInteractions.tryRemove(autocrafter, player)) {
                return InteractionResult.SUCCESS;
            }
            player.openMenu(autocrafter);
        }
        return InteractionResult.SUCCESS;
    }
}
