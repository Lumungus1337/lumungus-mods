package dev.lumungus.storage.item;

import dev.lumungus.storage.block.WorkBlockFacing;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageTags;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class CopperWrenchItem extends Item {
    public CopperWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return interact(
                context.getItemInHand(),
                context.getLevel(),
                context.getClickedPos(),
                context.getPlayer(),
                context.isSecondaryUseActive()
        );
    }

    public static InteractionResult interact(ItemStack wrench, Level level, BlockPos pos, Player player, boolean rotate) {
        if (!rotate) {
            return dismantle(wrench, level, pos, player);
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(LumungusStorageTags.WRENCH_REMOVABLE)) {
            return InteractionResult.PASS;
        }
        EnumProperty<Direction> facing = state.hasProperty(BlockStateProperties.FACING)
                ? BlockStateProperties.FACING : BlockStateProperties.HORIZONTAL_FACING;
        // Automatic pipes have no independent orientation; never dismantle them on a rotation click.
        if (!state.hasProperty(facing)) {
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            Direction next = facing == BlockStateProperties.FACING
                    ? WorkBlockFacing.next(state.getValue(facing)) : state.getValue(facing).getClockWise();
            level.setBlock(pos, state.setValue(facing, next), 3);
            StorageNetworkTopology.invalidateAround(level, pos);
            level.playSound(null, pos, SoundEvents.COPPER_STEP, SoundSource.BLOCKS, 0.6F, 1.3F);
            if (player != null) {
                player.sendSystemMessage(Component.translatable(
                        "message.lumungus_storage.work_block.facing", WorkBlockFacing.displayName(next)));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> textConsumer,
            TooltipFlag flag
    ) {
        textConsumer.accept(Component.translatable("tooltip.lumungus_storage.copper_wrench"));
    }

    public static InteractionResult dismantle(ItemStack wrench, Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(LumungusStorageTags.WRENCH_REMOVABLE)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        boolean dropBlock = player == null || !player.getAbilities().instabuild;
        level.destroyBlock(pos, dropBlock, player, 512);
        level.playSound(
                null,
                pos,
                SoundEvents.COPPER_BREAK,
                SoundSource.BLOCKS,
                0.75F,
                1.25F
        );
        damageWrench(wrench, level, player);
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void damageWrench(ItemStack wrench, Level level, Player player) {
        if (wrench.isEmpty() || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        wrench.hurtAndBreak(1, serverLevel, serverPlayer, item -> {
        });
    }
}
