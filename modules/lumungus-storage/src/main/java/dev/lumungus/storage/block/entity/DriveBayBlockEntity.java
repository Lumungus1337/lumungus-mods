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
    private static final String CELL_KEY = "cell";
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";
    private static final StorageCapacity EMPTY_CAPACITY = new StorageCapacity(0, 0);

    private ItemStack cell = ItemStack.EMPTY;
    private BlockPos controllerPos;
    private UUID networkId;

    public DriveBayBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.DRIVE_BAY, pos, state);
    }

    public boolean hasCell() {
        return !cell.isEmpty();
    }

    public ItemStack cell() {
        return cell.copy();
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
        controllerPos = input.read(CONTROLLER_POS_KEY, BlockPos.CODEC).orElse(null);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElse(null);
        if (!cell.isEmpty() && !cell.is(LumungusStorageItems.STORAGE_CELL_16K)) {
            cell = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(CELL_KEY, ItemStack.OPTIONAL_CODEC, cell);
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

    private StorageCellData cellData() {
        return StorageCellItem.getData(cell);
    }
}
