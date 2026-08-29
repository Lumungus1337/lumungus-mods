package dev.lumungus.storage.block.entity;

import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CraftingTerminalBlockEntity extends BlockEntity implements MenuProvider {
    private static final String CONTROLLER_POS_KEY = "controller_pos";
    private static final String NETWORK_ID_KEY = "network_id";

    private BlockPos controllerPos;
    private UUID networkId;

    public CraftingTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.CRAFTING_TERMINAL, pos, state);
    }

    public boolean refreshControllerLink() {
        if (level == null || level.isClientSide()) {
            return false;
        }

        if (hasValidControllerLink()) {
            return true;
        }

        StorageControllerBlockEntity nearestController = null;
        double nearestDistance = Double.MAX_VALUE;
        int radius = StorageControllerBlockEntity.SCAN_RADIUS;
        BlockPos min = worldPosition.offset(-radius, -radius, -radius);
        BlockPos max = worldPosition.offset(radius, radius, radius);

        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(candidate) instanceof StorageControllerBlockEntity controller) {
                double distance = worldPosition.distSqr(candidate);
                if (distance < nearestDistance) {
                    nearestController = controller;
                    nearestDistance = distance;
                }
            }
        }

        if (nearestController == null) {
            clearControllerLink();
            return false;
        }

        linkTo(nearestController);
        return true;
    }

    public boolean isLinkedTo(StorageControllerBlockEntity controller) {
        return controllerPos != null
                && networkId != null
                && controllerPos.equals(controller.getBlockPos())
                && networkId.equals(controller.getNetworkId());
    }

    private boolean hasValidControllerLink() {
        return controllerPos != null
                && networkId != null
                && level.getBlockEntity(controllerPos) instanceof StorageControllerBlockEntity controller
                && networkId.equals(controller.getNetworkId());
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
    public Component getDisplayName() {
        return Component.translatable("container.lumungus_storage.crafting_terminal");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (level == null) {
            return null;
        }

        return new LumungusCraftingMenu(
                containerId,
                inventory,
                ContainerLevelAccess.create(level, worldPosition)
        );
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
