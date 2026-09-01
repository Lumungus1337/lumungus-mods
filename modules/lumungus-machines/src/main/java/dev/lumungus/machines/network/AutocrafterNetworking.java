package dev.lumungus.machines.network;

import dev.lumungus.machines.menu.AutocrafterMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class AutocrafterNetworking {
    private AutocrafterNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(
                AutocrafterActionPayload.TYPE,
                AutocrafterActionPayload.STREAM_CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(AutocrafterActionPayload.TYPE, (payload, context) -> {
            if (context.player().containerMenu instanceof AutocrafterMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.handleAction(payload);
            }
        });
    }
}
