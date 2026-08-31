package dev.lumungus.storage.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public final class LumungusBlockItem extends BlockItem {
    private final String tooltipKey;

    public LumungusBlockItem(Block block, Properties properties, String tooltipKey) {
        super(block, properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> textConsumer,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, textConsumer, flag);
        if (tooltipKey != null) {
            textConsumer.accept(Component.translatable(tooltipKey));
        }
    }
}
