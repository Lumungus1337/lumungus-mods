package dev.lumungus.machines.menu;

import dev.lumungus.machines.block.entity.AutocrafterBlockEntity;
import dev.lumungus.machines.network.AutocrafterActionPayload;
import dev.lumungus.machines.production.AutocrafterRecipeExecutor;
import dev.lumungus.machines.production.AutocrafterState;
import dev.lumungus.machines.registry.LumungusMachinesBlocks;
import dev.lumungus.machines.registry.LumungusMachinesMenus;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class AutocrafterMenu extends AbstractContainerMenu {
    public static final int IMAGE_WIDTH = 230;
    public static final int IMAGE_HEIGHT = 203;
    public static final int RECIPE_SLOT_COUNT = 9;
    public static final int RESULT_SLOT = 9;

    private static final int DATA_STATE = 0;
    private static final int DATA_PAUSED = 1;
    private static final int DATA_TARGET_LOW = 2;
    private static final int DATA_TARGET_HIGH = 3;
    private static final int DATA_COMPLETED_LOW = 4;
    private static final int DATA_COMPLETED_HIGH = 5;
    private static final int DATA_COUNT = 6;

    private final ContainerLevelAccess access;
    private final AutocrafterBlockEntity autocrafter;
    private final Container preview = new SimpleContainer(RECIPE_SLOT_COUNT + 1);
    private final ContainerData data = new SimpleContainerData(DATA_COUNT);
    private int previewFingerprint = Integer.MIN_VALUE;

    public AutocrafterMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(
                containerId,
                inventory,
                inventory.player.level().getBlockEntity(pos) instanceof AutocrafterBlockEntity blockEntity
                        ? blockEntity
                        : null,
                pos
        );
    }

    public AutocrafterMenu(
            int containerId,
            Inventory inventory,
            AutocrafterBlockEntity autocrafter,
            BlockPos pos
    ) {
        super(LumungusMachinesMenus.AUTOCRAFTER, containerId);
        this.autocrafter = autocrafter;
        this.access = ContainerLevelAccess.create(inventory.player.level(), pos);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = row * 3 + column;
                addSlot(new PreviewSlot(preview, slot, 24 + column * 18, 35 + row * 18));
            }
        }
        addSlot(new PreviewSlot(preview, RESULT_SLOT, 112, 53));
        addStandardInventorySlots(inventory, 34, 121);
        addDataSlots(data);
        syncFromBlockEntity();
        refreshPreview();
    }

    public long targetAmount() {
        return joinLong(data.get(DATA_TARGET_LOW), data.get(DATA_TARGET_HIGH));
    }

    public long completedAmount() {
        return joinLong(data.get(DATA_COMPLETED_LOW), data.get(DATA_COMPLETED_HIGH));
    }

    public boolean paused() {
        return data.get(DATA_PAUSED) != 0;
    }

    public AutocrafterState state() {
        int ordinal = data.get(DATA_STATE);
        return ordinal >= 0 && ordinal < AutocrafterState.values().length
                ? AutocrafterState.values()[ordinal]
                : AutocrafterState.IDLE;
    }

    public ItemStack target() {
        return preview.getItem(RESULT_SLOT).copy();
    }

    public void handleAction(AutocrafterActionPayload payload) {
        if (autocrafter == null) {
            return;
        }
        switch (payload.action()) {
            case APPLY_AMOUNT -> autocrafter.setTargetAmount(payload.amount());
            case TOGGLE_PAUSED -> autocrafter.setPaused(!autocrafter.paused());
            case RESET_PROGRESS -> autocrafter.resetProgress();
        }
        syncFromBlockEntity();
        broadcastChanges();
    }

    @Override
    public void broadcastChanges() {
        syncFromBlockEntity();
        refreshPreview();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, LumungusMachinesBlocks.AUTOCRAFTER);
    }

    private void syncFromBlockEntity() {
        if (autocrafter == null) {
            return;
        }
        data.set(DATA_STATE, autocrafter.state().ordinal());
        data.set(DATA_PAUSED, autocrafter.paused() ? 1 : 0);
        splitLong(autocrafter.targetAmount(), DATA_TARGET_LOW, DATA_TARGET_HIGH);
        splitLong(autocrafter.completedAmount(), DATA_COMPLETED_LOW, DATA_COMPLETED_HIGH);
    }

    private void refreshPreview() {
        if (autocrafter == null || !(autocrafter.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        int fingerprint = 31 * autocrafter.target().hashCode()
                + java.util.Objects.hashCode(autocrafter.recipeId());
        if (fingerprint == previewFingerprint) {
            return;
        }
        previewFingerprint = fingerprint;
        AutocrafterRecipeExecutor.RecipePreview recipe = AutocrafterRecipeExecutor.preview(
                serverLevel,
                autocrafter.recipeId(),
                autocrafter.target()
        );
        List<ItemStack> ingredients = recipe.ingredients();
        for (int slot = 0; slot < RECIPE_SLOT_COUNT; slot++) {
            preview.setItem(slot, ingredients.get(slot));
        }
        preview.setItem(RESULT_SLOT, recipe.result().isEmpty() ? autocrafter.target() : recipe.result());
    }

    private void splitLong(long value, int lowIndex, int highIndex) {
        data.set(lowIndex, (int) value);
        data.set(highIndex, (int) (value >>> 32));
    }

    private static long joinLong(int low, int high) {
        return Integer.toUnsignedLong(low) | ((long) high << 32);
    }

    private static final class PreviewSlot extends Slot {
        private PreviewSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
