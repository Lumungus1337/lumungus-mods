package dev.lumungus.storage.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lumungus.core.api.resource.ResourceAmount;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public final class StorageCellEntry {
    public static final Codec<StorageCellEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(StorageCellEntry::stack),
            Codec.LONG.fieldOf("amount").forGetter(StorageCellEntry::amount)
    ).apply(instance, StorageCellEntry::new));

    private final ItemStack stack;
    private final long amount;

    public StorageCellEntry(ItemStack stack, long amount) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Storage cell entry must not be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Storage cell entry amount must be positive");
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

    public boolean matches(ItemStack candidate) {
        return ItemStack.isSameItemSameComponents(stack, candidate);
    }

    public ResourceAmount asResourceAmount() {
        return new ResourceAmount(stack, amount);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof StorageCellEntry that)) {
            return false;
        }
        return amount == that.amount && ItemStack.isSameItemSameComponents(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ItemStack.hashItemAndComponents(stack), amount);
    }
}
