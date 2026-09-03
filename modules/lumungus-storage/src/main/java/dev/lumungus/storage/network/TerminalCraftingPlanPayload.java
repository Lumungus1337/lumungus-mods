package dev.lumungus.storage.network;

import dev.lumungus.storage.LumungusStorage;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record TerminalCraftingPlanPayload(
        int containerId,
        ItemStack result,
        long resultAmount,
        List<Stage> stages
) implements CustomPacketPayload {
    private static final int MAX_STAGES = 64;
    private static final int MAX_INPUTS_PER_STAGE = 9;
    public static final Type<TerminalCraftingPlanPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            LumungusStorage.MOD_ID,
            "terminal_crafting_plan"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalCraftingPlanPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.write(buffer),
            TerminalCraftingPlanPayload::read
    );

    public TerminalCraftingPlanPayload {
        result = result.copyWithCount(result.isEmpty() ? 0 : 1);
        stages = List.copyOf(stages);
    }

    public static TerminalCraftingPlanPayload empty(int containerId) {
        return new TerminalCraftingPlanPayload(containerId, ItemStack.EMPTY, 0, List.of());
    }

    @Override
    public ItemStack result() {
        return result.copy();
    }

    @Override
    public Type<TerminalCraftingPlanPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, result);
        buffer.writeVarLong(resultAmount);
        buffer.writeVarInt(stages.size());
        for (Stage stage : stages) {
            buffer.writeVarInt(stage.inputs().size());
            for (Ingredient input : stage.inputs()) {
                ItemStack.STREAM_CODEC.encode(buffer, input.stack());
                buffer.writeVarLong(input.amount());
            }
            ItemStack.STREAM_CODEC.encode(buffer, stage.output());
            buffer.writeVarLong(stage.outputAmount());
        }
    }

    private static TerminalCraftingPlanPayload read(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        ItemStack result = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        long resultAmount = buffer.readVarLong();
        int stageCount = checkedCount(buffer.readVarInt(), MAX_STAGES, "crafting stage");
        List<Stage> stages = new ArrayList<>(stageCount);
        for (int stageIndex = 0; stageIndex < stageCount; stageIndex++) {
            int inputCount = checkedCount(buffer.readVarInt(), MAX_INPUTS_PER_STAGE, "crafting input");
            List<Ingredient> inputs = new ArrayList<>(inputCount);
            for (int inputIndex = 0; inputIndex < inputCount; inputIndex++) {
                inputs.add(new Ingredient(
                        ItemStack.STREAM_CODEC.decode(buffer),
                        buffer.readVarLong()
                ));
            }
            stages.add(new Stage(
                    inputs,
                    ItemStack.STREAM_CODEC.decode(buffer),
                    buffer.readVarLong()
            ));
        }
        return new TerminalCraftingPlanPayload(containerId, result, resultAmount, stages);
    }

    private static int checkedCount(int count, int maximum, String description) {
        if (count < 0 || count > maximum) {
            throw new DecoderException("Invalid " + description + " count: " + count);
        }
        return count;
    }

    public record Ingredient(ItemStack stack, long amount) {
        public Ingredient {
            stack = stack.copyWithCount(1);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }

    public record Stage(List<Ingredient> inputs, ItemStack output, long outputAmount) {
        public Stage {
            inputs = List.copyOf(inputs);
            output = output.copyWithCount(1);
        }

        @Override
        public ItemStack output() {
            return output.copy();
        }
    }
}
