package dev.lumungus.storage.block.entity;

import dev.lumungus.storage.block.WirelessStorageControllerBlock;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.network.StorageControllerRegistry;
import dev.lumungus.storage.network.StorageNetworkTopology;
import dev.lumungus.storage.network.WirelessStorageControllerRegistry;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import java.util.UUID;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class WirelessStorageControllerBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos> {
    private static final String CONTROLLER_DIMENSION_KEY = "controller_dimension";
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";

    private Identifier controllerDimension;
    private BlockPos controllerPos;
    private UUID networkId;

    public WirelessStorageControllerBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.WIRELESS_STORAGE_CONTROLLER, pos, state);
    }

    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            WirelessStorageControllerBlockEntity wireless
    ) {
        if (level.getGameTime() % 40 == 0) {
            WirelessStorageControllerRegistry.register(level, pos);
            wireless.refreshControllerLink();
        }
    }

    public boolean refreshControllerLink() {
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (hasValidControllerLink()) {
            return true;
        }
        if (hasStoredControllerLink() && !isStoredControllerChunkLoaded()) {
            return false;
        }

        StorageControllerBlockEntity nearestController = nearestRegisteredController();
        if (nearestController == null) {
            nearestController = nearestLocalController();
        }
        if (nearestController == null) {
            if (!hasStoredControllerLink() || isStoredControllerChunkLoaded()) {
                clearControllerLink();
            }
            return false;
        }
        linkTo(nearestController);
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

    public StorageControllerBlockEntity linkedController() {
        if (!refreshControllerLink() || controllerPos == null) {
            return null;
        }
        StorageControllerBlockEntity controller = linkedControllerBlockEntity();
        return controller != null && isLinkedTo(controller) ? controller : null;
    }

    public WirelessStorageControllerBlock.WirelessTier tier() {
        return level == null
                ? WirelessStorageControllerBlock.WirelessTier.SHORT_RANGE
                : WirelessStorageControllerBlock.tierFor(getBlockState().getBlock());
    }

    public boolean bindTo(BoundStorageController bound) {
        if (level == null || level.isClientSide() || bound == null) {
            return false;
        }
        boolean sameDimension = level.dimension().identifier().equals(bound.dimension());
        if (!tier().canReach(sameDimension, worldPosition, bound.pos())) {
            return false;
        }
        controllerDimension = bound.dimension();
        controllerPos = bound.pos().immutable();
        networkId = bound.networkId();
        setChanged();
        return true;
    }

    private StorageControllerBlockEntity nearestRegisteredController() {
        if (level == null || level.getServer() == null) {
            return null;
        }
        StorageControllerBlockEntity nearestController = null;
        double nearestDistance = Double.MAX_VALUE;
        for (StorageControllerBlockEntity controller : StorageControllerRegistry.loadedControllers(level.getServer())) {
            boolean sameDimension = controller.getLevel().dimension().identifier().equals(level.dimension().identifier());
            if (tier().canReach(sameDimension, worldPosition, controller.getBlockPos())) {
                double distance = sameDimension ? worldPosition.distSqr(controller.getBlockPos()) : Double.MAX_VALUE - 1;
                if (distance < nearestDistance) {
                    nearestController = controller;
                    nearestDistance = distance;
                }
            }
        }
        return nearestController;
    }

    private StorageControllerBlockEntity nearestLocalController() {
        StorageControllerBlockEntity nearestController = null;
        double nearestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : StorageNetworkTopology.connectedNodes(level, worldPosition)) {
            if (level.getBlockEntity(candidate) instanceof StorageControllerBlockEntity controller
                    && tier().canReach(true, worldPosition, candidate)) {
                double distance = worldPosition.distSqr(candidate);
                if (distance < nearestDistance) {
                    nearestController = controller;
                    nearestDistance = distance;
                }
            }
        }
        return nearestController;
    }

    private boolean hasValidControllerLink() {
        StorageControllerBlockEntity controller = linkedControllerBlockEntity();
        return controllerPos != null
                && controllerDimension != null
                && networkId != null
                && controller != null
                && networkId.equals(controller.getNetworkId())
                && tier().canReach(sameDimensionAs(controller), worldPosition, controllerPos)
                && (!sameDimensionAs(controller)
                        || tier() != WirelessStorageControllerBlock.WirelessTier.SHORT_RANGE
                        || StorageNetworkTopology.canReach(level, worldPosition, controllerPos, tier().searchRadius()));
    }

    private boolean sameDimensionAs(StorageControllerBlockEntity controller) {
        return level != null && controller.getLevel().dimension().identifier().equals(level.dimension().identifier());
    }

    private boolean hasStoredControllerLink() {
        return controllerDimension != null && controllerPos != null && networkId != null;
    }

    private boolean isStoredControllerChunkLoaded() {
        if (level == null || level.getServer() == null || controllerDimension == null || controllerPos == null) {
            return false;
        }
        for (net.minecraft.server.level.ServerLevel serverLevel : level.getServer().getAllLevels()) {
            if (serverLevel.dimension().identifier().equals(controllerDimension)) {
                return serverLevel.isLoaded(controllerPos);
            }
        }
        return false;
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
                    && tier() == WirelessStorageControllerBlock.WirelessTier.MULTIDIMENSIONAL) {
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
    public Component getDisplayName() {
        return Component.translatable("container.lumungus_storage.crafting_terminal");
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return worldPosition;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (level == null) {
            return null;
        }
        return new LumungusCraftingMenu(
                containerId,
                inventory,
                ContainerLevelAccess.create(level, worldPosition),
                worldPosition
        );
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
