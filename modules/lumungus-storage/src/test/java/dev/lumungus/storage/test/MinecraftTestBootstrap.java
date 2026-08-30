package dev.lumungus.storage.test;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class MinecraftTestBootstrap {
    private static boolean initialized;

    private MinecraftTestBootstrap() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        initialized = true;
    }

    public static synchronized ItemStack stack(Item item, int count) {
        initialize();
        Holder.Reference<Item> holder = item.builtInRegistryHolder();
        if (!holder.areComponentsBound()) {
            holder.bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 64)
                    .build());
        }
        return new ItemStack(holder, count);
    }
}
