package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
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

public final class StorageControllerBlock extends BaseEntityBlock {
    public static final MapCodec<StorageControllerBlock> CODEC = simpleCodec(StorageControllerBlock::new);

    public StorageControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
        BlockHitResult hit
    ) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof StorageControllerBlockEntity controller) {
            StorageControllerBlockEntity.NetworkStatus status = controller.refreshNetwork();
            long used = controller.snapshot().storedTotalAmount();
            long capacity = controller.capacity().maxTotalAmount();
            player.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.storage_controller.status",
                    controller.getNetworkLabel(),
                    status.linkedTerminals(),
                    status.linkedDriveBays(),
                    used,
                    capacity
            ));
        }

        return InteractionResult.SUCCESS;
    }
}
