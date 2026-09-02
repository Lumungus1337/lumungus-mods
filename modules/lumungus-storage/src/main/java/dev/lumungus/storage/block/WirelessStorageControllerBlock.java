package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.block.entity.WirelessStorageControllerBlockEntity;
import dev.lumungus.storage.item.CopperWrenchItem;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.network.WirelessStorageControllerRegistry;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.data.BoundStorageController;
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
import net.minecraft.world.level.block.Block;
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

public final class WirelessStorageControllerBlock extends BaseEntityBlock {
    public static final MapCodec<WirelessStorageControllerBlock> CODEC = simpleCodec(properties ->
            new WirelessStorageControllerBlock(properties, WirelessTier.SHORT_RANGE));
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final WirelessTier tier;

    public WirelessStorageControllerBlock(BlockBehaviour.Properties properties, WirelessTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WirelessStorageControllerBlockEntity(pos, state);
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
                        dev.lumungus.storage.registry.LumungusStorageBlockEntities.WIRELESS_STORAGE_CONTROLLER,
                        WirelessStorageControllerBlockEntity::serverTick
                );
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        WirelessStorageControllerRegistry.register(level, pos);
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
        WirelessStorageControllerRegistry.unregister(level, pos);
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
                }
                return InteractionResult.SUCCESS;
            }
            return CopperWrenchItem.dismantle(heldStack, level, pos, player);
        }
        if (heldStack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof WirelessStorageControllerBlockEntity wireless) {
                StorageControllerBlockEntity controller = wireless.linkedController();
                if (controller == null) {
                    player.sendSystemMessage(Component.translatable(
                            "message.lumungus_storage.wireless_module.controller_unlinked"
                    ));
                } else {
                    heldStack.set(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER, new BoundStorageController(
                            controller.getLevel().dimension().identifier(),
                            controller.getBlockPos().immutable(),
                            controller.getNetworkId()
                    ));
                    player.sendSystemMessage(Component.translatable(
                            "message.lumungus_storage.wireless_module.primed",
                            controller.getNetworkLabel()
                    ));
                }
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
        if (!level.isClientSide()) {
            if (level.getBlockEntity(pos) instanceof WirelessStorageControllerBlockEntity wireless) {
                WirelessStorageControllerRegistry.register(level, pos);
                StorageControllerBlockEntity controller = wireless.linkedController();
                player.sendSystemMessage(Component.translatable(controller == null
                        ? "message.lumungus_storage.wireless_controller.unlinked"
                        : "message.lumungus_storage.wireless_controller.connected",
                        tier.label(),
                        controller == null ? Component.empty() : WirelessStatusText.position(controller.getBlockPos())));
                if (controller != null) {
                    player.openMenu(wireless);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static WirelessTier tierFor(Block block) {
        if (block == dev.lumungus.storage.registry.LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_DIMENSION) {
            return WirelessTier.SAME_DIMENSION;
        }
        if (block == dev.lumungus.storage.registry.LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL) {
            return WirelessTier.MULTIDIMENSIONAL;
        }
        return WirelessTier.SHORT_RANGE;
    }

    public enum WirelessTier {
        SHORT_RANGE("short", 32),
        SAME_DIMENSION("dimension", 128),
        MULTIDIMENSIONAL("multidimensional", 256);

        private final String label;
        private final int searchRadius;

        WirelessTier(String label, int searchRadius) {
            this.label = label;
            this.searchRadius = searchRadius;
        }

        public String label() {
            return label;
        }

        public int searchRadius() {
            return searchRadius;
        }

        public boolean canReach(boolean sameDimension, BlockPos wirelessPos, BlockPos controllerPos) {
            return switch (this) {
                case SHORT_RANGE -> sameDimension && wirelessPos.closerThan(controllerPos, searchRadius + 1);
                case SAME_DIMENSION -> sameDimension;
                case MULTIDIMENSIONAL -> true;
            };
        }
    }
}
