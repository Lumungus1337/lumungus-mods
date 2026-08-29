package dev.lumungus.core.api.filter;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemFilter {
    ItemFilter ALLOW_ALL = stack -> true;
    ItemFilter DENY_ALL = stack -> false;

    boolean matches(ItemStack stack);

    default ItemFilter and(ItemFilter other) {
        return stack -> matches(stack) && other.matches(stack);
    }

    default ItemFilter or(ItemFilter other) {
        return stack -> matches(stack) || other.matches(stack);
    }

    default ItemFilter negate() {
        return stack -> !matches(stack);
    }
}
