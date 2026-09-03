package dev.lumungus.storage.item;

import dev.lumungus.storage.block.StorageBreakerBlock;
import dev.lumungus.storage.registry.LumungusStorageTags;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
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

public final class CopperWrenchItem extends Item {
    public CopperWrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (context.isSecondaryUseActive() && state.getBlock() instanceof StorageBreakerBlock) {
            return StorageBreakerBlock.rotateWithWrench(
                    state,
                    context.getLevel(),
                    context.getClickedPos(),
                    context.getPlayer()
            );
        }
        return dismantle(
                context.getItemInHand(),
                context.getLevel(),
                context.getClickedPos(),
                context.getPlayer()
        );
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
