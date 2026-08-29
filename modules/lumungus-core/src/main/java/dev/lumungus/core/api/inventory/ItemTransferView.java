package dev.lumungus.core.api.inventory;

import java.util.List;
import dev.lumungus.core.api.resource.ResourceAmount;
import net.minecraft.world.item.ItemStack;

public interface ItemTransferView {
    List<ResourceAmount> storedResources();

    long count(ItemStack template);

    default boolean contains(ItemStack template) {
        return count(template) > 0;
    }
}
