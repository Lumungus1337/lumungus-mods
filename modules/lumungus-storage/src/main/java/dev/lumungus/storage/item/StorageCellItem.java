package dev.lumungus.storage.item;

import dev.lumungus.storage.data.StorageCellData;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class StorageCellItem extends Item {
    public StorageCellItem(Properties properties) {
        super(properties);
    }

    public static StorageCellData getData(ItemStack stack) {
        StorageCellData data = stack.get(LumungusStorageDataComponents.STORAGE_CELL_DATA);
        return data == null ? StorageCellData.EMPTY : data;
    }

    public static void setData(ItemStack stack, StorageCellData data) {
        stack.set(LumungusStorageDataComponents.STORAGE_CELL_DATA, data);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !getData(stack).snapshot().isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        long used = getData(stack).snapshot().storedTotalAmount();
        return Math.round(13.0F * used / StorageCellData.MAX_TOTAL_AMOUNT);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float remaining = 1.0F - (float) getData(stack).snapshot().storedTotalAmount()
                / StorageCellData.MAX_TOTAL_AMOUNT;
        return Math.round(255.0F * remaining) << 8 | Math.round(255.0F * (1.0F - remaining));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> textConsumer,
            TooltipFlag flag
    ) {
        StorageCellData data = getData(stack);
        textConsumer.accept(Component.translatable(
                "tooltip.lumungus_storage.storage_cell_16k.items",
                data.snapshot().storedTotalAmount(),
                StorageCellData.MAX_TOTAL_AMOUNT
        ));
        textConsumer.accept(Component.translatable(
                "tooltip.lumungus_storage.storage_cell_16k.types",
                data.snapshot().storedDistinctTypes(),
                StorageCellData.MAX_DISTINCT_TYPES
        ));
    }
}
