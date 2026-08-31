package dev.lumungus.storage.block.entity;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageAccess;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageProvider;
import dev.lumungus.core.api.storage.StorageSnapshot;
import dev.lumungus.storage.data.StorageCellData;
import dev.lumungus.storage.item.StorageCellItem;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import dev.lumungus.storage.registry.LumungusStorageItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class DriveBayBlockEntity extends BlockEntity implements StorageAccess, StorageProvider {
    public static final int CELL_SLOTS = 8;

    private static final String CELL_KEY = "cell";
    private static final String CELLS_KEY = "cells";
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";
    private static final StorageCapacity EMPTY_CAPACITY = new StorageCapacity(0, 0);

    private List<ItemStack> cells = emptyCells();
    private BlockPos controllerPos;
    private UUID networkId;

    public DriveBayBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.DRIVE_BAY, pos, state);
    }

    public boolean hasCell() {
        return cells.stream().anyMatch(cell -> !cell.isEmpty());
    }

    public ItemStack cell() {
        return cells.stream()
                .filter(cell -> !cell.isEmpty())
                .findFirst()
                .map(ItemStack::copy)
                .orElse(ItemStack.EMPTY);
    }

    public int cellCount() {
        return (int) cells.stream().filter(cell -> !cell.isEmpty()).count();
    }

    public int freeCellSlots() {
        return CELL_SLOTS - cellCount();
    }

    public boolean refreshControllerLink() {
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (hasValidControllerLink()) {
            return true;
        }
        if (controllerPos != null && !level.isLoaded(controllerPos)) {
            return false;
        }

        int radius = StorageControllerBlockEntity.SCAN_RADIUS;
        BlockPos ownerPos = StorageControllerOwnership.ownerOf(
                        worldPosition,
                        StorageNetworkTopology.reachableControllers(level, worldPosition, radius)
                )
                .orElse(null);
        if (ownerPos == null
                || !(level.getBlockEntity(ownerPos) instanceof StorageControllerBlockEntity controller)) {
            clearControllerLink();
            return false;
        }
        linkTo(controller);
        return true;
    }

    public boolean isLinkedTo(StorageControllerBlockEntity controller) {
        return controllerPos != null
                && networkId != null
                && controllerPos.equals(controller.getBlockPos())
                && networkId.equals(controller.getNetworkId());
    }

    public ItemStack insertCell(ItemStack candidate, TransferMode mode) {
        if (candidate.isEmpty() || !candidate.is(LumungusStorageItems.STORAGE_CELL_16K)) {
            return candidate.copy();
        }

        int slot = firstEmptyCellSlot();
        if (slot < 0) {
            return candidate.copy();
        }
        ItemStack remainder = candidate.getCount() == 1
                ? ItemStack.EMPTY
                : candidate.copyWithCount(candidate.getCount() - 1);
        if (mode == TransferMode.EXECUTE) {
            cells.set(slot, candidate.copyWithCount(1));
            setChanged();
        }
        return remainder;
    }

    public ItemStack removeCell(TransferMode mode) {
        int slot = lastFilledCellSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        }

        ItemStack removed = cells.get(slot).copy();
        if (mode == TransferMode.EXECUTE) {
            cells.set(slot, ItemStack.EMPTY);
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
        StorageCapacity capacity = EMPTY_CAPACITY;
        for (ItemStack cell : cells) {
            if (!cell.isEmpty()) {
                capacity = new StorageCapacity(
                        capacity.maxTotalAmount() + StorageCellData.CAPACITY.maxTotalAmount(),
                        capacity.maxDistinctTypes() + StorageCellData.CAPACITY.maxDistinctTypes()
                );
            }
        }
        return capacity;
    }

    @Override
    public StorageSnapshot snapshot() {
        List<ResourceAmount> resources = storedResources();
        return new StorageSnapshot(
                resources.stream().mapToLong(ResourceAmount::amount).sum(),
                resources.size()
        );
    }

    @Override
    public List<ResourceAmount> storedResources() {
        List<ResourceAmount> resources = new java.util.ArrayList<>();
        for (ItemStack cell : cells) {
            if (!cell.isEmpty()) {
                for (ResourceAmount resource : StorageCellItem.getData(cell).storedResources()) {
                    mergeResource(resources, resource);
                }
            }
        }
        return List.copyOf(resources);
    }

    @Override
    public long count(ItemStack template) {
        long amount = 0;
        for (ItemStack cell : cells) {
            if (!cell.isEmpty()) {
                amount += StorageCellItem.getData(cell).count(template);
            }
        }
        return amount;
    }

    @Override
    public ItemStack insert(ItemStack stack, TransferMode mode) {
        if (!hasCell() || stack.isEmpty() || stack.getItem() instanceof StorageCellItem) {
            return stack.copy();
        }

        ItemStack remainder = stack.copy();
        for (int index = 0; index < cells.size(); index++) {
            ItemStack cell = cells.get(index);
            if (cell.isEmpty()) {
                continue;
            }
            StorageCellData.InsertResult result = StorageCellItem.getData(cell).insert(remainder);
            if (mode == TransferMode.EXECUTE && result.remainder().getCount() != remainder.getCount()) {
                StorageCellItem.setData(cell, result.data());
                cells.set(index, cell);
                setChanged();
            }
            remainder = result.remainder();
            if (remainder.isEmpty()) {
                break;
            }
        }
        return remainder;
    }

    @Override
    public ItemStack extract(ItemStack template, long maxAmount, TransferMode mode) {
        if (!hasCell()) {
            return ItemStack.EMPTY;
        }

        long requested = Math.min(maxAmount, template.getMaxStackSize());
        ItemStack extracted = ItemStack.EMPTY;
        for (int index = 0; index < cells.size() && extracted.getCount() < requested; index++) {
            ItemStack cell = cells.get(index);
            if (cell.isEmpty()) {
                continue;
            }

            StorageCellData.ExtractResult result = StorageCellItem.getData(cell).extract(
                    template,
                    requested - extracted.getCount()
            );
            if (!result.extracted().isEmpty()) {
                if (extracted.isEmpty()) {
                    extracted = result.extracted();
                } else {
                    extracted.grow(result.extracted().getCount());
                }
                if (mode == TransferMode.EXECUTE) {
                    StorageCellItem.setData(cell, result.data());
                    cells.set(index, cell);
                    setChanged();
                }
            }
        }
        return extracted;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            for (int index = 0; index < CELL_SLOTS; index++) {
                ItemStack removed = removeCell(TransferMode.EXECUTE);
                if (!removed.isEmpty()) {
                    Block.popResource(level, pos, removed);
                }
            }
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cells = normalizeCells(input.read(CELLS_KEY, ItemStack.OPTIONAL_CODEC.listOf())
                .orElseGet(() -> input.read(CELL_KEY, ItemStack.OPTIONAL_CODEC)
                        .filter(cell -> !cell.isEmpty())
                        .map(List::of)
                        .orElseGet(List::of)));
        controllerPos = input.read(CONTROLLER_POS_KEY, BlockPos.CODEC).orElse(null);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(CELLS_KEY, ItemStack.OPTIONAL_CODEC.listOf(), cells);
        output.storeNullable(CONTROLLER_POS_KEY, BlockPos.CODEC, controllerPos);
        output.storeNullable(NETWORK_ID_KEY, UUIDUtil.CODEC, networkId);
    }

    private boolean hasValidControllerLink() {
        return controllerPos != null
                && networkId != null
                && level.getBlockEntity(controllerPos) instanceof StorageControllerBlockEntity controller
                && networkId.equals(controller.getNetworkId())
                && StorageNetworkTopology.canReach(
                        level,
                        worldPosition,
                        controllerPos,
                        StorageControllerBlockEntity.SCAN_RADIUS
                );
    }

    private void linkTo(StorageControllerBlockEntity controller) {
        BlockPos newControllerPos = controller.getBlockPos().immutable();
        UUID newNetworkId = controller.getNetworkId();
        if (!newControllerPos.equals(controllerPos) || !newNetworkId.equals(networkId)) {
            controllerPos = newControllerPos;
            networkId = newNetworkId;
            setChanged();
        }
    }

    private void clearControllerLink() {
        if (controllerPos != null || networkId != null) {
            controllerPos = null;
            networkId = null;
            setChanged();
        }
    }

    private int firstEmptyCellSlot() {
        for (int index = 0; index < cells.size(); index++) {
            if (cells.get(index).isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    private int lastFilledCellSlot() {
        for (int index = cells.size() - 1; index >= 0; index--) {
            if (!cells.get(index).isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    private static List<ItemStack> normalizeCells(List<ItemStack> loadedCells) {
        List<ItemStack> normalized = emptyCells();
        int targetIndex = 0;
        for (ItemStack loadedCell : loadedCells) {
            if (targetIndex >= CELL_SLOTS) {
                break;
            }
            if (!loadedCell.isEmpty() && loadedCell.is(LumungusStorageItems.STORAGE_CELL_16K)) {
                normalized.set(targetIndex, loadedCell.copyWithCount(1));
                targetIndex++;
            }
        }
        return normalized;
    }

    private static List<ItemStack> emptyCells() {
        List<ItemStack> emptyCells = new java.util.ArrayList<>(CELL_SLOTS);
        for (int index = 0; index < CELL_SLOTS; index++) {
            emptyCells.add(ItemStack.EMPTY);
        }
        return emptyCells;
    }

    private static void mergeResource(List<ResourceAmount> resources, ResourceAmount candidate) {
        ItemStack candidateStack = candidate.stack();
        for (int index = 0; index < resources.size(); index++) {
            ResourceAmount current = resources.get(index);
            if (ItemStack.isSameItemSameComponents(current.stack(), candidateStack)) {
                resources.set(index, new ResourceAmount(candidateStack, current.amount() + candidate.amount()));
                return;
            }
        }
        resources.add(candidate);
    }
}
