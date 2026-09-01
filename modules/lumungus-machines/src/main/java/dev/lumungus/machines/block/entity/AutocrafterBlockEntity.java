package dev.lumungus.machines.block.entity;

import com.mojang.serialization.Codec;
import dev.lumungus.machines.registry.LumungusMachinesBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class AutocrafterBlockEntity extends BlockEntity {
    private static final String RECIPE_KEY = "recipe";
    private static final String TARGET_KEY = "target";
    private static final String TARGET_AMOUNT_KEY = "target_amount";
    private static final String COMPLETED_AMOUNT_KEY = "completed_amount";

    private Identifier recipeId;
    private ItemStack target = ItemStack.EMPTY;
    private long targetAmount;
    private long completedAmount;

    public AutocrafterBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusMachinesBlockEntities.AUTOCRAFTER, pos, state);
    }

    public void configure(Identifier recipeId, ItemStack target, long targetAmount) {
        if (target.isEmpty() || targetAmount <= 0) {
            throw new IllegalArgumentException("Autocrafter target and amount must be valid");
        }
        this.recipeId = recipeId;
        this.target = target.copyWithCount(1);
        this.targetAmount = targetAmount;
        this.completedAmount = 0;
        setChanged();
    }

    public void setTarget(ItemStack target, long targetAmount) {
        configure(null, target, targetAmount);
    }

    public Identifier recipeId() {
        return recipeId;
    }

    public ItemStack target() {
        return target.copy();
    }

    public long targetAmount() {
        return targetAmount;
    }

    public long completedAmount() {
        return completedAmount;
    }

    public void setCompletedAmount(long completedAmount) {
        if (completedAmount < 0 || completedAmount > targetAmount) {
            throw new IllegalArgumentException("Autocrafter progress is outside its target amount");
        }
        this.completedAmount = completedAmount;
        setChanged();
    }

    public Component statusText() {
        if (target.isEmpty()) {
            return Component.translatable("message.lumungus_machines.autocrafter.unconfigured");
        }
        return Component.translatable(
                "message.lumungus_machines.autocrafter.status",
                target.getHoverName(),
                completedAmount,
                targetAmount
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        recipeId = input.read(RECIPE_KEY, Identifier.CODEC).orElse(null);
        target = input.read(TARGET_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        targetAmount = input.read(TARGET_AMOUNT_KEY, Codec.LONG).orElse(0L);
        completedAmount = input.read(COMPLETED_AMOUNT_KEY, Codec.LONG).orElse(0L);
        if (target.isEmpty() || targetAmount <= 0) {
            recipeId = null;
            target = ItemStack.EMPTY;
            targetAmount = 0;
            completedAmount = 0;
        } else {
            completedAmount = Math.clamp(completedAmount, 0, targetAmount);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(RECIPE_KEY, Identifier.CODEC, recipeId);
        output.store(TARGET_KEY, ItemStack.OPTIONAL_CODEC, target);
        output.store(TARGET_AMOUNT_KEY, Codec.LONG, targetAmount);
        output.store(COMPLETED_AMOUNT_KEY, Codec.LONG, completedAmount);
    }
}
