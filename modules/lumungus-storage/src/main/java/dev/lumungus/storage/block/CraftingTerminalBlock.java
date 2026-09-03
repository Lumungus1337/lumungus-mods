package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.storage.block.entity.CraftingTerminalBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.item.CopperWrenchItem;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageItems;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class CraftingTerminalBlock extends BaseEntityBlock {
    public static final MapCodec<CraftingTerminalBlock> CODEC = simpleCodec(CraftingTerminalBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CraftingTerminalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraftingTerminalBlockEntity(pos, state);
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
                    Direction nextFacing = state.getValue(FACING).getClockWise();
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

        if (heldStack.isEmpty() || !player.isSecondaryUseActive()) {
            return openTerminal(level, pos, player);
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof CraftingTerminalBlockEntity terminal)) {
            return InteractionResult.PASS;
        }

        StorageControllerBlockEntity controller = terminal.linkedController();
        if (controller == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.unlinked"
            ));
            return InteractionResult.SUCCESS;
        }

        ItemStack remainder = controller.insert(heldStack, TransferMode.EXECUTE);
        int inserted = heldStack.getCount() - remainder.getCount();
        player.setItemInHand(hand, remainder);
        player.sendSystemMessage(Component.translatable(
                inserted > 0
                        ? "message.lumungus_storage.crafting_terminal.inserted"
                        : "message.lumungus_storage.crafting_terminal.full",
                inserted
        ));
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
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CraftingTerminalBlockEntity terminal) {
            StorageControllerBlockEntity controller = terminal.linkedController();
            if (player.isSecondaryUseActive() && controller != null) {
                if (controller.storedResources().isEmpty()) {
                    player.sendSystemMessage(Component.translatable(
                            "message.lumungus_storage.crafting_terminal.empty"
                    ));
                    return InteractionResult.SUCCESS;
                }

                ItemStack template = controller.storedResources().getFirst().stack();
                ItemStack extracted = controller.extract(
                        template,
                        template.getMaxStackSize(),
                        TransferMode.EXECUTE
                );
                player.getInventory().placeItemBackInInventory(extracted);
                player.sendSystemMessage(Component.translatable(
                        "message.lumungus_storage.crafting_terminal.extracted",
                        extracted.getCount(),
                        extracted.getHoverName()
                ));
                return InteractionResult.SUCCESS;
            }
        }

        return openTerminal(level, pos, player);
    }

    private InteractionResult openTerminal(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CraftingTerminalBlockEntity terminal) {
            StorageControllerBlockEntity controller = terminal.linkedController();
            player.sendSystemMessage(Component.translatable(controller != null
                    ? "message.lumungus_storage.crafting_terminal.connected"
                    : "message.lumungus_storage.crafting_terminal.unlinked"));
            player.openMenu(terminal);
        }

        return InteractionResult.SUCCESS;
    }
}
