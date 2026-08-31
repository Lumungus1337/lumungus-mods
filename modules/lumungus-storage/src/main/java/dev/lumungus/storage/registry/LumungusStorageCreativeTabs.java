package dev.lumungus.storage.registry;

import dev.lumungus.storage.LumungusStorage;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class LumungusStorageCreativeTabs {
    public static final ResourceKey<CreativeModeTab> STORAGE_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(LumungusStorage.MOD_ID, "storage")
    );

    private LumungusStorageCreativeTabs() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                STORAGE_TAB,
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.lumungus_storage.storage"))
                        .icon(() -> new ItemStack(LumungusStorageBlocks.PNEUMATIC_PIPE))
                        .displayItems((context, output) -> addStorageEntries(output))
                        .build()
        );
        CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((tab, output) -> {
            Identifier tabId = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
            if (tabId != null && ("functional_blocks".equals(tabId.getPath()) || "search".equals(tabId.getPath()))) {
                addStorageEntries(output);
            }
        });
        LumungusStorage.LOGGER.info("Registered Lumungus Storage creative tab");
    }

    private static void addStorageEntries(CreativeModeTab.Output output) {
        output.accept(LumungusStorageItems.COPPER_WRENCH);
        output.accept(LumungusStorageBlocks.STORAGE_CONTROLLER);
        output.accept(LumungusStorageBlocks.CRAFTING_TERMINAL);
        output.accept(LumungusStorageBlocks.DRIVE_BAY);
        output.accept(LumungusStorageBlocks.INVENTORY_CONNECTOR);
        output.accept(LumungusStorageBlocks.INVENTORY_TRIM);
        output.accept(LumungusStorageBlocks.PNEUMATIC_PIPE);
        output.accept(LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_SHORT);
        output.accept(LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_DIMENSION);
        output.accept(LumungusStorageBlocks.WIRELESS_STORAGE_CONTROLLER_MULTIDIMENSIONAL);
        output.accept(LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_SHORT);
        output.accept(LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_DIMENSION);
        output.accept(LumungusStorageBlocks.WIRELESS_INVENTORY_CONNECTOR_MULTIDIMENSIONAL);
        output.accept(LumungusStorageItems.PORTABLE_STORAGE_INTERFACE_SHORT);
        output.accept(LumungusStorageItems.PORTABLE_STORAGE_INTERFACE_DIMENSION);
        output.accept(LumungusStorageItems.PORTABLE_STORAGE_INTERFACE_MULTIDIMENSIONAL);
        output.accept(LumungusStorageBlocks.STORAGE_OUTPUT);
        output.accept(LumungusStorageBlocks.STORAGE_BREAKER);
        output.accept(LumungusStorageBlocks.STORAGE_PLACER);
        output.accept(LumungusStorageItems.STORAGE_CELL_16K);
    }
}
