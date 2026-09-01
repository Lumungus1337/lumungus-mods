package dev.lumungus.storage.item;

import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class WirelessNetworkModuleItem extends Item {
    public WirelessNetworkModuleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> textConsumer,
            TooltipFlag flag
    ) {
        BoundStorageController bound = stack.get(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER);
        textConsumer.accept(Component.translatable("tooltip.lumungus_storage.wireless_network_module"));
        textConsumer.accept(bound == null
                ? Component.translatable("tooltip.lumungus_storage.wireless_network_module.unprimed")
                : Component.translatable(
                        "tooltip.lumungus_storage.wireless_network_module.primed",
                        bound.dimension().toString(),
                        bound.pos().getX(),
                        bound.pos().getY(),
                        bound.pos().getZ()
                ));
    }
}
