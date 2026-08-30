package dev.lumungus.storage.block.entity;

import dev.lumungus.storage.inventory.FabricItemStorageAccess;
import dev.lumungus.storage.inventory.PhysicalInventoryEndpoint;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class InventoryConnectorBlockEntity extends BlockEntity {
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";

    private BlockPos controllerPos;
    private UUID networkId;

    public InventoryConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.INVENTORY_CONNECTOR, pos, state);
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

    public List<PhysicalInventoryEndpoint> endpoints() {
        if (level == null || level.isClientSide()) {
            return List.of();
        }

        List<PhysicalInventoryEndpoint> endpoints = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos inventoryPos = worldPosition.relative(direction);
            if (!level.isLoaded(inventoryPos)) {
                continue;
            }
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(
                    level,
                    inventoryPos,
                    direction.getOpposite()
            );
            if (storage != null) {
                endpoints.add(new PhysicalInventoryEndpoint(
                        canonicalInventoryPos(inventoryPos),
                        new FabricItemStorageAccess(storage)
                ));
            }
        }
        return List.copyOf(endpoints);
    }

    private BlockPos canonicalInventoryPos(BlockPos inventoryPos) {
        BlockState state = level.getBlockState(inventoryPos);
        if (!(state.getBlock() instanceof ChestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            return inventoryPos;
        }
        BlockPos connectedPos = ChestBlock.getConnectedBlockPos(inventoryPos, state);
        return inventoryPos.asLong() <= connectedPos.asLong() ? inventoryPos : connectedPos;
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

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        controllerPos = input.read(CONTROLLER_POS_KEY, BlockPos.CODEC).orElse(null);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(CONTROLLER_POS_KEY, BlockPos.CODEC, controllerPos);
        output.storeNullable(NETWORK_ID_KEY, UUIDUtil.CODEC, networkId);
    }
}
