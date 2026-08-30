package dev.lumungus.storage.block;

import com.mojang.serialization.MapCodec;
import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.registry.LumungusStorageItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class DriveBayBlock extends BaseEntityBlock {
    public static final MapCodec<DriveBayBlock> CODEC = simpleCodec(DriveBayBlock::new);

    public DriveBayBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DriveBayBlockEntity(pos, state);
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
        if (!(level.getBlockEntity(pos) instanceof DriveBayBlockEntity driveBay)) {
            return InteractionResult.PASS;
        }

        if (!driveBay.hasCell() && heldStack.is(LumungusStorageItems.STORAGE_CELL_16K)) {
            player.setItemInHand(hand, driveBay.insertCell(heldStack, TransferMode.EXECUTE));
            player.sendSystemMessage(Component.translatable("message.lumungus_storage.drive_bay.cell_inserted"));
            return InteractionResult.SUCCESS;
        }

        if (!driveBay.hasCell()) {
            player.sendSystemMessage(Component.translatable("message.lumungus_storage.drive_bay.no_cell"));
            return InteractionResult.SUCCESS;
        }

        ItemStack remainder = driveBay.insert(heldStack, TransferMode.EXECUTE);
        int inserted = heldStack.getCount() - remainder.getCount();
        player.setItemInHand(hand, remainder);
        player.sendSystemMessage(Component.translatable(
                inserted > 0
                        ? "message.lumungus_storage.drive_bay.inserted"
                        : "message.lumungus_storage.drive_bay.full",
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
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof DriveBayBlockEntity driveBay)) {
            return InteractionResult.PASS;
        }

        if (player.isSecondaryUseActive()) {
            ItemStack removed = driveBay.removeCell(TransferMode.EXECUTE);
            if (removed.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.lumungus_storage.drive_bay.no_cell"));
            } else {
                player.getInventory().placeItemBackInInventory(removed);
                player.sendSystemMessage(Component.translatable("message.lumungus_storage.drive_bay.cell_removed"));
            }
            return InteractionResult.SUCCESS;
        }

        if (!driveBay.hasCell()) {
            player.sendSystemMessage(Component.translatable("message.lumungus_storage.drive_bay.no_cell"));
            return InteractionResult.SUCCESS;
        }
        if (driveBay.storedResources().isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.lumungus_storage.drive_bay.empty"));
            return InteractionResult.SUCCESS;
        }

        ItemStack template = driveBay.storedResources().getFirst().stack();
        ItemStack extracted = driveBay.extract(template, template.getMaxStackSize(), TransferMode.EXECUTE);
        player.getInventory().placeItemBackInInventory(extracted);
        player.sendSystemMessage(Component.translatable(
                "message.lumungus_storage.drive_bay.extracted",
                extracted.getCount(),
                extracted.getHoverName()
        ));
        return InteractionResult.SUCCESS;
    }
}
