package dev.lumungus.storage.block.entity;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.storage.block.StorageOutputBlock;
import dev.lumungus.storage.block.WorkBlockPower;
import dev.lumungus.storage.inventory.FabricItemStorageAccess;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import dev.lumungus.storage.wireless.WirelessModuleBinding;
import dev.lumungus.storage.wireless.WirelessModuleHost;
import java.util.Comparator;
import java.util.UUID;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class StorageOutputBlockEntity extends BlockEntity implements WirelessModuleHost {
    private static final String FILTER_KEY = "filter";
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";
    private static final String WIRELESS_MODULE_KEY = "wireless_module";
    private static final int TRANSFER_INTERVAL_TICKS = 8;

    private ItemStack filter = ItemStack.EMPTY;
    private BlockPos controllerPos;
    private UUID networkId;
    private ItemStack wirelessModule = ItemStack.EMPTY;

    public StorageOutputBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.STORAGE_OUTPUT, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageOutputBlockEntity output) {
        if (level.getGameTime() % TRANSFER_INTERVAL_TICKS == 0) {
            output.exportOneStack();
        }
    }

    public ItemStack filter() {
        return filter.copy();
    }

    public void setFilter(ItemStack stack) {
        filter = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        setChanged();
    }

    public void clearFilter() {
        setFilter(ItemStack.EMPTY);
    }

    public boolean refreshControllerLink() {
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (!wirelessModule.isEmpty()) {
            StorageControllerBlockEntity controller = WirelessModuleBinding.resolve(level, wirelessModule);
            if (controller == null) {
                clearControllerLink();
                return false;
            }
            linkTo(controller);
            return true;
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

    public void exportOneStack() {
        StorageControllerBlockEntity controller = linkedController();
        Direction direction = getBlockState().getValue(StorageOutputBlock.FACING);
        if (controller == null || WorkBlockPower.isPaused(level, worldPosition, direction.getOpposite())) {
            return;
        }

        ItemStack template = exportTemplate(controller);
        if (template.isEmpty()) {
            return;
        }

        BlockPos targetPos = worldPosition.relative(direction);
        if (!level.isLoaded(targetPos)
                || StorageNetworkTopology.isDeviceNode(level.getBlockState(targetPos).getBlock())) {
            return;
        }
        Storage<ItemVariant> target = ItemStorage.SIDED.find(level, targetPos, direction.getOpposite());
        if (target == null) {
            return;
        }

        FabricItemStorageAccess access = new FabricItemStorageAccess(target);
        ItemStack available = controller.extract(template, template.getMaxStackSize(), TransferMode.SIMULATE);
        if (available.isEmpty()) {
            return;
        }
        ItemStack simulatedRemainder = access.insert(available, TransferMode.SIMULATE);
        int movable = available.getCount() - simulatedRemainder.getCount();
        if (movable <= 0) {
            return;
        }

        ItemStack extracted = controller.extract(template, movable, TransferMode.EXECUTE);
        ItemStack remainder = access.insert(extracted, TransferMode.EXECUTE);
        if (!remainder.isEmpty()) {
            controller.insert(remainder, TransferMode.EXECUTE);
        }
    }

    private ItemStack exportTemplate(StorageControllerBlockEntity controller) {
        if (!filter.isEmpty() && controller.count(filter) > 0) {
            return filter.copyWithCount(filter.getMaxStackSize());
        }
        if (!filter.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return controller.storedResources().stream()
                .max(Comparator.comparingLong(ResourceAmount::amount))
                .map(resource -> resource.stack().copyWithCount(resource.stack().getMaxStackSize()))
                .orElse(ItemStack.EMPTY);
    }

    private StorageControllerBlockEntity linkedController() {
        if (!refreshControllerLink() || level == null || controllerPos == null) {
            return null;
        }
        if (!wirelessModule.isEmpty()) {
            StorageControllerBlockEntity controller = WirelessModuleBinding.resolve(level, wirelessModule);
            return controller != null && isLinkedTo(controller) ? controller : null;
        }
        if (level.getBlockEntity(controllerPos) instanceof StorageControllerBlockEntity controller
                && isLinkedTo(controller)) {
            return controller;
        }
        return null;
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
        filter = input.read(FILTER_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        controllerPos = input.read(CONTROLLER_POS_KEY, BlockPos.CODEC).orElse(null);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElse(null);
        wirelessModule = input.read(WIRELESS_MODULE_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(FILTER_KEY, ItemStack.OPTIONAL_CODEC, filter);
        output.storeNullable(CONTROLLER_POS_KEY, BlockPos.CODEC, controllerPos);
        output.storeNullable(NETWORK_ID_KEY, UUIDUtil.CODEC, networkId);
        output.store(WIRELESS_MODULE_KEY, ItemStack.OPTIONAL_CODEC, wirelessModule);
    }
}
