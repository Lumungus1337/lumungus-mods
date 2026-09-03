package dev.lumungus.storage.network;

import dev.lumungus.storage.menu.LumungusCraftingMenu;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TerminalNetworking {
    private TerminalNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(
                TerminalSnapshotPayload.TYPE,
                TerminalSnapshotPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
                TerminalCraftingPlanPayload.TYPE,
                TerminalCraftingPlanPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
                TerminalActionPayload.TYPE,
                TerminalActionPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
                TerminalCraftRecipePayload.TYPE,
                TerminalCraftRecipePayload.STREAM_CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(TerminalActionPayload.TYPE, (payload, context) -> {
            if (context.player().containerMenu instanceof LumungusCraftingMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.handleTerminalAction(payload);
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(TerminalCraftRecipePayload.TYPE, (payload, context) -> {
            if (context.player().containerMenu instanceof LumungusCraftingMenu menu
                    && menu.containerId == payload.containerId()) {
                menu.placeRecipeFromNetwork(payload.recipeId(), payload.requestedResultAmount());
            }
        });
    }
}
