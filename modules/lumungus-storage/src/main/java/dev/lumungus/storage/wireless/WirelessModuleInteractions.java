package dev.lumungus.storage.wireless;

import dev.lumungus.storage.registry.LumungusStorageItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class WirelessModuleInteractions {
    private WirelessModuleInteractions() {
    }

    public static boolean tryInstall(WirelessModuleHost host, ItemStack heldStack, Player player) {
        if (!heldStack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE)) {
            return false;
        }
        if (!WirelessModuleBinding.isPrimedModule(heldStack)) {
            player.sendSystemMessage(Component.translatable("message.lumungus_storage.wireless_module.not_primed"));
            return true;
        }
        if (!host.installWirelessModule(heldStack)) {
            player.sendSystemMessage(Component.translatable("message.lumungus_storage.wireless_module.slot_occupied"));
            return true;
        }
        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }
        player.sendSystemMessage(Component.translatable("message.lumungus_storage.wireless_module.installed"));
        return true;
    }

    public static boolean tryRemove(WirelessModuleHost host, Player player) {
        ItemStack removed = host.removeWirelessModule();
        if (removed.isEmpty()) {
            return false;
        }
        if (!player.getInventory().add(removed)) {
            player.drop(removed, false);
        }
        player.sendSystemMessage(Component.translatable("message.lumungus_storage.wireless_module.removed"));
        return true;
    }
}
