package dev.lumungus.storage.block.entity;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageAccess;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageProvider;
import dev.lumungus.core.api.storage.StorageSnapshot;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class StorageControllerBlockEntity extends BlockEntity implements StorageAccess, StorageProvider {
    public static final int SCAN_RADIUS = 8;

    private static final String NETWORK_ID_KEY = "network_id";

    private UUID networkId = UUID.randomUUID();
    private long cachedNetworkStateTick = Long.MIN_VALUE;
    private NetworkState cachedNetworkState;

    public StorageControllerBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.STORAGE_CONTROLLER, pos, state);
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public String getNetworkLabel() {
        return networkId.toString().substring(0, 8).toUpperCase();
    }

    public NetworkStatus refreshNetwork() {
        if (level == null || level.isClientSide()) {
            return new NetworkStatus(0, 0);
        }

        int linkedTerminals = 0;
        int linkedDriveBays = 0;
        BlockPos min = worldPosition.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS);
        BlockPos max = worldPosition.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);

        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(candidate) instanceof CraftingTerminalBlockEntity terminal
                    && terminal.refreshControllerLink()
                    && terminal.isLinkedTo(this)) {
                linkedTerminals++;
            } else if (level.getBlockEntity(candidate) instanceof DriveBayBlockEntity driveBay
                    && driveBay.refreshControllerLink()
                    && driveBay.isLinkedTo(this)) {
                linkedDriveBays++;
            }
        }

        return new NetworkStatus(linkedTerminals, linkedDriveBays);
    }

    @Override
    public StorageAccess storageAccess() {
        return this;
    }

    @Override
    public StorageCapacity capacity() {
        return networkState().capacity();
    }

    @Override
    public StorageSnapshot snapshot() {
        return networkState().snapshot();
    }

    @Override
    public List<ResourceAmount> storedResources() {
        return networkState().resources();
    }

    public NetworkState networkState() {
        long gameTime = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (cachedNetworkState != null && cachedNetworkStateTick == gameTime) {
            return cachedNetworkState;
        }

        List<DriveBayBlockEntity> driveBays = driveBays();
        List<ResourceAmount> aggregated = new ArrayList<>();
        long maxTotal = 0;
        int maxTypes = 0;
        for (DriveBayBlockEntity driveBay : driveBays) {
            StorageCapacity driveCapacity = driveBay.capacity();
            maxTotal += driveCapacity.maxTotalAmount();
            maxTypes += driveCapacity.maxDistinctTypes();
            for (ResourceAmount resource : driveBay.storedResources()) {
                mergeResource(aggregated, resource);
            }
        }
        List<ResourceAmount> resources = List.copyOf(aggregated);
        long total = resources.stream().mapToLong(ResourceAmount::amount).sum();
        NetworkState state = new NetworkState(
                resources,
                new StorageSnapshot(total, resources.size()),
                new StorageCapacity(maxTotal, maxTypes)
        );
        cachedNetworkState = state;
        cachedNetworkStateTick = gameTime;
        return state;
    }

    @Override
    public long count(ItemStack template) {
        return driveBays().stream().mapToLong(driveBay -> driveBay.count(template)).sum();
    }

    @Override
    public ItemStack insert(ItemStack stack, TransferMode mode) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<DriveBayBlockEntity> ordered = new ArrayList<>(driveBays());
        ordered.sort(Comparator.comparingInt(driveBay -> driveBay.contains(stack) ? 0 : 1));

        ItemStack remainder = stack.copy();
        for (DriveBayBlockEntity driveBay : ordered) {
            remainder = driveBay.insert(remainder, mode);
            if (remainder.isEmpty()) {
                break;
            }
        }
        if (mode == TransferMode.EXECUTE && remainder.getCount() != stack.getCount()) {
            invalidateNetworkState();
        }
        return remainder;
    }

    @Override
    public ItemStack extract(ItemStack template, long maxAmount, TransferMode mode) {
        if (template.isEmpty() || maxAmount <= 0) {
            return ItemStack.EMPTY;
        }

        long requested = Math.min(maxAmount, template.getMaxStackSize());
        ItemStack result = ItemStack.EMPTY;
        for (DriveBayBlockEntity driveBay : driveBays()) {
            ItemStack part = driveBay.extract(template, requested - result.getCount(), mode);
            if (!part.isEmpty()) {
                if (result.isEmpty()) {
                    result = part.copy();
                } else {
                    result.grow(part.getCount());
                }
            }
            if (result.getCount() >= requested) {
                break;
            }
        }
        if (mode == TransferMode.EXECUTE && !result.isEmpty()) {
            invalidateNetworkState();
        }
        return result;
    }

    private List<DriveBayBlockEntity> driveBays() {
        if (level == null || level.isClientSide()) {
            return List.of();
        }

        List<DriveBayBlockEntity> driveBays = new ArrayList<>();
        BlockPos min = worldPosition.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS);
        BlockPos max = worldPosition.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(candidate) instanceof DriveBayBlockEntity driveBay
                    && driveBay.refreshControllerLink()
                    && driveBay.isLinkedTo(this)) {
                driveBays.add(driveBay);
            }
        }
        return driveBays;
    }

    private static void mergeResource(List<ResourceAmount> aggregated, ResourceAmount candidate) {
        ItemStack candidateStack = candidate.stack();
        for (int index = 0; index < aggregated.size(); index++) {
            ResourceAmount current = aggregated.get(index);
            if (ItemStack.isSameItemSameComponents(current.stack(), candidateStack)) {
                aggregated.set(index, new ResourceAmount(candidateStack, current.amount() + candidate.amount()));
                return;
            }
        }
        aggregated.add(candidate);
    }

    private void invalidateNetworkState() {
        cachedNetworkStateTick = Long.MIN_VALUE;
        cachedNetworkState = null;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        networkId = input.read(NETWORK_ID_KEY, UUIDUtil.CODEC).orElseGet(UUID::randomUUID);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(NETWORK_ID_KEY, UUIDUtil.CODEC, networkId);
    }

    public record NetworkStatus(int linkedTerminals, int linkedDriveBays) {
    }

    public record NetworkState(
            List<ResourceAmount> resources,
            StorageSnapshot snapshot,
            StorageCapacity capacity
    ) {
        public NetworkState {
            resources = List.copyOf(resources);
        }

        @Override
        public List<ResourceAmount> resources() {
            return resources;
        }
    }
}
