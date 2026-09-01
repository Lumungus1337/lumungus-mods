package dev.lumungus.machines.production;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.menu.RecipeIngredientPlanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;

public final class AutocrafterRecipeExecutor {
    private static final int CRAFTING_SLOTS = 9;

    private AutocrafterRecipeExecutor() {
    }

    public static CraftResult craftOnce(
            ServerLevel level,
            BlockPos machinePos,
            StorageControllerBlockEntity controller,
            Identifier configuredRecipe,
            ItemStack target,
            long remainingAmount
    ) {
        SelectedRecipe selected = findRecipe(level, configuredRecipe, target);
        if (selected == null) {
            return new CraftResult(AutocrafterState.NO_RECIPE, configuredRecipe, 0);
        }

        RecipePlan plan = planRecipe(selected.recipe(), controller.storedResources(), level);
        if (plan == null) {
            return new CraftResult(AutocrafterState.MISSING_INGREDIENTS, selected.id(), 0);
        }

        ItemStack preview = selected.recipe().assemble(plan.input());
        if (preview.isEmpty() || !ItemStack.isSameItemSameComponents(preview, target)) {
            return new CraftResult(AutocrafterState.NO_RECIPE, selected.id(), 0);
        }
        if (preview.getCount() > remainingAmount) {
            return new CraftResult(AutocrafterState.INVALID_TARGET_AMOUNT, selected.id(), 0);
        }

        List<ExtractedIngredient> extracted = extractIngredients(controller, plan);
        if (extracted == null) {
            return new CraftResult(AutocrafterState.MISSING_INGREDIENTS, selected.id(), 0);
        }

        CraftingInput actualInput = inputFromExtracted(extracted);
        ItemStack result = selected.recipe().assemble(actualInput);
        NonNullList<ItemStack> recipeRemainders = selected.recipe().getRemainingItems(actualInput);
        List<ItemStack> outputs = new ArrayList<>();
        outputs.add(result);
        recipeRemainders.stream().filter(stack -> !stack.isEmpty()).forEach(outputs::add);

        if (!canInsertAll(controller, outputs)) {
            rollbackIngredients(controller, extracted);
            return new CraftResult(AutocrafterState.OUTPUT_BLOCKED, selected.id(), 0);
        }

        result.onCraftedBySystem(level);
        boolean outputBlocked = false;
        for (ItemStack output : outputs) {
            ItemStack remainder = controller.insert(output, TransferMode.EXECUTE);
            if (!remainder.isEmpty()) {
                outputBlocked = true;
                Block.popResource(level, machinePos, remainder);
            }
        }
        return new CraftResult(
                outputBlocked ? AutocrafterState.OUTPUT_BLOCKED : AutocrafterState.WORKING,
                selected.id(),
                result.getCount()
        );
    }

