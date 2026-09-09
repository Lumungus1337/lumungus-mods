/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.BaseEntityBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jspecify.annotations.Nullable
 */
package de.djdaddy.railquarry.block;

import com.mojang.serialization.MapCodec;
import de.djdaddy.railquarry.RailQuarryMod;
import de.djdaddy.railquarry.block.entity.QuarryBlockEntity;
import de.djdaddy.railquarry.block.entity.VisualCompat;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class QuarryBlock
extends BaseEntityBlock {
    public static final BooleanProperty COAL_OK = BooleanProperty.create((String)"coal_ok");
    public static final BooleanProperty RAIL_OK = BooleanProperty.create((String)"rail_ok");

    public QuarryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{COAL_OK, RAIL_OK});
    }

    protected MapCodec<? extends BaseEntityBlock> codec() {
        throw new UnsupportedOperationException("Rail Quarry block codec is not used");
    }

    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new QuarryBlockEntity(blockPos, blockState);
    }

    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : QuarryBlock.createTickerHelper(blockEntityType, RailQuarryMod.QUARRY_BLOCK_ENTITY, QuarryBlockEntity::serverTick);
    }

    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (livingEntity == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof QuarryBlockEntity)) {
            return;
        }
        QuarryBlockEntity quarryBlockEntity = (QuarryBlockEntity)blockEntity;
        Direction direction = Direction.NORTH;
        try {
            Method method = livingEntity.getClass().getMethod("getDirection", new Class[0]);
            Object object = method.invoke((Object)livingEntity, new Object[0]);
            if (object instanceof Direction) {
                Direction direction2;
                direction = direction2 = (Direction)object;
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            // empty catch block
        }
        quarryBlockEntity.setFacing(direction);
    }

    public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            MenuProvider menuProvider;
            QuarryBlockEntity quarryBlockEntity;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof QuarryBlockEntity) {
                quarryBlockEntity = (QuarryBlockEntity)blockEntity;
                if (player.isShiftKeyDown()) {
                    boolean bl = quarryBlockEntity.toggleSilkTouch();
                    player.sendSystemMessage(RailQuarryMod.literalText("Rail Quarry Mining: " + (bl ? "SILK TOUCH" : "FORTUNE III")));
                    return QuarryBlock.successResult();
                }
            }
            if (blockEntity instanceof QuarryBlockEntity) {
                quarryBlockEntity = (QuarryBlockEntity)blockEntity;
                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)level;
                    player.sendSystemMessage(RailQuarryMod.literalText(quarryBlockEntity.getOperationalStatus(serverLevel, blockPos)));
                }
            }
            if ((menuProvider = blockState.getMenuProvider(level, blockPos)) != null) {
                player.openMenu(menuProvider);
            }
        }
        return QuarryBlock.successResult();
    }

    private static InteractionResult successResult() {
        try {
            Object object = InteractionResult.class.getField("SUCCESS").get(null);
            return (InteractionResult)object;
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException("Minecraft InteractionResult.SUCCESS is unavailable", reflectiveOperationException);
        }
    }

    public void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        QuarryBlockEntity quarryBlockEntity;
        VisualCompat.removeAt(serverLevel, blockPos);
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (!bl && blockEntity instanceof QuarryBlockEntity && !(quarryBlockEntity = (QuarryBlockEntity)blockEntity).isBeingMoved()) {
            serverLevel.updateNeighbourForOutputSignal(blockPos, (Block)this);
        }
        super.affectNeighborsAfterRemoval(blockState, serverLevel, blockPos, bl);
    }
}

