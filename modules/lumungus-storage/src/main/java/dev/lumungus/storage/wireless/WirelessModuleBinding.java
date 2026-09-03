package dev.lumungus.storage.wireless;

import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import dev.lumungus.storage.registry.LumungusStorageItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class WirelessModuleBinding {
    private WirelessModuleBinding() {
    }

    public static boolean isPrimedModule(ItemStack stack) {
        return stack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)
                && stack.has(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER);
    }

    public static StorageControllerBlockEntity resolve(Level hostLevel, ItemStack module) {
        BoundStorageController bound = module.get(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER);
        if (bound == null || hostLevel.getServer() == null) {
            return null;
        }
        for (ServerLevel serverLevel : hostLevel.getServer().getAllLevels()) {
            if (!serverLevel.dimension().identifier().equals(bound.dimension())) {
                continue;
            }
            if (!serverLevel.isLoaded(bound.pos())) {
                serverLevel.getChunkAt(bound.pos());
            }
            if (serverLevel.getBlockEntity(bound.pos()) instanceof StorageControllerBlockEntity controller
                    && controller.getNetworkId().equals(bound.networkId())) {
                return controller;
            }
            return null;
        }
        return null;
    }
}
