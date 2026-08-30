package dev.lumungus.storage.data;

import com.mojang.serialization.Codec;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

public final class StorageCellData {
    public static final long MAX_TOTAL_AMOUNT = 16_384;
    public static final int MAX_DISTINCT_TYPES = 64;
    public static final StorageCapacity CAPACITY = new StorageCapacity(MAX_TOTAL_AMOUNT, MAX_DISTINCT_TYPES);
    public static final StorageCellData EMPTY = new StorageCellData(List.of());
    public static final Codec<StorageCellData> CODEC = StorageCellEntry.CODEC.listOf()
            .xmap(StorageCellData::new, StorageCellData::entriesForCodec);

    private final List<StorageCellEntry> entries;
    private final long totalAmount;

    public StorageCellData(List<StorageCellEntry> entries) {
        List<StorageCellEntry> copy = List.copyOf(entries);
        if (copy.size() > MAX_DISTINCT_TYPES) {
            throw new IllegalArgumentException("Storage cell contains too many distinct item types");
        }

        long total = 0;
        Set<Integer> hashes = new HashSet<>();
        for (int index = 0; index < copy.size(); index++) {
            StorageCellEntry entry = copy.get(index);
            total = Math.addExact(total, entry.amount());
            int itemHash = ItemStack.hashItemAndComponents(entry.stack());
            if (!hashes.add(itemHash)) {
                for (int previous = 0; previous < index; previous++) {
                    if (copy.get(previous).matches(entry.stack())) {
                        throw new IllegalArgumentException("Storage cell contains a duplicate item type");
                    }
                }
            }
        }
        if (total > MAX_TOTAL_AMOUNT) {
            throw new IllegalArgumentException("Storage cell exceeds its item capacity");
        }

        this.entries = copy;
        this.totalAmount = total;
    }

    public List<ResourceAmount> storedResources() {
        return entries.stream().map(StorageCellEntry::asResourceAmount).toList();
    }

    public long count(ItemStack template) {
        return entries.stream()
                .filter(entry -> entry.matches(template))
                .mapToLong(StorageCellEntry::amount)
                .findFirst()
                .orElse(0);
    }

    public StorageSnapshot snapshot() {
        return new StorageSnapshot(totalAmount, entries.size());
    }

    public InsertResult insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return new InsertResult(this, ItemStack.EMPTY);
        }

        int matchingIndex = findEntry(stack);
        if (matchingIndex < 0 && entries.size() >= MAX_DISTINCT_TYPES) {
            return new InsertResult(this, stack.copy());
        }

        long available = MAX_TOTAL_AMOUNT - totalAmount;
        int inserted = (int) Math.min(available, stack.getCount());
        if (inserted <= 0) {
            return new InsertResult(this, stack.copy());
        }

        List<StorageCellEntry> updated = new ArrayList<>(entries);
        if (matchingIndex >= 0) {
            StorageCellEntry current = updated.get(matchingIndex);
            updated.set(matchingIndex, new StorageCellEntry(current.stack(), current.amount() + inserted));
        } else {
            updated.add(new StorageCellEntry(stack, inserted));
        }

        ItemStack remainder = stack.getCount() == inserted
                ? ItemStack.EMPTY
                : stack.copyWithCount(stack.getCount() - inserted);
        return new InsertResult(new StorageCellData(updated), remainder);
    }

    public ExtractResult extract(ItemStack template, long maxAmount) {
        if (template.isEmpty() || maxAmount <= 0) {
            return new ExtractResult(this, ItemStack.EMPTY);
        }

        int matchingIndex = findEntry(template);
        if (matchingIndex < 0) {
            return new ExtractResult(this, ItemStack.EMPTY);
        }

        StorageCellEntry current = entries.get(matchingIndex);
        int extractedAmount = (int) Math.min(Math.min(maxAmount, current.amount()), template.getMaxStackSize());
        if (extractedAmount <= 0) {
            return new ExtractResult(this, ItemStack.EMPTY);
        }

        List<StorageCellEntry> updated = new ArrayList<>(entries);
        long remainingAmount = current.amount() - extractedAmount;
        if (remainingAmount == 0) {
            updated.remove(matchingIndex);
        } else {
            updated.set(matchingIndex, new StorageCellEntry(current.stack(), remainingAmount));
        }

        return new ExtractResult(
                new StorageCellData(updated),
                current.stack().copyWithCount(extractedAmount)
        );
    }

    private int findEntry(ItemStack stack) {
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).matches(stack)) {
                return index;
            }
        }
        return -1;
    }

    private List<StorageCellEntry> entriesForCodec() {
        return entries;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof StorageCellData that && entries.equals(that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    public record InsertResult(StorageCellData data, ItemStack remainder) {
        public InsertResult {
            Objects.requireNonNull(data, "data");
            remainder = remainder.copy();
        }

        @Override
        public ItemStack remainder() {
            return remainder.copy();
        }
    }

    public record ExtractResult(StorageCellData data, ItemStack extracted) {
        public ExtractResult {
            Objects.requireNonNull(data, "data");
            extracted = extracted.copy();
        }

        @Override
        public ItemStack extracted() {
            return extracted.copy();
        }
    }
}
