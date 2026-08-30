package dev.lumungus.integration.migration;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/** Immutable item-and-components identity used by migration snapshots. */
public final class MigrationItemKey {
    private final ItemStack stack;

    private MigrationItemKey(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Migration item must not be empty");
        }
        this.stack = stack.copyWithCount(1);
    }

    public static MigrationItemKey of(ItemStack stack) {
        return new MigrationItemKey(Objects.requireNonNull(stack, "stack"));
    }

    public ItemStack stack() {
        return stack.copy();
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof MigrationItemKey that
                && ItemStack.isSameItemSameComponents(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}
