package dev.lumungus.storage.block.entity;

import dev.lumungus.storage.block.WirelessInventoryConnectorBlock;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.inventory.FabricItemStorageAccess;
import dev.lumungus.storage.inventory.PhysicalInventoryEndpoint;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.network.WirelessInventoryConnectorRegistry;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.wireless.WirelessModuleBinding;
import dev.lumungus.storage.wireless.WirelessModuleHost;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WirelessInventoryConnectorBlockEntity extends BlockEntity implements WirelessModuleHost {
    private static final String CONTROLLER_DIMENSION_KEY = "controller_dimension";
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";
    private static final String AUTO_SEND_KEY = "auto_send_to_drive_bays";
    private static final String WIRELESS_MODULE_KEY = "wireless_module";
    private static final int AUTO_SEND_INTERVAL_TICKS = 20;
    private static final int AUTO_SEND_STACKS_PER_CYCLE = 64;

    private Identifier controllerDimension;
    private BlockPos controllerPos;
    private UUID networkId;
    private boolean autoSendToDriveBays;
    private ItemStack wirelessModule = ItemStack.EMPTY;
    private long autoSendAttempts;
    private int maintenanceCooldown;

    public WirelessInventoryConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.WIRELESS_INVENTORY_CONNECTOR, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WirelessInventoryConnectorBlockEntity connector) {
        if (connector.maintenanceCooldown > 0) {
            connector.maintenanceCooldown--;
            return;
        }
        connector.maintenanceCooldown = AUTO_SEND_INTERVAL_TICKS - 1;
        WirelessInventoryConnectorRegistry.register(level, pos);
        connector.refreshControllerLink();
        if (connector.autoSendToDriveBays) {
            connector.runAutoSendCycle();
        }
    }

    public boolean autoSendToDriveBays() {
        return autoSendToDriveBays;
    }

    public long autoSendAttempts() {
        return autoSendAttempts;
    }

    public boolean toggleAutoSendToDriveBays() {
        autoSendToDriveBays = !autoSendToDriveBays;
        if (autoSendToDriveBays && level != null && !level.isClientSide()) {
            maintenanceCooldown = AUTO_SEND_INTERVAL_TICKS - 1;
            runAutoSendCycle();
        }
        setChanged();
        return autoSendToDriveBays;
    }

    private void runAutoSendCycle() {
        autoSendAttempts++;
        sendToDriveBays();
    }

    public StorageControllerBlockEntity.BayMoveResult sendToDriveBays() {
        StorageControllerBlockEntity controller = linkedControllerBlockEntity();
        return controller == null || !isLinkedTo(controller)
                ? new StorageControllerBlockEntity.BayMoveResult(0, 0, endpoints().size(), 0, false)
                : controller.moveInventoriesIntoDriveBays(endpoints(), AUTO_SEND_STACKS_PER_CYCLE);
    }

    public boolean refreshControllerLink() {
        if (level == null || level.isClientSide()) {
            return false;
        }
        StorageControllerBlockEntity controller = WirelessModuleBinding.resolve(level, wirelessModule);
        if (controller == null || !tier().canReach(
                controller.getLevel().dimension().identifier().equals(level.dimension().identifier()),
                worldPosition,
                controller.getBlockPos()
        )) {
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

    private StorageControllerBlockEntity linkedControllerBlockEntity() {
        if (level == null || level.getServer() == null || controllerDimension == null || controllerPos == null) {
            return null;
        }
        for (net.minecraft.server.level.ServerLevel serverLevel : level.getServer().getAllLevels()) {
            if (!serverLevel.dimension().identifier().equals(controllerDimension)) {
                continue;
            }
            if (!serverLevel.isLoaded(controllerPos)
                    && tier() == WirelessInventoryConnectorBlock.WirelessConnectorTier.MULTIDIMENSIONAL) {
                serverLevel.getChunkAt(controllerPos);
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
    public ItemStack wirelessModule() {
        return wirelessModule.copy();
    }

    @Override
    public boolean installWirelessModule(ItemStack module) {
        if (!wirelessModule.isEmpty() || !WirelessModuleBinding.isPrimedModule(module)) {
            return false;
        }
        wirelessModule = module.copyWithCount(1);
        clearControllerLink();
        setChanged();
        return true;
    }

    @Override
    public ItemStack removeWirelessModule() {
        ItemStack removed = wirelessModule;
        wirelessModule = ItemStack.EMPTY;
        clearControllerLink();
        setChanged();
        return removed;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            dropWirelessModule(level, pos);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        controllerDimension = input.read(CONTROLLER_DIMENSION_KEY, Identifier.CODEC).orElse(null);
        controllerPos = input.read(CONTROLLER_POS_KEY, BlockPos.CODEC).orElse(null);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElse(null);
        autoSendToDriveBays = input.read(AUTO_SEND_KEY, Codec.BOOL).orElse(false);
        wirelessModule = input.read(WIRELESS_MODULE_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        migrateLegacyBindingToModule();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(CONTROLLER_DIMENSION_KEY, Identifier.CODEC, controllerDimension);
        output.storeNullable(CONTROLLER_POS_KEY, BlockPos.CODEC, controllerPos);
        output.storeNullable(NETWORK_ID_KEY, UUIDUtil.CODEC, networkId);
        output.store(AUTO_SEND_KEY, Codec.BOOL, autoSendToDriveBays);
        output.store(WIRELESS_MODULE_KEY, ItemStack.OPTIONAL_CODEC, wirelessModule);
    }

    private void migrateLegacyBindingToModule() {
        if (!wirelessModule.isEmpty() || controllerDimension == null || controllerPos == null || networkId == null) {
            return;
        }
        wirelessModule = new ItemStack(LumungusStorageItems.WIRELESS_NETWORK_MODULE);
        wirelessModule.set(
                LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER,
                new BoundStorageController(controllerDimension, controllerPos.immutable(), networkId)
        );
    }
}
