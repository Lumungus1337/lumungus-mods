package dev.lumungus.storage.block.entity;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageAccess;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageProvider;
import dev.lumungus.core.api.storage.StorageSnapshot;
import dev.lumungus.storage.data.StorageCellData;
import dev.lumungus.storage.item.StorageCellItem;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import dev.lumungus.storage.registry.LumungusStorageItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class DriveBayBlockEntity extends BlockEntity implements StorageAccess, StorageProvider {
    private static final String CELL_KEY = "cell";
    private static final StorageCapacity EMPTY_CAPACITY = new StorageCapacity(0, 0);

    private ItemStack cell = ItemStack.EMPTY;

    public DriveBayBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.DRIVE_BAY, pos, state);
    }

    public boolean hasCell() {
        return !cell.isEmpty();
    }

    public ItemStack cell() {
        return cell.copy();
    }

    public ItemStack insertCell(ItemStack candidate, TransferMode mode) {
        if (hasCell() || candidate.isEmpty() || !candidate.is(LumungusStorageItems.STORAGE_CELL_16K)) {
            return candidate.copy();
        }

        ItemStack remainder = candidate.getCount() == 1
                ? ItemStack.EMPTY
                : candidate.copyWithCount(candidate.getCount() - 1);
        if (mode == TransferMode.EXECUTE) {
            cell = candidate.copyWithCount(1);
            setChanged();
        }
        return remainder;
    }

    public ItemStack removeCell(TransferMode mode) {
        if (!hasCell()) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = cell.copy();
        if (mode == TransferMode.EXECUTE) {
            cell = ItemStack.EMPTY;
            setChanged();
        }
        return removed;
    }

    @Override
    public StorageAccess storageAccess() {
        return this;
    }

    @Override
    public StorageCapacity capacity() {
        return hasCell() ? StorageCellData.CAPACITY : EMPTY_CAPACITY;
    }

    @Override
    public StorageSnapshot snapshot() {
        return hasCell() ? cellData().snapshot() : new StorageSnapshot(0, 0);
    }

    @Override
    public List<ResourceAmount> storedResources() {
        return hasCell() ? cellData().storedResources() : List.of();
    }

    @Override
    public long count(ItemStack template) {
        return hasCell() ? cellData().count(template) : 0;
    }

    @Override
    public ItemStack insert(ItemStack stack, TransferMode mode) {
        if (!hasCell() || stack.isEmpty() || stack.getItem() instanceof StorageCellItem) {
            return stack.copy();
        }

        StorageCellData.InsertResult result = cellData().insert(stack);
        if (mode == TransferMode.EXECUTE && result.remainder().getCount() != stack.getCount()) {
            StorageCellItem.setData(cell, result.data());
            setChanged();
        }
        return result.remainder();
    }

    @Override
    public ItemStack extract(ItemStack template, long maxAmount, TransferMode mode) {
        if (!hasCell()) {
            return ItemStack.EMPTY;
        }

        StorageCellData.ExtractResult result = cellData().extract(template, maxAmount);
        if (mode == TransferMode.EXECUTE && !result.extracted().isEmpty()) {
            StorageCellItem.setData(cell, result.data());
            setChanged();
        }
        return result.extracted();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide() && hasCell()) {
            Block.popResource(level, pos, removeCell(TransferMode.EXECUTE));
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cell = input.read(CELL_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        if (!cell.isEmpty() && !cell.is(LumungusStorageItems.STORAGE_CELL_16K)) {
            cell = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(CELL_KEY, ItemStack.OPTIONAL_CODEC, cell);
    }

    private StorageCellData cellData() {
        return StorageCellItem.getData(cell);
    }
}
