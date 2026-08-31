package dev.lumungus.storage.block.entity;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.api.storage.StorageAccess;
import dev.lumungus.core.api.storage.StorageCapacity;
import dev.lumungus.core.api.storage.StorageProvider;
import dev.lumungus.core.api.storage.StorageSnapshot;
import dev.lumungus.storage.network.StorageControllerRegistry;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.network.WirelessInventoryConnectorRegistry;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import dev.lumungus.storage.storage.ShulkerBoxTransfer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class StorageControllerBlockEntity extends BlockEntity implements StorageAccess, StorageProvider {
    public static final int SCAN_RADIUS = 8;

    private static final String NETWORK_ID_KEY = "network_id";
    private static final int MAX_BAY_MOVE_STACKS_PER_ACTION = 8192;

    private UUID networkId = UUID.randomUUID();
    private long cachedNetworkStateTick = Long.MIN_VALUE;
    private NetworkState cachedNetworkState;

    public StorageControllerBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.STORAGE_CONTROLLER, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StorageControllerBlockEntity controller) {
        if (level.getGameTime() % 100 == 0) {
            StorageControllerRegistry.register(level, pos);
        }
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
        for (WirelessInventoryConnectorBlockEntity connector : WirelessInventoryConnectorRegistry.linkedTo(this)) {
            if (!connector.endpoints().isEmpty()) {
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

        NetworkStorageAccesses networkAccesses = storageAccesses();
        List<ResourceAmount> aggregated = new ArrayList<>();
        long maxTotal = 0;
        int maxTypes = 0;
        for (StorageAccess storageAccess : networkAccesses.all()) {
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
        return storageAccesses().all().stream().mapToLong(access -> access.count(template)).sum();
    }

    @Override
    public ItemStack insert(ItemStack stack, TransferMode mode) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<StorageAccess> ordered = new ArrayList<>(storageAccesses().all());
        ordered.sort(Comparator.comparingInt(access -> access.contains(stack) ? 0 : 1));
        if (ShulkerBoxTransfer.isFilledShulkerBox(stack)) {
            ItemStack remainder = insertUnpackedShulker(ordered, stack, mode);
            if (mode == TransferMode.EXECUTE && remainder.isEmpty()) {
                invalidateNetworkState();
            }
            return remainder;
        }

        ItemStack remainder = insertIntoAccesses(ordered, stack, mode);
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
        for (StorageAccess storageAccess : storageAccesses().all()) {
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

    public BayMoveResult movePhysicalInventoriesIntoDriveBays() {
        NetworkStorageAccesses networkAccesses = storageAccesses();
        if (networkAccesses.driveBays().isEmpty() || networkAccesses.physicalInventories().isEmpty()) {
            return new BayMoveResult(0, 0, networkAccesses.physicalInventories().size(), networkAccesses.driveBays().size(), false);
        }

        long moved = 0;
        long remaining = 0;
        int movedStacks = 0;
        boolean paused = false;
        for (StorageAccess physicalInventory : networkAccesses.physicalInventories()) {
            for (ResourceAmount resource : physicalInventory.storedResources()) {
                ItemStack template = resource.stack();
                long sourceRemaining = resource.amount();
                while (sourceRemaining > 0) {
                    if (movedStacks >= MAX_BAY_MOVE_STACKS_PER_ACTION) {
                        paused = true;
                        remaining += sourceRemaining;
                        break;
                    }

                    int requested = (int) Math.min(template.getMaxStackSize(), sourceRemaining);
                    ItemStack extractable = physicalInventory.extract(template, requested, TransferMode.SIMULATE);
                    if (extractable.isEmpty()) {
                        break;
                    }

                    ItemStack simulatedRemainder = insertIntoDriveBays(
                            networkAccesses.driveBays(),
                            extractable,
                            TransferMode.SIMULATE
                    );
                    int movable = extractable.getCount() - simulatedRemainder.getCount();
                    if (movable <= 0) {
                        remaining += sourceRemaining;
                        break;
                    }

                    ItemStack extracted = physicalInventory.extract(template, movable, TransferMode.EXECUTE);
                    if (extracted.isEmpty()) {
                        break;
                    }

                    ItemStack insertRemainder = insertIntoDriveBays(networkAccesses.driveBays(), extracted, TransferMode.EXECUTE);
                    int inserted = extracted.getCount() - insertRemainder.getCount();
                    if (!insertRemainder.isEmpty()) {
                        physicalInventory.insert(insertRemainder, TransferMode.EXECUTE);
                    }
                    if (inserted <= 0) {
                        remaining += sourceRemaining;
                        break;
                    }

                    moved += inserted;
                    movedStacks++;
                    sourceRemaining -= inserted;
                    if (!insertRemainder.isEmpty()) {
                        remaining += sourceRemaining;
                        break;
                    }
                }
                if (paused) {
                    break;
                }
            }
            if (paused) {
                break;
            }
        }

        if (moved > 0) {
            invalidateNetworkState();
        }
        return new BayMoveResult(
                moved,
                remaining,
                networkAccesses.physicalInventories().size(),
                networkAccesses.driveBays().size(),
                paused
        );
    }

    private static ItemStack insertIntoDriveBays(
            List<StorageAccess> driveBays,
            ItemStack stack,
            TransferMode mode
    ) {
        if (ShulkerBoxTransfer.isFilledShulkerBox(stack)) {
            return insertUnpackedShulker(driveBays, stack, mode);
        }
        return insertIntoAccesses(driveBays, stack, mode);
    }

    private static ItemStack insertIntoAccesses(
            List<StorageAccess> accesses,
            ItemStack stack,
            TransferMode mode
    ) {
        ItemStack remainder = stack.copy();
        for (StorageAccess driveBay : accesses) {
            remainder = driveBay.insert(remainder, mode);
            if (remainder.isEmpty()) {
                break;
            }
        }
        return remainder;
    }

    private static ItemStack insertUnpackedShulker(
            List<StorageAccess> accesses,
            ItemStack shulkerBox,
            TransferMode mode
    ) {
        List<ItemStack> parts = new ArrayList<>(ShulkerBoxTransfer.unpackedContents(shulkerBox));
        parts.add(ShulkerBoxTransfer.emptyCopy(shulkerBox));
        if (mode == TransferMode.SIMULATE) {
            for (ItemStack part : parts) {
                if (!insertIntoAccesses(accesses, part, TransferMode.SIMULATE).isEmpty()) {
                    return shulkerBox.copy();
                }
            }
            return ItemStack.EMPTY;
        }

        List<ItemStack> insertedParts = new ArrayList<>();
        for (ItemStack part : parts) {
            ItemStack remainder = insertIntoAccesses(accesses, part, TransferMode.EXECUTE);
            int inserted = part.getCount() - remainder.getCount();
            if (inserted > 0) {
                insertedParts.add(part.copyWithCount(inserted));
            }
            if (!remainder.isEmpty()) {
                rollbackInsertedParts(accesses, insertedParts);
                return shulkerBox.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private static void rollbackInsertedParts(List<StorageAccess> accesses, List<ItemStack> insertedParts) {
        for (ItemStack insertedPart : insertedParts.reversed()) {
            long remaining = insertedPart.getCount();
            while (remaining > 0) {
                ItemStack extracted = ItemStack.EMPTY;
                for (StorageAccess access : accesses) {
                    extracted = access.extract(insertedPart, remaining, TransferMode.EXECUTE);
                    if (!extracted.isEmpty()) {
                        break;
                    }
                }
                if (extracted.isEmpty()) {
                    break;
                }
                remaining -= extracted.getCount();
            }
        }
    }

    private NetworkStorageAccesses storageAccesses() {
        if (level == null || level.isClientSide()) {
            return new NetworkStorageAccesses(List.of(), List.of());
        }

        List<StorageAccess> driveBays = new ArrayList<>();
        Map<BlockPos, StorageAccess> physicalInventories = new LinkedHashMap<>();
        for (BlockPos candidate : StorageNetworkTopology.reachableNodes(level, worldPosition, SCAN_RADIUS)) {
            if (level.getBlockEntity(candidate) instanceof DriveBayBlockEntity driveBay
                    && driveBay.refreshControllerLink()
                    && driveBay.isLinkedTo(this)) {
                driveBays.add(driveBay.storageAccess());
            } else if (level.getBlockEntity(candidate) instanceof InventoryConnectorBlockEntity connector
                    && connector.refreshControllerLink()
                    && connector.isLinkedTo(this)) {
                connector.endpoints().forEach(endpoint -> physicalInventories.putIfAbsent(
                        endpoint.key(),
                        endpoint.access()
                ));
            }
        }
        for (WirelessInventoryConnectorBlockEntity connector : WirelessInventoryConnectorRegistry.linkedTo(this)) {
            connector.endpoints().forEach(endpoint -> physicalInventories.putIfAbsent(
                    endpoint.key(),
                    endpoint.access()
            ));
        }
        return new NetworkStorageAccesses(driveBays, List.copyOf(physicalInventories.values()));
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

    public record BayMoveResult(
            long movedItems,
            long remainingItems,
            int physicalInventories,
            int driveBays,
            boolean paused
    ) {
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

    private record NetworkStorageAccesses(List<StorageAccess> driveBays, List<StorageAccess> physicalInventories) {
        private NetworkStorageAccesses {
            driveBays = List.copyOf(driveBays);
            physicalInventories = List.copyOf(physicalInventories);
        }

        private List<StorageAccess> all() {
            List<StorageAccess> all = new ArrayList<>(driveBays.size() + physicalInventories.size());
            all.addAll(driveBays);
            all.addAll(physicalInventories);
            return all;
        }
    }
}
