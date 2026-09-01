package dev.lumungus.machines.block.entity;

import com.mojang.serialization.Codec;
import dev.lumungus.machines.production.AutocrafterRecipeExecutor;
import dev.lumungus.machines.production.AutocrafterState;
import dev.lumungus.machines.registry.LumungusMachinesBlockEntities;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.wireless.WirelessModuleBinding;
import dev.lumungus.storage.wireless.WirelessModuleHost;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class AutocrafterBlockEntity extends BlockEntity implements WirelessModuleHost {
    private static final String RECIPE_KEY = "recipe";
    private static final String TARGET_KEY = "target";
    private static final String TARGET_AMOUNT_KEY = "target_amount";
    private static final String COMPLETED_AMOUNT_KEY = "completed_amount";
    private static final String WIRELESS_MODULE_KEY = "wireless_module";

    private Identifier recipeId;
    private ItemStack target = ItemStack.EMPTY;
    private long targetAmount;
    private long completedAmount;
    private ItemStack wirelessModule = ItemStack.EMPTY;
    private AutocrafterState state = AutocrafterState.IDLE;

    public AutocrafterBlockEntity(BlockPos pos, BlockState state) {
        super(LumungusMachinesBlockEntities.AUTOCRAFTER, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AutocrafterBlockEntity autocrafter) {
        if (level.getGameTime() % 20 == 0) {
            autocrafter.runCraftCycle();
        }
    }

    public void configure(Identifier recipeId, ItemStack target, long targetAmount) {
        if (target.isEmpty() || targetAmount <= 0) {
            throw new IllegalArgumentException("Autocrafter target and amount must be valid");
        }
        this.recipeId = recipeId;
        this.target = target.copyWithCount(1);
        this.targetAmount = targetAmount;
        this.completedAmount = 0;
        this.state = AutocrafterState.IDLE;
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

    public AutocrafterState state() {
        return state;
    }

    public void runCraftCycle() {
        if (!(level instanceof ServerLevel serverLevel) || target.isEmpty() || targetAmount <= 0) {
            state = AutocrafterState.IDLE;
            return;
        }
        if (completedAmount >= targetAmount) {
            state = AutocrafterState.COMPLETE;
            return;
        }
        if (wirelessModule.isEmpty()) {
            state = AutocrafterState.NO_MODULE;
            return;
        }

        StorageControllerBlockEntity controller = WirelessModuleBinding.resolve(serverLevel, wirelessModule);
        if (controller == null) {
            state = AutocrafterState.NO_CONTROLLER;
            return;
        }

        AutocrafterRecipeExecutor.CraftResult result = AutocrafterRecipeExecutor.craftOnce(
                serverLevel,
                worldPosition,
                controller,
                recipeId,
                target,
                targetAmount - completedAmount
        );
        recipeId = result.recipeId();
        completedAmount += result.producedAmount();
        state = completedAmount >= targetAmount ? AutocrafterState.COMPLETE : result.state();
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
                targetAmount,
                Component.translatable(state.translationKey())
        );
    }

    @Override
    public ItemStack wirelessModule() {
        return wirelessModule.copy();
    }

    @Override
    public boolean installWirelessModule(ItemStack module) {
        if (!wirelessModule.isEmpty() || !WirelessModuleBinding.isPrimedModule(module)) {
            return false;
        }
        wirelessModule = module.copyWithCount(1);
        state = AutocrafterState.IDLE;
        setChanged();
        return true;
    }

    @Override
    public ItemStack removeWirelessModule() {
        ItemStack removed = wirelessModule;
        wirelessModule = ItemStack.EMPTY;
        state = AutocrafterState.NO_MODULE;
        setChanged();
        return removed;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            dropWirelessModule(level, pos);
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        recipeId = input.read(RECIPE_KEY, Identifier.CODEC).orElse(null);
        target = input.read(TARGET_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        targetAmount = input.read(TARGET_AMOUNT_KEY, Codec.LONG).orElse(0L);
        completedAmount = input.read(COMPLETED_AMOUNT_KEY, Codec.LONG).orElse(0L);
        wirelessModule = input.read(WIRELESS_MODULE_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        if (target.isEmpty() || targetAmount <= 0) {
            recipeId = null;
            target = ItemStack.EMPTY;
            targetAmount = 0;
            completedAmount = 0;
            state = AutocrafterState.IDLE;
        } else {
            completedAmount = Math.clamp(completedAmount, 0, targetAmount);
            state = completedAmount >= targetAmount ? AutocrafterState.COMPLETE : AutocrafterState.IDLE;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(RECIPE_KEY, Identifier.CODEC, recipeId);
        output.store(TARGET_KEY, ItemStack.OPTIONAL_CODEC, target);
        output.store(TARGET_AMOUNT_KEY, Codec.LONG, targetAmount);
        output.store(COMPLETED_AMOUNT_KEY, Codec.LONG, completedAmount);
        output.store(WIRELESS_MODULE_KEY, ItemStack.OPTIONAL_CODEC, wirelessModule);
    }
}
