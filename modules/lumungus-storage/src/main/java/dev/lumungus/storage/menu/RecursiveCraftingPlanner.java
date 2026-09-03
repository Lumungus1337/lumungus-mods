package dev.lumungus.storage.menu;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;

final class RecursiveCraftingPlanner {
    private static final int GRID_SIZE = 9;
    private static final int MAX_DEPTH = 8;
    private static final int MAX_CRAFTS = 256;
    private static final int MAX_CANDIDATES_PER_INGREDIENT = 64;

    private final ServerLevel level;
    private final List<RecipeHolder<CraftingRecipe>> recipes;

    private RecursiveCraftingPlanner(ServerLevel level) {
        this.level = level;
        this.recipes = level.getServer().getRecipeManager().getRecipes().stream()
                .filter(holder -> holder.value() instanceof CraftingRecipe recipe && !recipe.isSpecial())
                .map(holder -> new RecipeHolder<>(holder.id(), (CraftingRecipe) holder.value()))
                .sorted(Comparator.comparing(holder -> holder.id().identifier().toString()))
                .toList();
    }

    static Optional<Plan> plan(
            ServerLevel level,
            CraftingRecipe finalRecipe,
            List<AvailableResource> available,
            int crafts
    ) {
        RecursiveCraftingPlanner planner = new RecursiveCraftingPlanner(level);
        State state = new State(available);
        List<SelectedSlot> finalSlots = new ArrayList<>();
        for (int craft = 0; craft < crafts; craft++) {
            List<SelectedSlot> craftSlots = new ArrayList<>();
            if (!planner.acquireRecipeIngredients(finalRecipe, state, craftSlots, 0, new HashSet<>())
                    || !mergeFinalSlots(finalSlots, craftSlots)) {
                return Optional.empty();
            }
        }
        if (!finalRecipe.matches(input(finalSlots), level)) {
            return Optional.empty();
        }
        return Optional.of(new Plan(List.copyOf(state.steps), List.copyOf(finalSlots)));
    }

