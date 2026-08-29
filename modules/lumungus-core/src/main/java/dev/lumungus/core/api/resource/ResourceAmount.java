package dev.lumungus.core.api.resource;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public final class ResourceAmount {
    private final ItemStack stack;
    private final long amount;

    public ResourceAmount(ItemStack stack, long amount) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Resource stack must not be empty");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Resource amount must not be negative");
        }

        this.stack = stack.copyWithCount(1);
        this.amount = amount;
    }

    public ItemStack stack() {
        return stack.copy();
    }

    public long amount() {
        return amount;
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ResourceAmount that)) {
            return false;
        }

        return amount == that.amount && ItemStack.isSameItemSameComponents(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ItemStack.hashItemAndComponents(stack), amount);
    }
}
