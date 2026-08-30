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

public record TerminalSnapshotPayload(
        int containerId,
        long storedAmount,
        long totalCapacity,
        int storedTypes,
        int totalTypeCapacity,
        List<TerminalResourceEntry> resources
) implements CustomPacketPayload {
    public static final int MAX_SYNCED_TYPES = 4_096;
    public static final Type<TerminalSnapshotPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            LumungusStorage.MOD_ID,
            "terminal_snapshot"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalSnapshotPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> payload.write(buffer),
            TerminalSnapshotPayload::read
    );

    public TerminalSnapshotPayload {
        resources = List.copyOf(resources);
    }

    @Override
    public Type<TerminalSnapshotPayload> type() {
        return TYPE;
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarLong(storedAmount);
        buffer.writeVarLong(totalCapacity);
        buffer.writeVarInt(storedTypes);
        buffer.writeVarInt(totalTypeCapacity);
        buffer.writeVarInt(resources.size());
        for (TerminalResourceEntry resource : resources) {
            ItemStack.STREAM_CODEC.encode(buffer, resource.stack());
            buffer.writeVarLong(resource.amount());
        }
    }

    private static TerminalSnapshotPayload read(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        long storedAmount = buffer.readVarLong();
        long totalCapacity = buffer.readVarLong();
        int storedTypes = buffer.readVarInt();
        int totalTypeCapacity = buffer.readVarInt();
        int resourceCount = buffer.readVarInt();
        if (resourceCount < 0 || resourceCount > MAX_SYNCED_TYPES) {
            throw new DecoderException("Invalid terminal resource count: " + resourceCount);
        }

        List<TerminalResourceEntry> resources = new ArrayList<>(resourceCount);
        for (int index = 0; index < resourceCount; index++) {
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
            long amount = buffer.readVarLong();
            resources.add(new TerminalResourceEntry(stack, amount));
        }
        return new TerminalSnapshotPayload(
                containerId,
                storedAmount,
                totalCapacity,
                storedTypes,
                totalTypeCapacity,
                resources
        );
    }
}
