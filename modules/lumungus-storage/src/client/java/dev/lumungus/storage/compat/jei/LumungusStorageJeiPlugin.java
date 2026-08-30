package dev.lumungus.storage.compat.jei;

import dev.lumungus.storage.LumungusStorage;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public final class LumungusStorageJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath(
            LumungusStorage.MOD_ID,
            "jei_plugin"
    );

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new StorageNetworkRecipeTransferHandler(registration.getTransferHelper()),
                RecipeTypes.CRAFTING
        );
    }
}
