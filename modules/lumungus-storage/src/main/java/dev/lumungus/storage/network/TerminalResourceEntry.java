package dev.lumungus.storage.network;

import dev.lumungus.core.api.resource.ResourceAmount;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public final class TerminalResourceEntry {
    private final ItemStack stack;
    private final long amount;

    public TerminalResourceEntry(ItemStack stack, long amount) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Terminal resource must not be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Terminal resource amount must be positive");
        }
        this.stack = stack.copyWithCount(1);
        this.amount = amount;
    }

    public TerminalResourceEntry(ResourceAmount resource) {
        this(resource.stack(), resource.amount());
    }

    public ItemStack stack() {
        return stack.copy();
    }

    public long amount() {
        return amount;
    }

    public ResourceAmount asResourceAmount() {
        return new ResourceAmount(stack, amount);
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof TerminalResourceEntry that
                && amount == that.amount
                && ItemStack.isSameItemSameComponents(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ItemStack.hashItemAndComponents(stack), amount);
    }
}
