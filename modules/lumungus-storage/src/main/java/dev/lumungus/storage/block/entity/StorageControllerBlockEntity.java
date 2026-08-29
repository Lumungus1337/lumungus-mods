package dev.lumungus.storage.block.entity;

import dev.lumungus.storage.registry.LumungusStorageBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class StorageControllerBlockEntity extends BlockEntity {
    public static final int SCAN_RADIUS = 8;

    private static final String NETWORK_ID_KEY = "network_id";

    private UUID networkId = UUID.randomUUID();

    public StorageControllerBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusStorageBlockEntities.STORAGE_CONTROLLER, pos, state);
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public String getNetworkLabel() {
        return networkId.toString().substring(0, 8).toUpperCase();
    }

    public int refreshNetwork() {
        if (level == null || level.isClientSide()) {
            return 0;
        }

        int linkedTerminals = 0;
        BlockPos min = worldPosition.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS);
        BlockPos max = worldPosition.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS);

        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockEntity(candidate) instanceof CraftingTerminalBlockEntity terminal
                    && terminal.refreshControllerLink()
                    && terminal.isLinkedTo(this)) {
                linkedTerminals++;
            }
        }

        return linkedTerminals;
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
}
