package dev.lumungus.core.api.inventory;

import net.minecraft.world.item.ItemStack;

public interface ItemTransferTarget {
    ItemStack insert(ItemStack stack, TransferMode mode);

    ItemStack extract(ItemStack template, long maxAmount, TransferMode mode);
}
