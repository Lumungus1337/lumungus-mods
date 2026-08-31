package dev.lumungus.storage.network;

import dev.lumungus.storage.LumungusStorage;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record TerminalActionPayload(int containerId, Action action, ItemStack template)
        implements CustomPacketPayload {
    public static final Type<TerminalActionPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            LumungusStorage.MOD_ID,
            "terminal_action"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalActionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.write(buffer),
            TerminalActionPayload::read
    );

    public TerminalActionPayload {
        template = template.copy();
    }

    @Override
    public ItemStack template() {
        return template.copy();
    }

    @Override
    public Type<TerminalActionPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeByte(action.ordinal());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, template);
    }

    private static TerminalActionPayload read(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        int actionIndex = buffer.readUnsignedByte();
        if (actionIndex >= Action.values().length) {
            throw new DecoderException("Invalid terminal action: " + actionIndex);
        }
        return new TerminalActionPayload(
                containerId,
                Action.values()[actionIndex],
                ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer)
        );
    }

    public enum Action {
        EXTRACT_STACK_TO_CURSOR,
        EXTRACT_ONE_TO_CURSOR,
        EXTRACT_STACK_TO_INVENTORY,
        DEPOSIT_CARRIED_STACK,
        DEPOSIT_ONE_CARRIED,
        MOVE_PHYSICAL_TO_DRIVE_BAYS
    }
}
