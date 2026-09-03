package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.block.entity.WirelessInventoryConnectorBlockEntity;
import dev.lumungus.storage.item.CopperWrenchItem;
import dev.lumungus.storage.menu.WirelessModuleMenuProvider;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.network.WirelessInventoryConnectorRegistry;
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

public final class WirelessInventoryConnectorBlock extends BaseEntityBlock {
    public static final MapCodec<WirelessInventoryConnectorBlock> CODEC = simpleCodec(properties ->
            new WirelessInventoryConnectorBlock(properties, WirelessConnectorTier.SHORT_RANGE));
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final WirelessConnectorTier tier;

    public WirelessInventoryConnectorBlock(BlockBehaviour.Properties properties, WirelessConnectorTier tier) {
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
        return new WirelessInventoryConnectorBlockEntity(pos, state);
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
                        dev.lumungus.storage.registry.LumungusStorageBlockEntities.WIRELESS_INVENTORY_CONNECTOR,
                        WirelessInventoryConnectorBlockEntity::serverTick
                );
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        WirelessInventoryConnectorRegistry.register(level, pos);
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
        WirelessInventoryConnectorRegistry.unregister(level, pos);
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
            if (!level.isClientSide()
                    && level.getBlockEntity(pos) instanceof WirelessInventoryConnectorBlockEntity connector) {
                WirelessModuleInteractions.tryInstall(connector, heldStack, player);
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
        if (!level.isClientSide()
                && level.getBlockEntity(pos) instanceof WirelessInventoryConnectorBlockEntity connector) {
            WirelessInventoryConnectorRegistry.register(level, pos);
            if (player.isSecondaryUseActive()) {
                player.openMenu(new WirelessModuleMenuProvider(connector, connector));
                return InteractionResult.SUCCESS;
            }
            if (connector.wirelessModule().isEmpty()) {
                player.sendSystemMessage(Component.translatable(
                        "message.lumungus_storage.wireless_module.missing"
                ));
                return InteractionResult.SUCCESS;
            }
            boolean linked = connector.refreshControllerLink();
            boolean enabled = connector.toggleAutoSendToDriveBays();
            StorageControllerBlockEntity.BayMoveResult result = enabled
                    ? connector.sendToDriveBays()
                    : new StorageControllerBlockEntity.BayMoveResult(0, 0, connector.endpoints().size(), 0, false);
            BlockPos controllerPos = connector.linkedControllerPosition();
            player.sendSystemMessage(Component.translatable(
                    enabled
                            ? "message.lumungus_storage.inventory_connector.auto_send_enabled_details"
                            : "message.lumungus_storage.inventory_connector.auto_send_disabled_details",
                    linked && controllerPos != null
                            ? Component.translatable(
                                    "message.lumungus_storage.inventory_connector.linked_at",
                                    WirelessStatusText.position(controllerPos)
                            )
                            : Component.translatable("message.lumungus_storage.inventory_connector.unlinked"),
                    result.physicalInventories(),
                    result.driveBays(),
                    result.movedItems()
            ));
        }
        return InteractionResult.SUCCESS;
    }

    public static WirelessConnectorTier tierFor(Block block) {
        if (block == dev.lumungus.storage.registry.LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_DIMENSION) {
            return WirelessConnectorTier.SAME_DIMENSION;
        }
        if (block == dev.lumungus.storage.registry.LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_MULTIDIMENSIONAL) {
            return WirelessConnectorTier.MULTIDIMENSIONAL;
        }
        return WirelessConnectorTier.SHORT_RANGE;
    }

    public enum WirelessConnectorTier {
        SHORT_RANGE("short", 32),
        SAME_DIMENSION("dimension", 128),
        MULTIDIMENSIONAL("multidimensional", 256);

        private final String label;
        private final int searchRadius;

        WirelessConnectorTier(String label, int searchRadius) {
            this.label = label;
            this.searchRadius = searchRadius;
        }

        public String label() {
            return label;
        }

        public int searchRadius() {
            return searchRadius;
        }

        public boolean canReach(boolean sameDimension, BlockPos connectorPos, BlockPos wirelessControllerPos) {
            return switch (this) {
                case SHORT_RANGE -> sameDimension && connectorPos.closerThan(wirelessControllerPos, searchRadius + 1);
                case SAME_DIMENSION -> sameDimension;
                case MULTIDIMENSIONAL -> true;
            };
        }
    }
}
