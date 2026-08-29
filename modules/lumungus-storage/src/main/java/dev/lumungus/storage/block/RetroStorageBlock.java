package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;

public final class RetroStorageBlock extends Block {
    public static final MapCodec<RetroStorageBlock> CODEC = simpleCodec(RetroStorageBlock::new);

    private final String statusTranslationKey;

    public RetroStorageBlock(BlockBehaviour.Properties properties, String statusTranslationKey) {
        super(properties);
        this.statusTranslationKey = statusTranslationKey;
    }

    private RetroStorageBlock(BlockBehaviour.Properties properties) {
        this(properties, "message.lumungus_storage.block_pending");
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.sendSystemMessage(Component.translatable(statusTranslationKey));
        }

        return InteractionResult.SUCCESS;
    }
}
