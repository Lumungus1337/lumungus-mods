package dev.lumungus.storage.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;

public record BoundStorageController(Identifier dimension, BlockPos pos, UUID networkId) {
    public static final Codec<BoundStorageController> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(BoundStorageController::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(BoundStorageController::pos),
            UUIDUtil.CODEC.fieldOf("network_id").forGetter(BoundStorageController::networkId)
    ).apply(instance, BoundStorageController::new));
}
