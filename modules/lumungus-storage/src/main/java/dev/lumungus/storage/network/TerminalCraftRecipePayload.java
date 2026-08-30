package dev.lumungus.storage.network;

import dev.lumungus.storage.LumungusStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TerminalCraftRecipePayload(int containerId, Identifier recipeId, boolean maxTransfer)
        implements CustomPacketPayload {
    public static final Type<TerminalCraftRecipePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            LumungusStorage.MOD_ID,
            "terminal_craft_recipe"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalCraftRecipePayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.containerId());
                Identifier.STREAM_CODEC.encode(buffer, payload.recipeId());
                buffer.writeBoolean(payload.maxTransfer());
            },
            buffer -> new TerminalCraftRecipePayload(
                    buffer.readVarInt(),
                    Identifier.STREAM_CODEC.decode(buffer),
                    buffer.readBoolean()
            )
    );

    @Override
    public Type<TerminalCraftRecipePayload> type() {
        return TYPE;
    }
}
