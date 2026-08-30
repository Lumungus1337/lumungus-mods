package dev.lumungus.storage.data;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StorageCellDataTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void mergesMatchingItemsAndKeepsTheRemainder() {
        ItemStack cobblestone = stack(Items.COBBLESTONE, 64);

        StorageCellData.InsertResult first = StorageCellData.EMPTY.insert(cobblestone);
        StorageCellData.InsertResult second = first.data().insert(cobblestone);

        assertTrue(first.remainder().isEmpty());
        assertTrue(second.remainder().isEmpty());
        assertEquals(128, second.data().count(cobblestone));
        assertEquals(1, second.data().snapshot().storedDistinctTypes());
    }

    @Test
    void preservesComponentVariantsAsDistinctTypes() {
        ItemStack alpha = stack(Items.PAPER, 1);
        alpha.set(DataComponents.CUSTOM_NAME, Component.literal("Alpha"));
        ItemStack beta = stack(Items.PAPER, 1);
        beta.set(DataComponents.CUSTOM_NAME, Component.literal("Beta"));

        StorageCellData data = StorageCellData.EMPTY.insert(alpha).data().insert(beta).data();

        assertEquals(1, data.count(alpha));
        assertEquals(1, data.count(beta));
        assertEquals(2, data.snapshot().storedDistinctTypes());
    }

    @Test
    void enforcesTheTotalItemCapacity() {
        StorageCellData data = new StorageCellData(java.util.List.of(
                new StorageCellEntry(stack(Items.STONE, 1), StorageCellData.MAX_TOTAL_AMOUNT - 8)
        ));
        ItemStack incoming = stack(Items.STONE, 16);

        StorageCellData.InsertResult result = data.insert(incoming);

        assertEquals(StorageCellData.MAX_TOTAL_AMOUNT, result.data().snapshot().storedTotalAmount());
        assertEquals(8, result.remainder().getCount());
    }

    @Test
    void extractionNeverCreatesAnOversizedVanillaStack() {
        ItemStack stone = stack(Items.STONE, 1);
        StorageCellData data = new StorageCellData(java.util.List.of(
                new StorageCellEntry(stone, 1_000)
        ));

        StorageCellData.ExtractResult result = data.extract(stone, 1_000);

        assertEquals(stone.getMaxStackSize(), result.extracted().getCount());
        assertEquals(1_000 - stone.getMaxStackSize(), result.data().count(stone));
    }

    private static ItemStack stack(Item item, int count) {
        return new ItemStack(
                Holder.direct(item, DataComponentMap.EMPTY),
                count,
                DataComponentPatch.EMPTY
        );
    }
}
