package dev.lumungus.core.api.filter;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public final class ConfiguredItemFilter implements ItemFilter {
    private final FilterMode mode;
    private final List<ItemStack> entries;

    public ConfiguredItemFilter(FilterMode mode, List<ItemStack> entries) {
        this.mode = mode;
        this.entries = entries.stream()
            .filter(stack -> !stack.isEmpty())
            .map(stack -> stack.copyWithCount(1))
            .toList();
    }

    public FilterMode mode() {
        return mode;
    }

    public List<ItemStack> entries() {
        return entries.stream().map(ItemStack::copy).toList();
    }

    @Override
    public boolean matches(ItemStack stack) {
        boolean listed = entries.stream().anyMatch(entry -> ItemStack.isSameItemSameComponents(entry, stack));
        return mode == FilterMode.ALLOW_LIST ? listed : !listed;
    }
}
