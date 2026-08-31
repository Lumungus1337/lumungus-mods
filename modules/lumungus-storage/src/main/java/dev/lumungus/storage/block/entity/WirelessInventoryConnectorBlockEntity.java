package dev.lumungus.storage.block.entity;

import dev.lumungus.storage.block.WirelessInventoryConnectorBlock;
import dev.lumungus.storage.inventory.FabricItemStorageAccess;
import dev.lumungus.storage.inventory.PhysicalInventoryEndpoint;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.network.WirelessInventoryConnectorRegistry;
import dev.lumungus.storage.network.WirelessStorageControllerRegistry;
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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WirelessInventoryConnectorBlockEntity extends BlockEntity {
    private static final String CONTROLLER_DIMENSION_KEY = "controller_dimension";
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";

    private Identifier controllerDimension;
    private BlockPos controllerPos;
    private UUID networkId;

    public WirelessInventoryConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.WIRELESS_INVENTORY_CONNECTOR, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WirelessInventoryConnectorBlockEntity connector) {
        if (level.getGameTime() % 40 == 0) {
            WirelessInventoryConnectorRegistry.register(level, pos);
            connector.refreshControllerLink();
        }
    }

    public boolean refreshControllerLink() {
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (hasValidControllerLink()) {
            return true;
        }

        StorageControllerBlockEntity controller = findControllerViaWirelessController();
        if (controller == null) {
            clearControllerLink();
            return false;
        }
        linkTo(controller);
        return true;
    }

    public boolean isLinkedTo(StorageControllerBlockEntity controller) {
        return controllerDimension != null
                && controllerPos != null
                && networkId != null
                && controllerDimension.equals(controller.getLevel().dimension().identifier())
                && controllerPos.equals(controller.getBlockPos())
                && networkId.equals(controller.getNetworkId());
    }

    public BlockPos linkedControllerPosition() {
        StorageControllerBlockEntity controller = linkedControllerBlockEntity();
        return controller != null && isLinkedTo(controller) ? controller.getBlockPos() : null;
    }

    public List<PhysicalInventoryEndpoint> endpoints() {
        if (level == null || level.isClientSide()) {
            return List.of();
        }

        List<PhysicalInventoryEndpoint> endpoints = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            BlockPos inventoryPos = worldPosition.relative(direction);
            if (!level.isLoaded(inventoryPos)
                    || StorageNetworkTopology.isDeviceNode(level.getBlockState(inventoryPos).getBlock())) {
                continue;
            }
            Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, inventoryPos, direction.getOpposite());
            if (storage != null) {
                endpoints.add(new PhysicalInventoryEndpoint(
                        canonicalInventoryPos(inventoryPos),
                        new FabricItemStorageAccess(storage)
                ));
            }
        }
        return List.copyOf(endpoints);
    }

    private StorageControllerBlockEntity findControllerViaWirelessController() {
        WirelessInventoryConnectorBlock.WirelessConnectorTier tier = tier();
        int radius = tier.searchRadius();
        StorageControllerBlockEntity nearestController = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos candidate : StorageNetworkTopology.reachableNodes(level, worldPosition, radius)) {
            if (level.getBlockEntity(candidate) instanceof WirelessStorageControllerBlockEntity wireless
                    && tier.canReach(true, worldPosition, candidate)) {
                StorageControllerBlockEntity controller = wireless.linkedController();
                if (controller != null) {
                    double distance = worldPosition.distSqr(wireless.getBlockPos());
                    if (distance < nearestDistance) {
                        nearestController = controller;
                        nearestDistance = distance;
                    }
                }
            }
        }
        if (nearestController != null) {
            return nearestController;
        }

        if (level.getServer() == null) {
            return null;
        }
        for (WirelessStorageControllerBlockEntity wireless : WirelessStorageControllerRegistry.loadedControllers(level.getServer())) {
            boolean sameDimension = wireless.getLevel().dimension().identifier().equals(level.dimension().identifier());
            if (tier.canReach(sameDimension, worldPosition, wireless.getBlockPos())) {
                StorageControllerBlockEntity controller = wireless.linkedController();
                if (controller != null) {
                    double distance = sameDimension ? worldPosition.distSqr(wireless.getBlockPos()) : Double.MAX_VALUE - 1;
                    if (distance < nearestDistance) {
                        nearestController = controller;
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearestController;
    }

    private WirelessInventoryConnectorBlock.WirelessConnectorTier tier() {
        return level == null
                ? WirelessInventoryConnectorBlock.WirelessConnectorTier.SHORT_RANGE
                : WirelessInventoryConnectorBlock.tierFor(getBlockState().getBlock());
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
        StorageControllerBlockEntity controller = linkedControllerBlockEntity();
        return controllerDimension != null
                && controllerPos != null
                && networkId != null
                && controller != null
                && networkId.equals(controller.getNetworkId());
    }

    private StorageControllerBlockEntity linkedControllerBlockEntity() {
        if (level == null || level.getServer() == null || controllerDimension == null || controllerPos == null) {
            return null;
        }
        for (net.minecraft.server.level.ServerLevel serverLevel : level.getServer().getAllLevels()) {
            if (!serverLevel.dimension().identifier().equals(controllerDimension)) {
                continue;
            }
            if (serverLevel.isLoaded(controllerPos)
                    && serverLevel.getBlockEntity(controllerPos) instanceof StorageControllerBlockEntity controller) {
                return controller;
            }
            return null;
        }
        return null;
    }

    private void linkTo(StorageControllerBlockEntity controller) {
        Identifier newControllerDimension = controller.getLevel().dimension().identifier();
        BlockPos newControllerPos = controller.getBlockPos().immutable();
        UUID newNetworkId = controller.getNetworkId();
        if (!newControllerDimension.equals(controllerDimension)
                || !newControllerPos.equals(controllerPos)
                || !newNetworkId.equals(networkId)) {
            controllerDimension = newControllerDimension;
            controllerPos = newControllerPos;
            networkId = newNetworkId;
            setChanged();
        }
    }

    private void clearControllerLink() {
        if (controllerDimension != null || controllerPos != null || networkId != null) {
            controllerDimension = null;
            controllerPos = null;
            networkId = null;
            setChanged();
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        controllerDimension = input.read(CONTROLLER_DIMENSION_KEY, Identifier.CODEC).orElse(null);
        controllerPos = input.read(CONTROLLER_POS_KEY, BlockPos.CODEC).orElse(null);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(CONTROLLER_DIMENSION_KEY, Identifier.CODEC, controllerDimension);
        output.storeNullable(CONTROLLER_POS_KEY, BlockPos.CODEC, controllerPos);
        output.storeNullable(NETWORK_ID_KEY, UUIDUtil.CODEC, networkId);
    }
}
