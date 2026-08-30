package dev.lumungus.storage.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

final class RecipeIngredientPlanner {
    private RecipeIngredientPlanner() {
    }

    static Optional<List<PlannedSlot>> plan(
            List<IngredientSlot> ingredients,
            List<AvailableResource> available,
            int crafts
    ) {
        if (crafts <= 0) {
            return Optional.empty();
        }

        List<IngredientSlot> orderedIngredients = new ArrayList<>(ingredients);
        orderedIngredients.sort(Comparator.comparingInt(
                ingredient -> matchingResourceCount(ingredient.ingredient(), available, crafts)
        ));
        List<AvailableResource> pool = new ArrayList<>(available);
        List<PlannedSlot> planned = new ArrayList<>();
        if (!assign(orderedIngredients, 0, pool, crafts, planned)) {
            return Optional.empty();
        }
        return Optional.of(List.copyOf(planned));
    }

    private static boolean assign(
            List<IngredientSlot> ingredients,
            int ingredientIndex,
            List<AvailableResource> pool,
            int crafts,
            List<PlannedSlot> planned
    ) {
        if (ingredientIndex == ingredients.size()) {
            return true;
        }

        IngredientSlot ingredient = ingredients.get(ingredientIndex);
        for (int resourceIndex = 0; resourceIndex < pool.size(); resourceIndex++) {
            AvailableResource resource = pool.get(resourceIndex);
            if (resource.amount() < crafts
                    || resource.stack().getMaxStackSize() < crafts
                    || !ingredient.ingredient().test(resource.stack())) {
                continue;
            }

            pool.set(resourceIndex, new AvailableResource(
                    resource.stack(),
                    resource.amount() - crafts
            ));
            planned.add(new PlannedSlot(
                    ingredient.slot(),
                    resource.stack().copyWithCount(crafts)
            ));
            if (assign(ingredients, ingredientIndex + 1, pool, crafts, planned)) {
                return true;
            }
            planned.remove(planned.size() - 1);
            pool.set(resourceIndex, resource);
        }
        return false;
    }

    private static int matchingResourceCount(
            Ingredient ingredient,
            List<AvailableResource> available,
            int crafts
    ) {
        int matches = 0;
        for (AvailableResource resource : available) {
            if (resource.amount() >= crafts
                    && resource.stack().getMaxStackSize() >= crafts
                    && ingredient.test(resource.stack())) {
                matches++;
            }
        }
        return matches;
    }

    record IngredientSlot(int slot, Ingredient ingredient) {
    }

    record AvailableResource(ItemStack stack, long amount) {
        AvailableResource {
            stack = stack.copyWithCount(1);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    record PlannedSlot(int slot, ItemStack stack) {
        PlannedSlot {
            stack = stack.copy();
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }
}

