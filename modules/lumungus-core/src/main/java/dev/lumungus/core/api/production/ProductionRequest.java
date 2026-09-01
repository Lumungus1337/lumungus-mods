package dev.lumungus.core.api.production;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class ProductionRequest {
    private final UUID id;
    private final ItemStack target;
    private final long requestedAmount;
    private final Set<Identifier> allowedRecipes;

    public ProductionRequest(UUID id, ItemStack target, long requestedAmount, Set<Identifier> allowedRecipes) {
        this.id = Objects.requireNonNull(id, "id");
        if (target.isEmpty()) {
            throw new IllegalArgumentException("Production target must not be empty");
        }
        if (requestedAmount <= 0) {
            throw new IllegalArgumentException("Requested production amount must be positive");
        }
        this.target = target.copyWithCount(1);
        this.requestedAmount = requestedAmount;
        this.allowedRecipes = Set.copyOf(Objects.requireNonNull(allowedRecipes, "allowedRecipes"));
    }

    public static ProductionRequest create(ItemStack target, long requestedAmount) {
        return new ProductionRequest(UUID.randomUUID(), target, requestedAmount, Set.of());
    }

    public UUID id() {
        return id;
    }

    public ItemStack target() {
        return target.copy();
    }

    public long requestedAmount() {
        return requestedAmount;
    }

    public Set<Identifier> allowedRecipes() {
        return allowedRecipes;
    }

    public boolean acceptsAnyRecipe() {
        return allowedRecipes.isEmpty();
    }
}
