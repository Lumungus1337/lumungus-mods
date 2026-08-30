package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.storage.block.entity.InventoryConnectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class InventoryConnectorBlock extends BaseEntityBlock {
    public static final MapCodec<InventoryConnectorBlock> CODEC = simpleCodec(InventoryConnectorBlock::new);

    public InventoryConnectorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InventoryConnectorBlockEntity(pos, state);
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
                && level.getBlockEntity(pos) instanceof InventoryConnectorBlockEntity connector) {
            int inventories = connector.endpoints().size();
            player.sendSystemMessage(Component.translatable(
                    connector.refreshControllerLink()
                            ? "message.lumungus_storage.inventory_connector.connected"
                            : "message.lumungus_storage.inventory_connector.no_controller",
                    inventories
            ));
        }
        return InteractionResult.SUCCESS;
    }
}
