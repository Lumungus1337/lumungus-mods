package dev.lumungus.storage.menu;

import dev.lumungus.storage.test.MinecraftTestBootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class LumungusCraftingMenuSafetyTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        MinecraftTestBootstrap.initialize();
    }

    @Test
    void preservesAResultRemainderWhenTheResultSlotWasClearedByTake() {
        SimpleContainer container = new SimpleContainer(1);
        Slot resultSlot = new Slot(container, 0, 0, 0);
        ItemStack remainder = stack(Items.OAK_PLANKS, 2);

        ItemStack unhandled = LumungusCraftingMenu.placeOrReturnResultRemainder(resultSlot, remainder);

        assertEquals(2, resultSlot.getItem().getCount());
        assertEquals(0, unhandled.getCount());
    }

    @Test
    void doesNotOverwriteANewResultCreatedDuringTake() {
        SimpleContainer container = new SimpleContainer(1);
        Slot resultSlot = new Slot(container, 0, 0, 0);
        resultSlot.setByPlayer(stack(Items.BIRCH_PLANKS, 1));

        ItemStack unhandled = LumungusCraftingMenu.placeOrReturnResultRemainder(
                resultSlot,
                stack(Items.OAK_PLANKS, 2)
        );

        assertEquals(Items.BIRCH_PLANKS, resultSlot.getItem().getItem());
        assertEquals(1, resultSlot.getItem().getCount());
        assertEquals(Items.OAK_PLANKS, unhandled.getItem());
        assertEquals(2, unhandled.getCount());
    }

    private static ItemStack stack(net.minecraft.world.item.Item item, int count) {
        return MinecraftTestBootstrap.stack(item, count);
    }
}
