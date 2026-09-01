package dev.lumungus.machines.registry;

import dev.lumungus.machines.LumungusMachines;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class LumungusMachinesCreativeTabs {
    private static final ResourceKey<CreativeModeTab> MACHINES_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(LumungusMachines.MOD_ID, "machines")
    );

    private LumungusMachinesCreativeTabs() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                MACHINES_TAB,
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.lumungus_machines.machines"))
                        .icon(() -> new ItemStack(LumungusMachinesBlocks.AUTOCRAFTER))
                        .displayItems((context, output) -> output.accept(LumungusMachinesBlocks.AUTOCRAFTER))
                        .build()
        );
    }
}
