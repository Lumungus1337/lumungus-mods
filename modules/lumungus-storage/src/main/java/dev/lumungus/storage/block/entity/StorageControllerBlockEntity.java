package dev.lumungus.storage.block.entity;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageAccess;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageProvider;
import dev.lumungus.core.api.storage.StorageSnapshot;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            return new NetworkStatus(0, 0, 0);
        }

        int linkedTerminals = 0;
        int linkedDriveBays = 0;
        int linkedInventoryConnectors = 0;
        for (BlockPos candidate : StorageNetworkTopology.reachableNodes(level, worldPosition, SCAN_RADIUS)) {
            if (level.getBlockEntity(candidate) instanceof CraftingTerminalBlockEntity terminal
                    && terminal.refreshControllerLink()
                    && terminal.isLinkedTo(this)) {
                linkedTerminals++;
            } else if (level.getBlockEntity(candidate) instanceof DriveBayBlockEntity driveBay
                    && driveBay.refreshControllerLink()
                    && driveBay.isLinkedTo(this)) {
                linkedDriveBays++;
            } else if (level.getBlockEntity(candidate) instanceof InventoryConnectorBlockEntity connector
                    && connector.refreshControllerLink()
                    && connector.isLinkedTo(this)) {
                linkedInventoryConnectors++;
            }
        }

        return new NetworkStatus(linkedTerminals, linkedDriveBays, linkedInventoryConnectors);
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

        List<StorageAccess> storageAccesses = storageAccesses();
        List<ResourceAmount> aggregated = new ArrayList<>();
        long maxTotal = 0;
        int maxTypes = 0;
        for (StorageAccess storageAccess : storageAccesses) {
            StorageCapacity accessCapacity = storageAccess.capacity();
            maxTotal += accessCapacity.maxTotalAmount();
            maxTypes += accessCapacity.maxDistinctTypes();
            for (ResourceAmount resource : storageAccess.storedResources()) {
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
        return storageAccesses().stream().mapToLong(access -> access.count(template)).sum();
    }

    @Override
    public ItemStack insert(ItemStack stack, TransferMode mode) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<StorageAccess> ordered = new ArrayList<>(storageAccesses());
        ordered.sort(Comparator.comparingInt(access -> access.contains(stack) ? 0 : 1));

        ItemStack remainder = stack.copy();
        for (StorageAccess storageAccess : ordered) {
            remainder = storageAccess.insert(remainder, mode);
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
        for (StorageAccess storageAccess : storageAccesses()) {
            ItemStack part = storageAccess.extract(template, requested - result.getCount(), mode);
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

    private List<StorageAccess> storageAccesses() {
        if (level == null || level.isClientSide()) {
            return List.of();
        }

        List<StorageAccess> storageAccesses = new ArrayList<>();
        Map<BlockPos, StorageAccess> physicalInventories = new LinkedHashMap<>();
        for (BlockPos candidate : StorageNetworkTopology.reachableNodes(level, worldPosition, SCAN_RADIUS)) {
            if (level.getBlockEntity(candidate) instanceof DriveBayBlockEntity driveBay
                    && driveBay.refreshControllerLink()
                    && driveBay.isLinkedTo(this)) {
                storageAccesses.add(driveBay.storageAccess());
            } else if (level.getBlockEntity(candidate) instanceof InventoryConnectorBlockEntity connector
                    && connector.refreshControllerLink()
                    && connector.isLinkedTo(this)) {
                connector.endpoints().forEach(endpoint -> physicalInventories.putIfAbsent(
                        endpoint.key(),
                        endpoint.access()
                ));
            }
        }
        storageAccesses.addAll(physicalInventories.values());
        return storageAccesses;
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

    public record NetworkStatus(int linkedTerminals, int linkedDriveBays, int linkedInventoryConnectors) {
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
