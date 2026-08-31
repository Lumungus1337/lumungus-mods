package dev.lumungus.storage.storage;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

public final class ShulkerBoxTransfer {
    public static final int SLOT_COUNT = 27;

    private ShulkerBoxTransfer() {
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof ShulkerBoxBlock;
    }

    public static boolean isFilledShulkerBox(ItemStack stack) {
        if (!isShulkerBox(stack)) {
            return false;
        }
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        return contents != null && contents.nonEmptyItemCopyStream().findAny().isPresent();
    }

    public static boolean isEmptyShulkerBox(ItemStack stack) {
        return isShulkerBox(stack) && !isFilledShulkerBox(stack);
    }

    public static ItemStack emptyCopy(ItemStack shulkerBox) {
        ItemStack empty = shulkerBox.copyWithCount(1);
        empty.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        return empty;
    }

    public static List<ItemStack> unpackedContents(ItemStack shulkerBox) {
        ItemContainerContents contents = shulkerBox.get(DataComponents.CONTAINER);
        if (contents == null) {
            return List.of();
        }
        return contents.nonEmptyItemCopyStream()
                .map(ItemStack::copy)
                .toList();
    }

    public static int maxPackedAmount(ItemStack template) {
        if (template.isEmpty() || isShulkerBox(template)) {
            return 0;
        }
        return SLOT_COUNT * template.getMaxStackSize();
    }

    public static ItemStack filledShulker(ItemStack emptyShulkerBox, List<ItemStack> contents) {
        ItemStack filled = emptyCopy(emptyShulkerBox);
        filled.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        return filled;
    }

    public static List<ItemStack> packSingleItem(ItemStack template, int amount) {
        if (template.isEmpty() || amount <= 0 || isShulkerBox(template)) {
            return List.of();
        }

        List<ItemStack> contents = new ArrayList<>();
        int remaining = Math.min(amount, maxPackedAmount(template));
        while (remaining > 0 && contents.size() < SLOT_COUNT) {
            int stackSize = Math.min(template.getMaxStackSize(), remaining);
            contents.add(template.copyWithCount(stackSize));
            remaining -= stackSize;
        }
        return List.copyOf(contents);
    }
}
