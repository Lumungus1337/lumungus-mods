package dev.lumungus.storage.compat.jei;

import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.network.TerminalCraftRecipePayload;
import dev.lumungus.storage.registry.LumungusStorageMenus;
import java.util.Optional;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public final class StorageNetworkRecipeTransferHandler implements IRecipeTransferHandler<
        LumungusCraftingMenu,
        RecipeHolder<CraftingRecipe>
> {
    private final IRecipeTransferHandlerHelper transferHelper;

    public StorageNetworkRecipeTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends LumungusCraftingMenu> getContainerClass() {
        return LumungusCraftingMenu.class;
    }

    @Override
    public Optional<MenuType<LumungusCraftingMenu>> getMenuType() {
        return Optional.of(LumungusStorageMenus.CRAFTING_TERMINAL);
    }

    @Override
    public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(
            LumungusCraftingMenu menu,
            RecipeHolder<CraftingRecipe> recipe,
            IRecipeSlotsView recipeSlots,
            Player player,
            boolean maxTransfer,
            boolean doTransfer
    ) {
        if (!ClientPlayNetworking.canSend(TerminalCraftRecipePayload.TYPE)) {
            return transferHelper.createUserErrorWithTooltip(Component.translatable(
                    "message.lumungus_storage.crafting_terminal.recipe_transfer_unavailable"
            ));
        }
        if (doTransfer) {
            ClientPlayNetworking.send(new TerminalCraftRecipePayload(
                    menu.containerId,
                    recipe.id().identifier(),
                    maxTransfer
            ));
        }
        return null;
    }
}