    private static boolean mergeFinalSlots(List<SelectedSlot> combined, List<SelectedSlot> addition) {
        for (SelectedSlot added : addition) {
            SelectedSlot existing = combined.stream()
                    .filter(slot -> slot.slot() == added.slot())
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                combined.add(added);
                continue;
            }
            if (!ItemStack.isSameItemSameComponents(existing.stack(), added.stack())
                    || existing.stack().getCount() >= existing.stack().getMaxStackSize()) {
                return false;
            }
            int index = combined.indexOf(existing);
            combined.set(index, new SelectedSlot(
                    existing.slot(),
                    existing.stack().copyWithCount(existing.stack().getCount() + added.stack().getCount())
            ));
        }
        return true;
    }

    private boolean acquireRecipeIngredients(
            CraftingRecipe recipe,
            State state,
            List<SelectedSlot> selected,
            int depth,
            Set<String> recipePath
    ) {
        for (IngredientSlot slot : ingredients(recipe)) {
            ItemStack acquired = acquire(slot.ingredient(), state, depth, recipePath);
            if (acquired.isEmpty()) {
                return false;
            }
            selected.add(new SelectedSlot(slot.slot(), acquired));
        }
        return true;
    }

    private ItemStack acquire(Ingredient ingredient, State state, int depth, Set<String> recipePath) {
        ItemStack existing = state.pool.take(ingredient);
        if (!existing.isEmpty()) {
            return existing;
        }
        if (depth >= MAX_DEPTH || state.steps.size() >= MAX_CRAFTS) {
            return ItemStack.EMPTY;
        }

        int candidates = 0;
        for (RecipeHolder<CraftingRecipe> holder : recipes) {
            if (candidates >= MAX_CANDIDATES_PER_INGREDIENT) {
                break;
            }
            if (recipePath.contains(holder.id().toString())) {
                continue;
            }
            ItemStack representative = representativeResult(holder.value());
            if (representative.isEmpty() || !ingredient.test(representative)) {
                continue;
            }
            candidates++;

            State attempt = state.copy();
            Set<String> nextPath = new HashSet<>(recipePath);
            nextPath.add(holder.id().toString());
            List<SelectedSlot> inputs = new ArrayList<>();
            if (!acquireRecipeIngredients(holder.value(), attempt, inputs, depth + 1, nextPath)) {
                continue;
            }
            CraftingInput input = input(inputs);
            if (!holder.value().matches(input, level)) {
                continue;
            }
            ItemStack result = holder.value().assemble(input);
            if (result.isEmpty() || !ingredient.test(result)) {
                continue;
            }

            attempt.steps.add(new CraftStep(holder.value(), List.copyOf(inputs)));
            attempt.pool.add(result, result.getCount());
            holder.value().getRemainingItems(input).stream()
                    .filter(stack -> !stack.isEmpty())
                    .forEach(stack -> attempt.pool.add(stack, stack.getCount()));
            ItemStack produced = attempt.pool.take(ingredient);
            if (!produced.isEmpty()) {
                state.replaceWith(attempt);
                return produced;
            }
        }
        return ItemStack.EMPTY;
    }

    private ItemStack representativeResult(CraftingRecipe recipe) {
        List<SelectedSlot> selected = new ArrayList<>();
        for (IngredientSlot ingredient : ingredients(recipe)) {
            ItemStack stack = ingredient.ingredient().items()
                    .findFirst()
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            selected.add(new SelectedSlot(ingredient.slot(), stack));
        }
        CraftingInput input = input(selected);
        return recipe.matches(input, level) ? recipe.assemble(input) : ItemStack.EMPTY;
    }

    static CraftingInput input(List<SelectedSlot> slots) {
        List<ItemStack> grid = new ArrayList<>(GRID_SIZE);
        for (int index = 0; index < GRID_SIZE; index++) {
            grid.add(ItemStack.EMPTY);
        }
        for (SelectedSlot slot : slots) {
            grid.set(slot.slot(), slot.stack());
        }
        return CraftingInput.of(3, 3, grid);
    }

    private static List<IngredientSlot> ingredients(CraftingRecipe recipe) {
        List<IngredientSlot> ingredients = new ArrayList<>();
        if (recipe instanceof ShapedRecipe shaped) {
            List<Optional<Ingredient>> shapedIngredients = shaped.getIngredients();
            for (int row = 0; row < shaped.getHeight(); row++) {
                for (int column = 0; column < shaped.getWidth(); column++) {
                    int recipeIndex = row * shaped.getWidth() + column;
                    Optional<Ingredient> ingredient = shapedIngredients.get(recipeIndex);
                    if (ingredient.isPresent()) {
                        ingredients.add(new IngredientSlot(row * 3 + column, ingredient.get()));
                    }
                }
            }
            return ingredients;
        }
        List<Ingredient> shapeless = recipe.placementInfo().ingredients();
        for (int index = 0; index < shapeless.size() && index < GRID_SIZE; index++) {
            ingredients.add(new IngredientSlot(index, shapeless.get(index)));
        }
        return ingredients;
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

    record Plan(List<CraftStep> steps, List<SelectedSlot> finalSlots) {
    }

    record CraftStep(CraftingRecipe recipe, List<SelectedSlot> inputs) {
    }

    record SelectedSlot(int slot, ItemStack stack) {
        SelectedSlot {
            stack = stack.copy();
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    private record IngredientSlot(int slot, Ingredient ingredient) {
    }

    private static final class State {
        private ResourcePool pool;
        private List<CraftStep> steps;

        private State(List<AvailableResource> available) {
            pool = new ResourcePool(available);
            steps = new ArrayList<>();
        }

        private State(ResourcePool pool, List<CraftStep> steps) {
            this.pool = pool;
            this.steps = steps;
        }

        private State copy() {
            return new State(pool.copy(), new ArrayList<>(steps));
        }

        private void replaceWith(State other) {
            pool = other.pool;
            steps = other.steps;
        }
    }

    private static final class ResourcePool {
        private final List<AvailableResource> resources;

        private ResourcePool(List<AvailableResource> resources) {
            this.resources = new ArrayList<>();
            resources.forEach(resource -> add(resource.stack(), resource.amount()));
        }

        private ResourcePool copy() {
            return new ResourcePool(resources);
        }

        private void add(ItemStack stack, long amount) {
            for (int index = 0; index < resources.size(); index++) {
                AvailableResource resource = resources.get(index);
                if (ItemStack.isSameItemSameComponents(resource.stack(), stack)) {
                    resources.set(index, new AvailableResource(stack, resource.amount() + amount));
                    return;
                }
            }
            resources.add(new AvailableResource(stack, amount));
        }

        private ItemStack take(Ingredient ingredient) {
            for (int index = 0; index < resources.size(); index++) {
                AvailableResource resource = resources.get(index);
                if (resource.amount() > 0 && ingredient.test(resource.stack())) {
                    resources.set(index, new AvailableResource(resource.stack(), resource.amount() - 1));
                    return resource.stack().copyWithCount(1);
                }
            }
            return ItemStack.EMPTY;
        }
    }
}
