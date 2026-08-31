package dev.lumungus.storage.block.entity;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.storage.block.StorageBreakerBlock;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class StorageBreakerBlockEntity extends BlockEntity {
    private static final String FILTER_KEY = "filter";
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";
    private static final int WORK_INTERVAL_TICKS = 20;

    private ItemStack filter = ItemStack.EMPTY;
    private BlockPos controllerPos;
    private UUID networkId;

    public StorageBreakerBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.STORAGE_BREAKER, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageBreakerBlockEntity breaker) {
        if (level.getGameTime() % WORK_INTERVAL_TICKS == 0) {
            breaker.breakBelow();
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

    public void breakBelow() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        StorageControllerBlockEntity controller = linkedController();
        if (controller == null) {
            return;
        }

        BlockPos targetPos = worldPosition.relative(getBlockState().getValue(StorageBreakerBlock.FACING));
        BlockState targetState = level.getBlockState(targetPos);
        if (targetState.isAir()
                || targetState.is(Blocks.BEDROCK)
                || targetState.getDestroySpeed(level, targetPos) < 0
                || !matchesFilter(targetState)) {
            return;
        }

        BlockEntity targetEntity = level.getBlockEntity(targetPos);
        LootParams.Builder lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(targetPos))
                .withParameter(LootContextParams.BLOCK_STATE, targetState)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, targetEntity);
        List<ItemStack> drops = targetState.getDrops(lootParams);
        for (ItemStack drop : drops) {
            if (!controller.insert(drop, TransferMode.SIMULATE).isEmpty()) {
                return;
            }
        }

        level.removeBlock(targetPos, false);
        drops.forEach(drop -> controller.insert(drop, TransferMode.EXECUTE));
    }

    private boolean matchesFilter(BlockState targetState) {
        return filter.isEmpty()
                || filter.getItem() instanceof BlockItem blockItem
                && targetState.is(blockItem.getBlock());
    }

    private StorageControllerBlockEntity linkedController() {
        if (!refreshControllerLink() || level == null || controllerPos == null) {
            return null;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        filter = input.read(FILTER_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        controllerPos = input.read(CONTROLLER_POS_KEY, BlockPos.CODEC).orElse(null);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(FILTER_KEY, ItemStack.OPTIONAL_CODEC, filter);
        output.storeNullable(CONTROLLER_POS_KEY, BlockPos.CODEC, controllerPos);
        output.storeNullable(NETWORK_ID_KEY, UUIDUtil.CODEC, networkId);
    }
}
