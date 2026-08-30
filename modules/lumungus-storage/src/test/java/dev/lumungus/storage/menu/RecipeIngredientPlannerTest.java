package dev.lumungus.storage.menu;

import java.util.List;
import dev.lumungus.storage.test.MinecraftTestBootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecipeIngredientPlannerTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        MinecraftTestBootstrap.initialize();
    }

    @Test
    void backtracksWhenTheFirstMatchingAlternativeBlocksALaterIngredient() {
        Ingredient flexible = Ingredient.of(Items.OAK_PLANKS, Items.BIRCH_PLANKS);
        Ingredient oakOnly = Ingredient.of(Items.OAK_PLANKS);

        List<RecipeIngredientPlanner.IngredientSlot> ingredients = List.of(
                new RecipeIngredientPlanner.IngredientSlot(0, flexible),
                new RecipeIngredientPlanner.IngredientSlot(1, oakOnly)
        );
        List<RecipeIngredientPlanner.AvailableResource> available = List.of(
                new RecipeIngredientPlanner.AvailableResource(stack(Items.OAK_PLANKS), 1),
                new RecipeIngredientPlanner.AvailableResource(stack(Items.BIRCH_PLANKS), 1)
        );

        List<RecipeIngredientPlanner.PlannedSlot> planned = RecipeIngredientPlanner.plan(
                ingredients,
                available,
                1
        ).orElseThrow();

        assertEquals(Items.BIRCH_PLANKS, planned.stream()
                .filter(slot -> slot.slot() == 0)
                .findFirst()
                .orElseThrow()
                .stack()
                .getItem());
        assertEquals(Items.OAK_PLANKS, planned.stream()
                .filter(slot -> slot.slot() == 1)
                .findFirst()
                .orElseThrow()
                .stack()
                .getItem());
    }

    @Test
    void planningDoesNotMutateTheAvailablePool() {
        ItemStack oak = stack(Items.OAK_PLANKS);
        List<RecipeIngredientPlanner.AvailableResource> available = List.of(
                new RecipeIngredientPlanner.AvailableResource(oak, 2)
        );

        assertTrue(RecipeIngredientPlanner.plan(
                List.of(new RecipeIngredientPlanner.IngredientSlot(
                        0,
                        Ingredient.of(Items.OAK_PLANKS)
                )),
                available,
                1
        ).isPresent());
        assertEquals(2, available.getFirst().amount());
        assertEquals(1, oak.getCount());
    }

    private static ItemStack stack(Item item) {
        return MinecraftTestBootstrap.stack(item, 1);
    }
}
