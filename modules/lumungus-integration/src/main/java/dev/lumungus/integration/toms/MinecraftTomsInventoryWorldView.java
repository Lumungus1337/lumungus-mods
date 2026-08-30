package dev.lumungus.integration.toms;

import dev.lumungus.core.api.resource.ResourceAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class MinecraftTomsInventoryWorldView implements TomsInventoryWorldView {
    private final Level level;
    private final MinecraftTomsBlockWorldView blocks;

    public MinecraftTomsInventoryWorldView(Level level) {
        this.level = level;
        this.blocks = new MinecraftTomsBlockWorldView(level);
    }

    @Override
    public boolean isLoaded(BlockPos pos) {
        return blocks.isLoaded(pos);
    }

    @Override
    public Identifier blockId(BlockPos pos) {
        return blocks.blockId(pos);
    }

    @Override
    public Optional<TomsReadOnlyInventoryEndpoint> inventoryAt(BlockPos pos, Direction side) {
        if (!isLoaded(pos) || TomsMigrationCatalog.planFor(blockId(pos)).isPresent()) {
            return Optional.empty();
        }
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        if (storage == null) {
            return Optional.empty();
        }

        long slots = 0;
        List<ResourceAmount> resources = new ArrayList<>();
        for (StorageView<ItemVariant> view : storage) {
            if (view.getCapacity() > 0) {
                slots++;
            }
            if (!view.isResourceBlank() && view.getAmount() > 0) {
                resources.add(new ResourceAmount(view.getResource().toStack(), view.getAmount()));
            }
        }
        return Optional.of(new TomsReadOnlyInventoryEndpoint(canonicalInventoryPos(pos), slots, resources));
    }

    private BlockPos canonicalInventoryPos(BlockPos inventoryPos) {
        BlockState state = level.getBlockState(inventoryPos);
        if (!(state.getBlock() instanceof ChestBlock) || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
            return inventoryPos;
        }
        BlockPos connectedPos = ChestBlock.getConnectedBlockPos(inventoryPos, state);
        return inventoryPos.asLong() <= connectedPos.asLong() ? inventoryPos : connectedPos;
    }
}
