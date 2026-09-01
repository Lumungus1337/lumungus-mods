package dev.lumungus.core.api.production;

import dev.lumungus.core.api.resource.ResourceAmount;
import dev.lumungus.core.test.MinecraftTestBootstrap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ProductionRequestTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        MinecraftTestBootstrap.initialize();
    }

    @Test
    void preservesLargeTargetAmountsWithoutMutatingTheInputStack() {
        ItemStack barrels = MinecraftTestBootstrap.stack(Items.BARREL, 16);
        ProductionRequest request = ProductionRequest.create(barrels, 120);

        barrels.setCount(1);
        ItemStack returnedTarget = request.target();
        returnedTarget.setCount(32);

        assertEquals(120, request.requestedAmount());
        assertEquals(1, request.target().getCount());
    }

    @Test
    void restrictsAJobToExplicitRecipeChoices() {
        Identifier recipe = Identifier.fromNamespaceAndPath("minecraft", "barrel");
        ProductionRequest request = new ProductionRequest(
                UUID.randomUUID(),
                MinecraftTestBootstrap.stack(Items.BARREL, 1),
                120,
                Set.of(recipe)
        );

        assertFalse(request.acceptsAnyRecipe());
        assertEquals(Set.of(recipe), request.allowedRecipes());
    }

    @Test
    void progressReportsRemainingAndMissingResources() {
        ProductionRequest request = ProductionRequest.create(
                MinecraftTestBootstrap.stack(Items.BARREL, 1),
                120
        );
        ResourceAmount missingLogs = new ResourceAmount(MinecraftTestBootstrap.stack(Items.OAK_LOG, 1), 30);
        ProductionProgress progress = new ProductionProgress(
                request,
                48,
                ProductionStatus.WAITING_FOR_MATERIAL,
                List.of(missingLogs)
        );

        assertEquals(72, progress.remainingAmount());
        assertEquals(List.of(missingLogs), progress.missingResources());
        assertTrue(ProductionStatus.COMPLETE.isTerminal());
        assertTrue(ProductionStatus.BLOCKED.isTerminal());
        assertFalse(ProductionStatus.IN_PROGRESS.isTerminal());
    }

    @Test
    void rejectsImpossibleProgressStates() {
        ProductionRequest request = ProductionRequest.create(
                MinecraftTestBootstrap.stack(Items.BARREL, 1),
                120
        );

        assertThrows(IllegalArgumentException.class, () -> new ProductionProgress(
                request,
                119,
                ProductionStatus.COMPLETE,
                List.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> new ProductionProgress(
                request,
                121,
                ProductionStatus.IN_PROGRESS,
                List.of()
        ));
    }
}