    private static SelectedRecipe findRecipe(ServerLevel level, Identifier configuredRecipe, ItemStack target) {
        if (configuredRecipe != null) {
            ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, configuredRecipe);
            Optional<RecipeHolder<?>> holder = level.getServer().getRecipeManager().byKey(key);
            if (holder.isPresent() && holder.get().value() instanceof CraftingRecipe recipe) {
                return recipeProducesTarget(level, recipe, target)
                        ? new SelectedRecipe(configuredRecipe, recipe)
                        : null;
            }
            return null;
        }

        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
            if (holder.value() instanceof CraftingRecipe recipe
                    && !recipe.isSpecial()
                    && recipeProducesTarget(level, recipe, target)) {
                return new SelectedRecipe(holder.id().identifier(), recipe);
            }
        }
        return null;
    }

    private static boolean recipeProducesTarget(ServerLevel level, CraftingRecipe recipe, ItemStack target) {
        CraftingInput representative = representativeInput(recipe);
        if (representative == null || !recipe.matches(representative, level)) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(recipe.assemble(representative), target);
    }

    private static CraftingInput representativeInput(CraftingRecipe recipe) {
        List<IngredientSlot> ingredients = recipeIngredients(recipe);
        List<ItemStack> grid = emptyGrid();
        for (IngredientSlot ingredient : ingredients) {
            ItemStack representative = ingredient.ingredient().items()
                    .findFirst()
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
            if (representative.isEmpty()) {
                return null;
            }
            grid.set(ingredient.slot(), representative);
        }
        return CraftingInput.of(3, 3, grid);
    }

    private static RecipePlan planRecipe(
            CraftingRecipe recipe,
            List<ResourceAmount> availableResources,
            ServerLevel level
    ) {
        List<RecipeIngredientPlanner.IngredientSlot> ingredients = recipeIngredients(recipe).stream()
                .map(slot -> new RecipeIngredientPlanner.IngredientSlot(slot.slot(), slot.ingredient()))
                .toList();
        List<RecipeIngredientPlanner.AvailableResource> available = availableResources.stream()
                .map(resource -> new RecipeIngredientPlanner.AvailableResource(resource.stack(), resource.amount()))
                .toList();
        Optional<List<RecipeIngredientPlanner.PlannedSlot>> planned = RecipeIngredientPlanner.plan(
                ingredients,
                available,
                1
        );
        if (planned.isEmpty()) {
            return null;
        }

        List<ItemStack> grid = emptyGrid();
        for (RecipeIngredientPlanner.PlannedSlot slot : planned.get()) {
            grid.set(slot.slot(), slot.stack().copyWithCount(1));
        }
        CraftingInput input = CraftingInput.of(3, 3, grid);
        return recipe.matches(input, level) ? new RecipePlan(planned.get(), input) : null;
    }

    private static List<ExtractedIngredient> extractIngredients(
            StorageControllerBlockEntity controller,
            RecipePlan plan
    ) {
        List<ExtractedIngredient> extracted = new ArrayList<>();
        for (RecipeIngredientPlanner.PlannedSlot slot : plan.slots()) {
            ItemStack stack = controller.extract(slot.stack(), 1, TransferMode.EXECUTE);
            if (stack.getCount() != 1) {
                rollbackIngredients(controller, extracted);
                return null;
            }
            extracted.add(new ExtractedIngredient(slot.slot(), stack));
        }
        return List.copyOf(extracted);
    }

    private static CraftingInput inputFromExtracted(List<ExtractedIngredient> extracted) {
        List<ItemStack> grid = emptyGrid();
        for (ExtractedIngredient ingredient : extracted) {
            grid.set(ingredient.slot(), ingredient.stack());
        }
        return CraftingInput.of(3, 3, grid);
    }

    private static boolean canInsertAll(StorageControllerBlockEntity controller, List<ItemStack> outputs) {
        for (ItemStack output : outputs) {
            if (!controller.insert(output, TransferMode.SIMULATE).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void rollbackIngredients(
            StorageControllerBlockEntity controller,
            List<ExtractedIngredient> extracted
    ) {
        for (ExtractedIngredient ingredient : extracted) {
            controller.insert(ingredient.stack(), TransferMode.EXECUTE);
        }
    }

    private static List<IngredientSlot> recipeIngredients(CraftingRecipe recipe) {
        List<IngredientSlot> ingredients = new ArrayList<>();
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            List<Optional<Ingredient>> shapedIngredients = shapedRecipe.getIngredients();
            for (int row = 0; row < shapedRecipe.getHeight(); row++) {
                for (int column = 0; column < shapedRecipe.getWidth(); column++) {
                    int recipeIndex = row * shapedRecipe.getWidth() + column;
                    Optional<Ingredient> ingredient = shapedIngredients.get(recipeIndex);
                    if (ingredient.isPresent()) {
                        ingredients.add(new IngredientSlot(row * 3 + column, ingredient.get()));
                    }
                }
            }
            return List.copyOf(ingredients);
        }

        List<Ingredient> shapelessIngredients = recipe.placementInfo().ingredients();
        for (int index = 0; index < shapelessIngredients.size() && index < CRAFTING_SLOTS; index++) {
            ingredients.add(new IngredientSlot(index, shapelessIngredients.get(index)));
        }
        return List.copyOf(ingredients);
    }

    private static List<ItemStack> emptyGrid() {
        List<ItemStack> grid = new ArrayList<>(CRAFTING_SLOTS);
        for (int index = 0; index < CRAFTING_SLOTS; index++) {
            grid.add(ItemStack.EMPTY);
        }
        return grid;
    }

    public record CraftResult(AutocrafterState state, Identifier recipeId, int producedAmount) {
    }

    private record SelectedRecipe(Identifier id, CraftingRecipe recipe) {
    }

    private record IngredientSlot(int slot, Ingredient ingredient) {
    }

    private record RecipePlan(List<RecipeIngredientPlanner.PlannedSlot> slots, CraftingInput input) {
    }

    private record ExtractedIngredient(int slot, ItemStack stack) {
        private ExtractedIngredient {
            stack = stack.copyWithCount(1);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }
}
