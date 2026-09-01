package dev.lumungus.machines.network;

import dev.lumungus.machines.LumungusMachines;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AutocrafterActionPayload(int containerId, Action action, long amount)
        implements CustomPacketPayload {
    public static final Type<AutocrafterActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            LumungusMachines.MOD_ID,
            "autocrafter_action"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocrafterActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.containerId());
                buffer.writeByte(payload.action().ordinal());
                buffer.writeVarLong(payload.amount());
            },
            buffer -> {
                int containerId = buffer.readVarInt();
                int actionIndex = buffer.readUnsignedByte();
                if (actionIndex >= Action.values().length) {
                    throw new DecoderException("Invalid autocrafter action: " + actionIndex);
                }
                return new AutocrafterActionPayload(
                        containerId,
                        Action.values()[actionIndex],
                        buffer.readVarLong()
                );
            }
    );

    @Override
    public Type<AutocrafterActionPayload> type() {
        return TYPE;
    }

    public enum Action {
        APPLY_AMOUNT,
        TOGGLE_PAUSED,
        RESET_PROGRESS
    }
}
