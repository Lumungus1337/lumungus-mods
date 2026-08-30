package dev.lumungus.storage.inventory;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageAccess;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

/** Adapts a Fabric item storage without moving its contents into Lumungus-owned data. */
public final class FabricItemStorageAccess implements StorageAccess {
    private final Storage<ItemVariant> storage;

    public FabricItemStorageAccess(Storage<ItemVariant> storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    @Override
    public StorageCapacity capacity() {
        long totalCapacity = 0;
        int typeCapacity = 0;
        for (StorageView<ItemVariant> view : storage) {
            totalCapacity = saturatedAdd(totalCapacity, Math.max(0, view.getCapacity()));
            if (view.getCapacity() > 0 && typeCapacity < Integer.MAX_VALUE) {
                typeCapacity++;
            }
        }
        return new StorageCapacity(totalCapacity, typeCapacity);
    }

    @Override
    public StorageSnapshot snapshot() {
        List<ResourceAmount> resources = storedResources();
        long total = resources.stream().mapToLong(ResourceAmount::amount).sum();
        return new StorageSnapshot(total, resources.size());
    }

    @Override
    public List<ResourceAmount> storedResources() {
        Map<ItemVariant, Long> amounts = new LinkedHashMap<>();
        for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
            ItemVariant resource = view.getResource();
            if (!resource.isBlank() && view.getAmount() > 0) {
                amounts.merge(resource, view.getAmount(), FabricItemStorageAccess::saturatedAdd);
            }
        }

        List<ResourceAmount> resources = new ArrayList<>(amounts.size());
        amounts.forEach((variant, amount) -> resources.add(new ResourceAmount(variant.toStack(), amount)));
        return List.copyOf(resources);
    }

    @Override
    public long count(ItemStack template) {
        if (template.isEmpty()) {
            return 0;
        }

        ItemVariant requested = ItemVariant.of(template);
        long amount = 0;
        for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
            if (requested.equals(view.getResource())) {
                amount = saturatedAdd(amount, view.getAmount());
            }
        }
        return amount;
    }

    @Override
    public ItemStack insert(ItemStack stack, TransferMode mode) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(mode, "mode");
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            if (mode == TransferMode.EXECUTE) {
                transaction.commit();
            }
        }
        return inserted >= stack.getCount()
                ? ItemStack.EMPTY
                : stack.copyWithCount(stack.getCount() - (int) inserted);
    }

    @Override
    public ItemStack extract(ItemStack template, long maxAmount, TransferMode mode) {
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(mode, "mode");
        if (template.isEmpty() || maxAmount <= 0) {
            return ItemStack.EMPTY;
        }

        long requested = Math.min(maxAmount, template.getMaxStackSize());
        long extracted;
        ItemVariant variant = ItemVariant.of(template);
        try (Transaction transaction = Transaction.openOuter()) {
            extracted = storage.extract(variant, requested, transaction);
            if (mode == TransferMode.EXECUTE) {
                transaction.commit();
            }
        }
        return extracted <= 0 ? ItemStack.EMPTY : variant.toStack((int) extracted);
    }

    private static long saturatedAdd(long first, long second) {
        if (second > 0 && first > Long.MAX_VALUE - second) {
            return Long.MAX_VALUE;
        }
        return first + second;
    }
}
