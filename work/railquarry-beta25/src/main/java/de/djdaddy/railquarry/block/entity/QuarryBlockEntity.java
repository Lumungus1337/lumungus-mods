/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.Container
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.ItemContainerContents
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.FluidState
 *  net.minecraft.world.level.storage.ValueInput
 *  net.minecraft.world.level.storage.ValueOutput
 */
package de.djdaddy.railquarry.block.entity;

import de.djdaddy.railquarry.RailQuarryMod;
import de.djdaddy.railquarry.block.QuarryBlock;
import de.djdaddy.railquarry.block.entity.ChunkLoaderCompat;
import de.djdaddy.railquarry.block.entity.FluidDrainCompat;
import de.djdaddy.railquarry.block.entity.PersistenceCompat;
import de.djdaddy.railquarry.block.entity.QuarrySettingsContainer;
import de.djdaddy.railquarry.block.entity.VisualCompat;
import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.registry.LumungusStorageItems;
import dev.lumungus.storage.wireless.WirelessModuleBinding;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class QuarryBlockEntity
extends RandomizableContainerBlockEntity implements net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPos> {
    public static final int DEFAULT_WIDTH = 16;
    public static final int MIN_TRACK_WIDTH = 2;
    public static final int MAX_TRACK_WIDTH = 65;
    public static final int HEIGHT = 8;
    public static final int MAX_HEIGHT = 64;
    public static final int SCAN_BUDGET = 256;
    private int miningHeight = HEIGHT;
    private int miningCursor;
    private int shieldCursor;
    private boolean shieldChecked;
    public static final int TICKS_PER_BLOCK = 2;
    public static final int FUEL_TICKS_PER_COAL = 3600;
    public static final int FUEL_SLOT = 0;
    public static final int BUCKET_INPUT_START = 1;
    public static final int BUCKET_INPUT_END = 8;
    public static final int SHULKER_INPUT_START = 9;
    public static final int SHULKER_INPUT_END = 26;
    public static final int OUTPUT_START = 27;
    public static final int OUTPUT_END = 50;
    public static final int WIRELESS_MODULE_SLOT = 51;
    private static final int INTERNAL_INVENTORY_SIZE = 54;
    private static final int SHULKER_SIZE = 27;
    private NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
    private int workTicks;
    private int fuelTicks;
    private boolean silkTouch;
    private boolean beingMoved;
    private boolean hasMoved;
    private boolean directionVerified;
    private int railOffset;
    private int workingShulkerSlot = -1;
    private Direction facing = Direction.NORTH;
    private boolean ledCoalOk;
    private boolean ledRailOk;
    private boolean ledsInitialized;
    private boolean collectFluids = true;

    public QuarryBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RailQuarryMod.QUARRY_BLOCK_ENTITY, blockPos, blockState);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, QuarryBlockEntity quarryBlockEntity) {
        VisualCompat.tick(level, blockPos, (Object)quarryBlockEntity);
        ChunkLoaderCompat.tick(level, blockPos, (Object)quarryBlockEntity);
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        quarryBlockEntity.updateStatusLeds(serverLevel, blockPos);
        if (!QuarryBlockEntity.isRail(serverLevel, blockPos.below())) {
            quarryBlockEntity.workTicks = 0;
            return;
        }
        if (!quarryBlockEntity.ensureFuelLoaded()) {
            quarryBlockEntity.workTicks = 0;
            return;
        }
        ++quarryBlockEntity.workTicks;
        if (quarryBlockEntity.workTicks < 2) {
            return;
        }
        quarryBlockEntity.workTicks = 0;
        TrackLayout trackLayout = quarryBlockEntity.resolveTrackLayout(serverLevel, blockPos);
        if (trackLayout == null) {
            return;
        }
        if (!quarryBlockEntity.shieldChecked && quarryBlockEntity.drainFluidShield(serverLevel, blockPos, trackLayout.facing(), trackLayout.railOffset())) {
            quarryBlockEntity.consumeFuelForWorkCycle();
            return;
        }
        if (!quarryBlockEntity.shieldChecked) {
            return;
        }
        MiningResult miningResult = quarryBlockEntity.mineOneBlock(serverLevel, blockPos, trackLayout.facing(), trackLayout.railOffset());
        if (miningResult == MiningResult.SCANNING) {
            return;
        }
        quarryBlockEntity.shieldChecked = false;
        if (miningResult == MiningResult.MINED) {
            quarryBlockEntity.consumeFuelForWorkCycle();
            return;
        }
        if (miningResult == MiningResult.STORAGE_FULL || miningResult == MiningResult.NO_SHULKER || miningResult == MiningResult.NO_BUCKET) {
            return;
        }
        if (quarryBlockEntity.tryAdvance(serverLevel, blockPos, blockState, trackLayout)) {
            quarryBlockEntity.consumeFuelForWorkCycle();
        }
    }

    private TrackLayout resolveTrackLayout(ServerLevel serverLevel, BlockPos blockPos) {
        int n;
        if (this.directionVerified && this.railOffset != 0 && !QuarryBlockEntity.hasForwardDualTrack(serverLevel, blockPos, this.facing, this.railOffset)) {
            return null;
        }
        if (this.directionVerified && this.railOffset != 0 && QuarryBlockEntity.hasForwardDualTrack(serverLevel, blockPos, this.facing, this.railOffset)) {
            return new TrackLayout(this.facing, this.railOffset);
        }
        if (this.railOffset != 0 && QuarryBlockEntity.hasForwardDualTrack(serverLevel, blockPos, this.facing, this.railOffset)) {
            this.directionVerified = true;
            this.setChanged();
            return new TrackLayout(this.facing, this.railOffset);
        }
        Direction opposite = this.facing.getOpposite();
        if (this.railOffset != 0 && QuarryBlockEntity.hasForwardDualTrack(serverLevel, blockPos, opposite, n = -this.railOffset)) {
            this.facing = opposite;
            this.railOffset = n;
            this.directionVerified = true;
            this.setChanged();
            return new TrackLayout(opposite, n);
        }
        Direction[] directionArray = new Direction[]{this.facing, this.facing.getOpposite(), this.facing.getClockWise(), this.facing.getClockWise().getOpposite()};
        for (Direction direction : directionArray) {
            int n2 = QuarryBlockEntity.detectCompanionRail(serverLevel, blockPos, direction);
            if (n2 == 0 || !QuarryBlockEntity.hasForwardDualTrack(serverLevel, blockPos, direction, n2)) continue;
            this.facing = direction;
            this.railOffset = n2;
            this.directionVerified = true;
            this.setChanged();
            return new TrackLayout(direction, n2);
        }
        return null;
    }

    private static int detectCompanionRail(ServerLevel serverLevel, BlockPos blockPos, Direction direction) {
        if (!QuarryBlockEntity.isRail(serverLevel, blockPos.below())) {
            return 0;
        }
        Direction direction2 = direction.getClockWise();
        for (int i = 1; i <= 64; ++i) {
            BlockPos blockPos2 = blockPos.relative(direction2, i).below();
            if (QuarryBlockEntity.isRail(serverLevel, blockPos2) && QuarryBlockEntity.isRail(serverLevel, blockPos2.relative(direction))) {
                return i;
            }
            BlockPos blockPos3 = blockPos.relative(direction2, -i).below();
            if (!QuarryBlockEntity.isRail(serverLevel, blockPos3) || !QuarryBlockEntity.isRail(serverLevel, blockPos3.relative(direction))) continue;
            return -i;
        }
        return 0;
    }

    private static boolean hasDualTrack(ServerLevel serverLevel, BlockPos blockPos, Direction direction, int n) {
        Direction direction2 = direction.getClockWise();
        BlockPos blockPos2 = blockPos.relative(direction2, n);
        return QuarryBlockEntity.isRail(serverLevel, blockPos.below()) && QuarryBlockEntity.isRail(serverLevel, blockPos2.below());
    }

    private static boolean hasForwardDualTrack(ServerLevel serverLevel, BlockPos blockPos, Direction direction, int n) {
        Direction direction2 = direction.getClockWise();
        BlockPos blockPos2 = blockPos.relative(direction2, n);
        BlockPos blockPos3 = blockPos.relative(direction);
        BlockPos blockPos4 = blockPos2.relative(direction);
        return QuarryBlockEntity.isRail(serverLevel, blockPos.below()) && QuarryBlockEntity.isRail(serverLevel, blockPos2.below()) && QuarryBlockEntity.isRail(serverLevel, blockPos3.below()) && QuarryBlockEntity.isRail(serverLevel, blockPos4.below());
    }

    private static boolean isRail(ServerLevel serverLevel, BlockPos blockPos) {
        return serverLevel.getBlockState(blockPos).is(BlockTags.RAILS);
    }

    private MiningResult mineOneBlock(ServerLevel serverLevel, BlockPos blockPos, Direction direction, int n) {
        Direction direction2 = direction.getClockWise();
        BlockPos blockPos2 = blockPos.relative(direction);
        int n2 = Math.min(0, n);
        int n3 = Math.max(0, n);
        int height = effectiveHeight(serverLevel, blockPos);
        int width = n3 - n2 + 1;
        int checked = 0;
        while (miningCursor < height * width && checked++ < SCAN_BUDGET) {
                int index = miningCursor++;
                int i = height - 1 - index / width;
                int j = n2 + index % width;
                List<ItemStack> list;
                BlockPos blockPos3 = blockPos2.relative(direction2, j).above(i);
                BlockState blockState = serverLevel.getBlockState(blockPos3);
                if (blockState.isAir() || blockState.is(BlockTags.RAILS) || QuarryBlockEntity.isBlock(blockState, RailQuarryMod.QUARRY) || QuarryBlockEntity.isBlock(blockState, Blocks.BEDROCK) || serverLevel.getBlockEntity(blockPos3) != null) continue;
                boolean bl = QuarryBlockEntity.isBlock(blockState, Blocks.WATER);
                boolean bl2 = QuarryBlockEntity.isBlock(blockState, Blocks.LAVA);
                if (bl || bl2) {
                    FluidState fluidState = blockState.getFluidState();
                    if (!fluidState.isSource()) continue;
                    ItemStack itemStack = (bl ? Items.WATER_BUCKET : Items.LAVA_BUCKET).getDefaultInstance();
                    miningCursor = 0;
                    return this.mineFluidSource(serverLevel, blockPos3, itemStack);
                }
                miningCursor = 0;
                ItemStack toolStack = Items.NETHERITE_PICKAXE.getDefaultInstance();
                this.applyFortuneIfNeeded(serverLevel, toolStack);
                boolean bl3 = !this.silkTouch || this.addSilkTouch(serverLevel, toolStack);
                List<ItemStack> list2 = list = this.silkTouch && !bl3 ? QuarryBlockEntity.getSilkFallbackDrops(blockState) : QuarryBlockEntity.getDropsCompat(blockState, serverLevel, blockPos3, toolStack);
                if (list == null) {
                    return MiningResult.STORAGE_FULL;
                }
                StorageControllerBlockEntity storageController = this.linkedStorageController(serverLevel);
                boolean sendToStorage = storageController != null && canInsertAllIntoStorage(storageController, list);
                if (!sendToStorage) {
                    SpaceResult spaceResult = this.prepareSpaceForDrops(list);
                    if (spaceResult == SpaceResult.NO_SHULKER) {
                        return MiningResult.NO_SHULKER;
                    }
                    if (spaceResult == SpaceResult.NO_OUTPUT_SPACE) {
                        return MiningResult.STORAGE_FULL;
                    }
                }
                serverLevel.destroyBlock(blockPos3, false);
                if (sendToStorage) {
                    this.storeNetworkRemainders(serverLevel, blockPos3, insertIntoStorage(storageController, list));
                } else {
                    this.insertDropsIntoActiveShulker(list);
                    this.finishBoxIfFull();
                }
                this.setChanged();
                return MiningResult.MINED;
        }
        if (miningCursor < height * width) {
            return MiningResult.SCANNING;
        }
        miningCursor = 0;
        return MiningResult.SLICE_CLEAR;
    }

    private boolean drainFluidShield(ServerLevel serverLevel, BlockPos blockPos, Direction direction, int n) {
        Direction direction2 = direction.getClockWise();
        BlockPos blockPos2 = blockPos.relative(direction);
        int n2 = Math.min(0, n) - 1;
        int n3 = Math.max(0, n) + 1;
        int width = n3 - n2 + 1;
        int total = 27 + (effectiveHeight(serverLevel, blockPos) + 2) * width;
        int checked = 0;
        while (shieldCursor < total && checked++ < SCAN_BUDGET) {
                int index = shieldCursor++;
                BlockPos target;
                if (index < 27) {
                    if (index == 13) continue;
                    target = blockPos.offset(index / 9 - 1, index / 3 % 3 - 1, index % 3 - 1);
                } else {
                    int surface = index - 27;
                    target = blockPos2.relative(direction2, n2 + surface % width).above(surface / width - 1);
                }
                if (serverLevel.isOutsideBuildHeight(target) || !this.clearShieldFluidAt(serverLevel, target)) continue;
                shieldCursor = 0;
                return true;
        }
        if (shieldCursor >= total) {
            shieldCursor = 0;
            shieldChecked = true;
        }
        return false;
    }

    private boolean clearShieldFluidAt(ServerLevel serverLevel, BlockPos blockPos) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        boolean bl = QuarryBlockEntity.isBlock(blockState, Blocks.WATER);
        boolean bl2 = QuarryBlockEntity.isBlock(blockState, Blocks.LAVA);
        if (!bl && !bl2) {
            return false;
        }
        FluidState fluidState = blockState.getFluidState();
        if (this.collectFluids && fluidState.isSource()) {
            ItemStack itemStack = (bl ? Items.WATER_BUCKET : Items.LAVA_BUCKET).getDefaultInstance();
            StorageControllerBlockEntity storageController = this.linkedStorageController(serverLevel);
            boolean sendToStorage = storageController != null
                    && canInsertAllIntoStorage(storageController, List.of(itemStack));
            SpaceResult spaceResult = sendToStorage ? SpaceResult.READY : this.prepareSpaceForDrops(List.of(itemStack));
            if (this.hasEmptyBucket() && spaceResult == SpaceResult.READY) {
                this.consumeEmptyBucket();
                serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                if (sendToStorage) {
                    this.storeNetworkRemainders(
                            serverLevel,
                            blockPos,
                            insertIntoStorage(storageController, List.of(itemStack))
                    );
                } else {
                    this.insertDropsIntoActiveShulker(List.of(itemStack));
                    this.finishBoxIfFull();
                }
                this.setChanged();
                return true;
            }
        }
        serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
        this.setChanged();
        return true;
    }

    private static List<ItemStack> getDropsCompat(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.world.level.storage.loot.LootParams$Builder");
            Object object2 = null;
            for (Constructor<?> constructor : clazz.getConstructors()) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length != 1 || !parameterTypes[0].isInstance(serverLevel)) continue;
                object2 = constructor.newInstance(serverLevel);
                break;
            }
            if (object2 == null) {
                return null;
            }
            Class<?> clazz2 = Class.forName("net.minecraft.world.level.storage.loot.parameters.LootContextParams");
            Object object3 = clazz2.getField("ORIGIN").get(null);
            Object object4 = clazz2.getField("BLOCK_STATE").get(null);
            Object blockEntityParam = clazz2.getField("BLOCK_ENTITY").get(null);
            Object object = clazz2.getField("TOOL").get(null);
            Object object5 = net.minecraft.world.phys.Vec3.atCenterOf(blockPos);
            if ((object2 = QuarryBlockEntity.putLootParameter(object2, "withParameter", object3, object5)) == null) {
                return null;
            }
            if ((object2 = QuarryBlockEntity.putLootParameter(object2, "withParameter", object4, blockState)) == null) {
                return null;
            }
            if ((object2 = QuarryBlockEntity.putLootParameter(object2, "withOptionalParameter", blockEntityParam, serverLevel.getBlockEntity(blockPos))) == null) {
                return null;
            }
            if ((object2 = QuarryBlockEntity.putLootParameter(object2, "withParameter", object, itemStack)) == null) {
                return null;
            }
            for (Method method : blockState.getClass().getMethods()) {
                Object object6;
                if (!method.getName().equals("getDrops") || method.getParameterCount() != 1 || !method.getParameterTypes()[0].isInstance(object2) || !((object6 = method.invoke((Object)blockState, object2)) instanceof List)) continue;
                List list = (List)object6;
                return list;
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return null;
    }

    private static Object putLootParameter(Object object, String string, Object object2, Object object3) {
        try {
            for (Method method : object.getClass().getMethods()) {
                Class<?>[] classArray;
                if (!method.getName().equals(string) || method.getParameterCount() != 2 || !(classArray = method.getParameterTypes())[0].isInstance(object2) || object3 != null && !classArray[1].isInstance(object3) && classArray[1] != Object.class) continue;
                Object object4 = method.invoke(object, object2, object3);
                return object4 != null ? object4 : object;
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return null;
    }

    private static boolean isBlock(BlockState blockState, Block block) {
        try {
            for (Method method : BlockState.class.getMethods()) {
                if (!method.getName().equals("getBlock") || method.getParameterCount() != 0) continue;
                return method.invoke((Object)blockState, new Object[0]) == block;
            }
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            // empty catch block
        }
        return false;
    }

    private MiningResult mineFluidSourceBucket(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        if (!this.hasEmptyBucket()) {
            return MiningResult.NO_BUCKET;
        }
        StorageControllerBlockEntity storageController = this.linkedStorageController(serverLevel);
        boolean sendToStorage = storageController != null
                && canInsertAllIntoStorage(storageController, List.of(itemStack));
        if (!sendToStorage) {
            SpaceResult spaceResult = this.prepareSpaceForDrops(List.of(itemStack));
            if (spaceResult == SpaceResult.NO_SHULKER) {
                return MiningResult.NO_SHULKER;
            }
            if (spaceResult == SpaceResult.NO_OUTPUT_SPACE) {
                return MiningResult.STORAGE_FULL;
            }
        }
        this.consumeEmptyBucket();
        serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
        if (sendToStorage) {
            this.storeNetworkRemainders(serverLevel, blockPos, insertIntoStorage(storageController, List.of(itemStack)));
        } else {
            this.insertDropsIntoActiveShulker(List.of(itemStack));
            this.finishBoxIfFull();
        }
        this.setChanged();
        return MiningResult.MINED;
    }

    private boolean addSilkTouch(ServerLevel serverLevel, ItemStack itemStack) {
        try {
            Object object = QuarryBlockEntity.invokeNoArg(serverLevel, "registryAccess");
            if (object == null) {
                return false;
            }
            Class<?> clazz = Class.forName("net.minecraft.core.registries.Registries");
            Object object2 = clazz.getField("ENCHANTMENT").get(null);
            Object object3 = QuarryBlockEntity.invokeOneArg(object, "lookupOrThrow", object2);
            if (object3 == null) {
                object3 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object, "lookup", object2));
            }
            if (object3 == null) {
                return false;
            }
            Class<?> clazz2 = Class.forName("net.minecraft.world.item.enchantment.Enchantments");
            Object object4 = clazz2.getField("SILK_TOUCH").get(null);
            Object object5 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object3, "get", object4));
            if (object5 == null) {
                object5 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object3, "getHolder", object4));
            }
            if (object5 == null) {
                object5 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object3, "getOrThrow", object4));
            }
            if (object5 == null) {
                return false;
            }
            for (Method method : itemStack.getClass().getMethods()) {
                Class<?>[] classArray;
                if (!method.getName().equals("enchant") || method.getParameterCount() != 2 || !(classArray = method.getParameterTypes())[0].isInstance(object5) || classArray[1] != Integer.TYPE && classArray[1] != Integer.class) continue;
                method.invoke((Object)itemStack, object5, 1);
                return true;
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return false;
    }

    private static List<ItemStack> getSilkFallbackDrops(BlockState blockState) {
        try {
            ItemStack itemStack;
            Object object = QuarryBlockEntity.invokeNoArg(blockState, "getBlock");
            Object object2 = QuarryBlockEntity.invokeNoArg(object, "asItem");
            Object object3 = QuarryBlockEntity.invokeNoArg(object2, "getDefaultInstance");
            if (object3 instanceof ItemStack && !(itemStack = (ItemStack)object3).isEmpty()) {
                return List.of(itemStack);
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
        return null;
    }

    private static Object invokeNoArg(Object object, String string) {
        if (object == null) {
            return null;
        }
        try {
            for (Method method : object.getClass().getMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != 0) continue;
                return method.invoke(object, new Object[0]);
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return null;
    }

    private static Object invokeOneArg(Object object, String string, Object object2) {
        if (object == null) {
            return null;
        }
        try {
            for (Method method : object.getClass().getMethods()) {
                if (!method.getName().equals(string) || method.getParameterCount() != 1) continue;
                Class<?> clazz = method.getParameterTypes()[0];
                if (object2 != null && !clazz.isInstance(object2) && clazz != Object.class) continue;
                return method.invoke(object, object2);
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return null;
    }

    private static Object unwrapOptional(Object object) {
        if (object instanceof Optional) {
            Optional optional = (Optional)object;
            return optional.orElse(null);
        }
        return object;
    }

    private boolean tryAdvance(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, TrackLayout trackLayout) {
        Direction direction = trackLayout.facing();
        int n = trackLayout.railOffset();
        Direction direction2 = direction.getClockWise();
        BlockPos blockPos2 = blockPos.relative(direction);
        BlockPos blockPos3 = blockPos.relative(direction2, n);
        BlockPos blockPos4 = blockPos3.relative(direction);
        if (!serverLevel.getBlockState(blockPos2).isAir()) {
            return false;
        }
        if (!QuarryBlockEntity.isRail(serverLevel, blockPos2.below()) || !QuarryBlockEntity.isRail(serverLevel, blockPos4.below())) {
            return false;
        }
        if (!serverLevel.setBlock(blockPos2, blockState, 3)) {
            return false;
        }
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos2);
        if (!(blockEntity instanceof QuarryBlockEntity)) {
            serverLevel.removeBlock(blockPos2, false);
            return false;
        }
        QuarryBlockEntity quarryBlockEntity = (QuarryBlockEntity)blockEntity;
        quarryBlockEntity.copyStateFrom(this);
        quarryBlockEntity.facing = direction;
        quarryBlockEntity.railOffset = n;
        quarryBlockEntity.hasMoved = true;
        quarryBlockEntity.directionVerified = true;
        quarryBlockEntity.workTicks = 0;
        quarryBlockEntity.setChanged();
        PersistenceCompat.markMoveDirty(serverLevel, blockPos, blockPos2);
        this.beingMoved = true;
        for (int i = 0; i < this.items.size(); ++i) {
            this.items.set(i, ItemStack.EMPTY);
        }
        this.workingShulkerSlot = -1;
        this.setChanged();
        PersistenceCompat.markMoveDirty(serverLevel, blockPos, blockPos2);
        VisualCompat.removeAt(serverLevel, blockPos);
        serverLevel.removeBlock(blockPos, true);
        VisualCompat.tick(serverLevel, blockPos2, (Object)quarryBlockEntity);
        return true;
    }

    private void copyStateFrom(QuarryBlockEntity quarryBlockEntity) {
        for (int i = 0; i < this.items.size(); ++i) {
            this.items.set(i, quarryBlockEntity.items.get(i).copy());
        }
        this.fuelTicks = quarryBlockEntity.fuelTicks;
        this.silkTouch = quarryBlockEntity.silkTouch;
        this.hasMoved = quarryBlockEntity.hasMoved;
        this.directionVerified = quarryBlockEntity.directionVerified;
        this.railOffset = quarryBlockEntity.railOffset;
        this.workingShulkerSlot = quarryBlockEntity.workingShulkerSlot;
        this.facing = quarryBlockEntity.facing;
        this.ledsInitialized = false;
        this.collectFluids = quarryBlockEntity.collectFluids;
        this.miningHeight = quarryBlockEntity.miningHeight;
        resetScan();
    }

    public void setFacing(Direction direction) {
        if (direction == Direction.NORTH || direction == Direction.SOUTH || direction == Direction.EAST || direction == Direction.WEST) {
            this.facing = direction;
            resetScan();
            this.setChanged();
        }
    }

    public boolean toggleSilkTouch() {
        this.silkTouch = !this.silkTouch;
        this.setChanged();
        this.syncSettingsButtons();
        return this.silkTouch;
    }

    public boolean isBeingMoved() {
        return this.beingMoved;
    }

    private boolean ensureFuelLoaded() {
        if (this.fuelTicks > 0) {
            return true;
        }
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = (ItemStack)this.items.get(i);
            if (!QuarryBlockEntity.isFuel(itemStack)) continue;
            itemStack.shrink(1);
            this.fuelTicks = 3600;
            this.setChanged();
            return true;
        }
        return false;
    }

    private boolean hasFuelAvailable() {
        if (this.fuelTicks > 0) {
            return true;
        }
        for (int i = 0; i < this.items.size(); ++i) {
            if (!QuarryBlockEntity.isFuel((ItemStack)this.items.get(i))) continue;
            return true;
        }
        return false;
    }

    private void consumeFuelForWorkCycle() {
        this.fuelTicks = Math.max(0, this.fuelTicks - 2);
        this.setChanged();
    }

    private static boolean isFuel(ItemStack itemStack) {
        String string;
        String string2;
        Object object;
        if (itemStack.isEmpty()) {
            return false;
        }
        Item item = itemStack.getItem();
        return item == Items.COAL || item == Items.CHARCOAL || (object = QuarryBlockEntity.invokeNoArg(item, "getDescriptionId")) != null && ("item.minecraft.coal".equalsIgnoreCase(string2 = String.valueOf(object)) || "item.minecraft.charcoal".equalsIgnoreCase(string2) || "minecraft:coal".equalsIgnoreCase(string2) || "minecraft:charcoal".equalsIgnoreCase(string2) || "coal".equalsIgnoreCase(string2) || "charcoal".equalsIgnoreCase(string2) || string2.endsWith(".coal") || string2.endsWith(".charcoal")) || (string = String.valueOf(itemStack).toLowerCase()).contains("charcoal") || string.contains("coal");
    }

    private boolean hasEmptyBucket() {
        for (int i = 0; i < this.items.size(); ++i) {
            if (((ItemStack)this.items.get(i)).isEmpty() || ((ItemStack)this.items.get(i)).getItem() != Items.BUCKET) continue;
            return true;
        }
        return false;
    }

    private void consumeEmptyBucket() {
        for (int i = 0; i < this.items.size(); ++i) {
            ItemStack itemStack = (ItemStack)this.items.get(i);
            if (itemStack.isEmpty() || itemStack.getItem() != Items.BUCKET) continue;
            itemStack.shrink(1);
            this.setChanged();
            return;
        }
    }

    private boolean ensureActiveShulker() {
        return this.findWorkingShulkerSlot() >= 0;
    }

    private int findWorkingShulkerSlot() {
        int n;
        ItemStack itemStack;
        if (this.workingShulkerSlot >= 0 && this.workingShulkerSlot < 27 && QuarryBlockEntity.isShulker(itemStack = (ItemStack)this.items.get(this.workingShulkerSlot)) && itemStack.getCount() == 1) {
            return this.workingShulkerSlot;
        }
        this.workingShulkerSlot = -1;
        for (n = 0; n < 27; ++n) {
            ItemStack itemStack2 = (ItemStack)this.items.get(n);
            if (!QuarryBlockEntity.isShulker(itemStack2) || itemStack2.getCount() != 1) continue;
            this.workingShulkerSlot = n;
            return n;
        }
        for (n = 0; n < 27; ++n) {
            int n2;
            if (!QuarryBlockEntity.isEmptyShulker((ItemStack)this.items.get(n)) || (n2 = this.activateSingleShulker(n)) < 0) continue;
            return n2;
        }
        return -1;
    }

    private int activateSingleShulker(int n) {
        ItemStack itemStack = (ItemStack)this.items.get(n);
        if (!QuarryBlockEntity.isEmptyShulker(itemStack)) {
            return -1;
        }
        if (itemStack.getCount() == 1) {
            this.workingShulkerSlot = n;
            return n;
        }
        int n2 = this.findEmptyInputSlot(n);
        if (n2 < 0) {
            return -1;
        }
        ItemStack itemStack2 = itemStack.copy();
        itemStack2.setCount(1);
        itemStack.shrink(1);
        this.items.set(n2, itemStack2);
        this.workingShulkerSlot = n2;
        this.setChanged();
        return n2;
    }

    private int findEmptyInputSlot(int n) {
        for (int i = 0; i < 27; ++i) {
            if (i == n || !((ItemStack)this.items.get(i)).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private boolean hasActiveShulker() {
        return this.findWorkingShulkerSlot() >= 0;
    }

    private boolean isActiveShulkerFull() {
        return this.hasActiveShulker() && QuarryBlockEntity.allSlotsOccupied(this.getActiveContents());
    }

    private void finishBoxIfFull() {
        if (this.isActiveShulkerFull()) {
            this.finalizeActiveShulker();
        }
    }

    private boolean finalizeActiveShulker() {
        int n = this.findWorkingShulkerSlot();
        if (n < 0 || !this.isActiveShulkerFull()) {
            return true;
        }
        int n2 = this.findEmptyOutputSlot();
        if (n2 < 0) {
            return false;
        }
        ItemStack itemStack = ((ItemStack)this.items.get(n)).copy();
        itemStack.setCount(1);
        this.items.set(n2, itemStack);
        this.items.set(n, ItemStack.EMPTY);
        this.workingShulkerSlot = -1;
        this.setChanged();
        return true;
    }

    private int findEmptyOutputSlot() {
        for (int i = OUTPUT_START; i <= OUTPUT_END; ++i) {
            if (i == this.workingShulkerSlot || !((ItemStack)this.items.get(i)).isEmpty()) continue;
            return i;
        }
        return -1;
    }

    private NonNullList<ItemStack> getActiveContents() {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(SHULKER_SIZE, ItemStack.EMPTY);
        int n = this.findWorkingShulkerSlot();
        if (n < 0) {
            return nonNullList;
        }
        ItemContainerContents itemContainerContents = this.items.get(n).getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        itemContainerContents.copyInto(nonNullList);
        return nonNullList;
    }

    private void setActiveContents(NonNullList<ItemStack> nonNullList) {
        int n = this.findWorkingShulkerSlot();
        if (n >= 0) {
            this.items.get(n).set(DataComponents.CONTAINER, ItemContainerContents.fromItems(nonNullList));
        }
    }

    private static boolean isShulker(ItemStack itemStack) {
        return !itemStack.isEmpty() && Block.byItem((Item)itemStack.getItem()) instanceof ShulkerBoxBlock;
    }

    private static boolean isEmptyShulker(ItemStack itemStack) {
        if (!QuarryBlockEntity.isShulker(itemStack)) {
            return false;
        }
        ItemContainerContents itemContainerContents = itemStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        return itemContainerContents.allItemsCopyStream().allMatch(ItemStack::isEmpty);
    }

    private SpaceResult prepareSpaceForDrops(List<ItemStack> list) {
        if (!this.ensureActiveShulker()) {
            return SpaceResult.NO_SHULKER;
        }
        NonNullList<ItemStack> nonNullList = this.getActiveContents();
        if (QuarryBlockEntity.canInsertAll(nonNullList, list)) {
            return SpaceResult.READY;
        }
        if (QuarryBlockEntity.allSlotsOccupied(nonNullList)) {
            if (!this.finalizeActiveShulker()) {
                return SpaceResult.NO_OUTPUT_SPACE;
            }
            if (!this.ensureActiveShulker()) {
                return SpaceResult.NO_SHULKER;
            }
            nonNullList = this.getActiveContents();
            if (QuarryBlockEntity.canInsertAll(nonNullList, list)) {
                return SpaceResult.READY;
            }
        }
        return SpaceResult.NO_OUTPUT_SPACE;
    }

    private void insertDropsIntoActiveShulker(List<ItemStack> list) {
        NonNullList<ItemStack> nonNullList = this.getActiveContents();
        for (ItemStack itemStack : list) {
            QuarryBlockEntity.insertInto(nonNullList, itemStack.copy());
        }
        this.setActiveContents(nonNullList);
    }

    private StorageControllerBlockEntity linkedStorageController(ServerLevel level) {
        ItemStack module = this.items.get(WIRELESS_MODULE_SLOT);
        return WirelessModuleBinding.isPrimedModule(module)
                ? WirelessModuleBinding.resolve(level, module)
                : null;
    }

    private static boolean canInsertAllIntoStorage(
            StorageControllerBlockEntity controller,
            List<ItemStack> drops
    ) {
        for (ItemStack drop : drops) {
            if (!controller.insert(drop.copy(), TransferMode.SIMULATE).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<ItemStack> insertIntoStorage(
            StorageControllerBlockEntity controller,
            List<ItemStack> drops
    ) {
        List<ItemStack> remainders = new ArrayList<>();
        for (ItemStack drop : drops) {
            ItemStack remainder = controller.insert(drop.copy(), TransferMode.EXECUTE);
            if (!remainder.isEmpty()) {
                remainders.add(remainder);
            }
        }
        return remainders;
    }

    private void storeNetworkRemainders(ServerLevel level, BlockPos minedPos, List<ItemStack> remainders) {
        if (remainders.isEmpty()) {
            return;
        }
        if (this.prepareSpaceForDrops(remainders) == SpaceResult.READY) {
            this.insertDropsIntoActiveShulker(remainders);
            this.finishBoxIfFull();
            return;
        }
        for (ItemStack remainder : remainders) {
            Block.popResource(level, minedPos, remainder);
        }
    }

    private static boolean canInsertAll(NonNullList<ItemStack> nonNullList, List<ItemStack> list) {
        NonNullList<ItemStack> nonNullList2 = NonNullList.withSize(nonNullList.size(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            nonNullList2.set(i, nonNullList.get(i).copy());
        }
        for (ItemStack itemStack : list) {
            if (QuarryBlockEntity.insertInto(nonNullList2, itemStack.copy())) continue;
            return false;
        }
        return true;
    }

    private static boolean insertInto(NonNullList<ItemStack> nonNullList, ItemStack itemStack) {
        int n;
        if (itemStack.isEmpty()) {
            return true;
        }
        for (n = 0; n < nonNullList.size() && !itemStack.isEmpty(); ++n) {
            int n2;
            ItemStack itemStack2 = (ItemStack)nonNullList.get(n);
            if (itemStack2.isEmpty() || !ItemStack.isSameItemSameComponents((ItemStack)itemStack2, (ItemStack)itemStack) || (n2 = itemStack2.getMaxStackSize() - itemStack2.getCount()) <= 0) continue;
            int n3 = Math.min(n2, itemStack.getCount());
            itemStack2.grow(n3);
            itemStack.shrink(n3);
        }
        for (n = 0; n < nonNullList.size() && !itemStack.isEmpty(); ++n) {
            if (!((ItemStack)nonNullList.get(n)).isEmpty()) continue;
            int n4 = Math.min(itemStack.getMaxStackSize(), itemStack.getCount());
            ItemStack itemStack3 = itemStack.copy();
            itemStack3.setCount(n4);
            nonNullList.set(n, itemStack3);
            itemStack.shrink(n4);
        }
        return itemStack.isEmpty();
    }

    private static boolean allSlotsOccupied(NonNullList<ItemStack> nonNullList) {
        for (ItemStack itemStack : nonNullList) {
            if (!itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    public int getContainerSize() {
        return 54;
    }

    public boolean canPlaceItem(int n, ItemStack itemStack) {
        if (n == WIRELESS_MODULE_SLOT) {
            return itemStack.is(LumungusStorageItems.WIRELESS_NETWORK_MODULE);
        }
        if (n < 0 || n >= 27) {
            return false;
        }
        return QuarryBlockEntity.isFuel(itemStack) || !itemStack.isEmpty() && itemStack.getItem() == Items.BUCKET || QuarryBlockEntity.isEmptyShulker(itemStack);
    }

    private void updateStatusLeds(ServerLevel serverLevel, BlockPos blockPos) {
        boolean bl = this.hasFuelAvailable();
        boolean bl2 = this.hasParallelRailForLed(serverLevel, blockPos);
        if (this.ledsInitialized && bl == this.ledCoalOk && bl2 == this.ledRailOk) {
            return;
        }
        BlockState blockState = serverLevel.getBlockState(blockPos);
        BlockState blockState2 = QuarryBlockEntity.setBooleanPropertyCompat(blockState, QuarryBlock.COAL_OK, bl);
        if ((blockState2 = QuarryBlockEntity.setBooleanPropertyCompat(blockState2, QuarryBlock.RAIL_OK, bl2)) != null && blockState2 != blockState) {
            serverLevel.setBlock(blockPos, blockState2, 3);
        }
        this.ledCoalOk = bl;
        this.ledRailOk = bl2;
        this.ledsInitialized = true;
    }

    private boolean hasParallelRailForLed(ServerLevel serverLevel, BlockPos blockPos) {
        if (!QuarryBlockEntity.isRail(serverLevel, blockPos.below())) {
            return false;
        }
        if (this.railOffset != 0) {
            Direction direction = this.facing.getClockWise();
            BlockPos blockPos2 = blockPos.relative(direction, this.railOffset).below();
            return QuarryBlockEntity.isRail(serverLevel, blockPos2);
        }
        Direction[] directionArray = new Direction[]{this.facing, this.facing.getOpposite(), this.facing.getClockWise(), this.facing.getClockWise().getOpposite()};
        for (Direction direction : directionArray) {
            if (QuarryBlockEntity.detectCompanionRail(serverLevel, blockPos, direction) == 0) continue;
            return true;
        }
        return false;
    }

    private static BlockState setBooleanPropertyCompat(BlockState blockState, Object object, boolean bl) {
        if (blockState == null || object == null) {
            return blockState;
        }
        try {
            for (Method method : blockState.getClass().getMethods()) {
                Object object2;
                Class<?>[] classArray;
                if (!method.getName().equals("setValue") || method.getParameterCount() != 2 || !(classArray = method.getParameterTypes())[0].isInstance(object) || !((object2 = method.invoke((Object)blockState, object, bl)) instanceof BlockState)) continue;
                BlockState blockState2 = (BlockState)object2;
                return blockState2;
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return blockState;
    }

    public String getOperationalStatus(ServerLevel serverLevel, BlockPos blockPos) {
        String storageStatus = this.storageStatus(serverLevel);
        if (!QuarryBlockEntity.isRail(serverLevel, blockPos.below())) {
            return "STOP: primary rail missing | " + storageStatus;
        }
        if (!this.hasFuelAvailable()) {
            return "STOP: no coal/charcoal | " + storageStatus;
        }
        TrackLayout trackLayout = this.resolveTrackLayout(serverLevel, blockPos);
        if (trackLayout == null) {
            return "STOP: second parallel rail not detected | " + storageStatus;
        }
        int n = Math.abs(trackLayout.railOffset()) + 1;
        return "READY | dual rail OK | width " + n + " | height " + miningHeight + " | fuel " + this.fuelTicks + " | " + storageStatus;
    }

    private String storageStatus(ServerLevel level) {
        ItemStack module = this.items.get(WIRELESS_MODULE_SLOT);
        if (module.isEmpty()) {
            return "storage: no module";
        }
        if (!WirelessModuleBinding.isPrimedModule(module)) {
            return "storage: module unbound";
        }
        return this.linkedStorageController(level) == null
                ? "storage: controller unavailable"
                : "storage: linked";
    }

    protected Component getDefaultName() {
        return RailQuarryMod.translatableText("container.railquarry.quarry");
    }

    protected AbstractContainerMenu createMenu(int n, Inventory inventory) {
        this.syncSettingsButtons();
        return new de.djdaddy.railquarry.menu.QuarryMenu(n, inventory, this);
    }

    @Override
    public BlockPos getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
        return worldPosition;
    }

    public int getMiningHeight() {
        return miningHeight;
    }

    public void setMiningHeight(int height) {
        miningHeight = Math.clamp(height, 1, MAX_HEIGHT);
        resetScan();
        setChanged();
    }

    private int effectiveHeight(ServerLevel level, BlockPos pos) {
        return Math.max(0, Math.min(miningHeight, level.getMaxY() - pos.getY() + 1));
    }

    private void resetScan() {
        miningCursor = 0;
        shieldCursor = 0;
        shieldChecked = false;
    }

    protected void saveAdditional(ValueOutput valueOutput) {
        this.syncSettingsButtons();
        ContainerHelper.saveAllItems((ValueOutput)valueOutput, this.items);
        valueOutput.putInt("FuelTicks", this.fuelTicks);
        valueOutput.putInt("MiningHeight", miningHeight);
        valueOutput.putBoolean("SilkTouch", this.silkTouch);
        valueOutput.putBoolean("HasMoved", this.hasMoved);
        valueOutput.putBoolean("DirectionVerified", this.directionVerified);
        valueOutput.putInt("RailOffset", this.railOffset);
        valueOutput.putInt("WorkingShulkerSlot", this.workingShulkerSlot);
        valueOutput.putInt("Facing", this.facing.ordinal());
        valueOutput.putBoolean("CollectFluids", this.collectFluids);
        super.saveAdditional(valueOutput);
    }

    public void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        ContainerHelper.loadAllItems((ValueInput)valueInput, this.items);
        this.fuelTicks = valueInput.getIntOr("FuelTicks", 0);
        miningHeight = Math.clamp(valueInput.getIntOr("MiningHeight", HEIGHT), 1, MAX_HEIGHT);
        resetScan();
        this.silkTouch = valueInput.getBooleanOr("SilkTouch", false);
        this.hasMoved = valueInput.getBooleanOr("HasMoved", false);
        this.directionVerified = valueInput.getBooleanOr("DirectionVerified", false);
        this.railOffset = valueInput.getIntOr("RailOffset", 0);
        this.workingShulkerSlot = valueInput.getIntOr("WorkingShulkerSlot", -1);
        if (this.workingShulkerSlot < -1 || this.workingShulkerSlot >= 54) {
            this.workingShulkerSlot = -1;
        }
        if (Math.abs(this.railOffset) >= 65) {
            this.railOffset = 0;
        }
        int n = valueInput.getIntOr("Facing", Direction.NORTH.ordinal());
        Direction[] directionArray = Direction.values();
        this.facing = directionArray[Math.max(0, Math.min(directionArray.length - 1, n))];
        if (this.facing != Direction.NORTH && this.facing != Direction.SOUTH && this.facing != Direction.EAST && this.facing != Direction.WEST) {
            this.facing = Direction.NORTH;
        }
        this.collectFluids = valueInput.getBooleanOr("CollectFluids", true);
        this.syncSettingsButtons();
    }

    public void setRemoved() {
        ChunkLoaderCompat.onRemoved((Object)this);
        super.setRemoved();
    }

    private boolean addFortuneThree(ServerLevel serverLevel, ItemStack itemStack) {
        try {
            Object object = QuarryBlockEntity.invokeNoArg(serverLevel, "registryAccess");
            if (object == null) {
                return false;
            }
            Class<?> clazz = Class.forName("net.minecraft.core.registries.Registries");
            Object object2 = clazz.getField("ENCHANTMENT").get(null);
            Object object3 = QuarryBlockEntity.invokeOneArg(object, "lookupOrThrow", object2);
            if (object3 == null) {
                object3 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object, "lookup", object2));
            }
            if (object3 == null) {
                return false;
            }
            Class<?> clazz2 = Class.forName("net.minecraft.world.item.enchantment.Enchantments");
            Object object4 = clazz2.getField("FORTUNE").get(null);
            Object object5 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object3, "get", object4));
            if (object5 == null) {
                object5 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object3, "getHolder", object4));
            }
            if (object5 == null) {
                object5 = QuarryBlockEntity.unwrapOptional(QuarryBlockEntity.invokeOneArg(object3, "getOrThrow", object4));
            }
            if (object5 == null) {
                return false;
            }
            for (Method method : itemStack.getClass().getMethods()) {
                Class<?>[] classArray;
                if (!method.getName().equals("enchant") || method.getParameterCount() != 2 || !(classArray = method.getParameterTypes())[0].isInstance(object5) || classArray[1] != Integer.TYPE && classArray[1] != Integer.class) continue;
                method.invoke((Object)itemStack, object5, 3);
                return true;
            }
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            // empty catch block
        }
        return false;
    }

    private void applyFortuneIfNeeded(ServerLevel serverLevel, ItemStack itemStack) {
        if (this.silkTouch) {
            return;
        }
        this.addFortuneThree(serverLevel, itemStack);
    }

    private MiningResult mineFluidSource(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
        return (MiningResult)((Object)FluidDrainCompat.mine((Object)this, serverLevel, blockPos, itemStack));
    }

    private ItemStack makeSettingsButton(Item item, String string) {
        ItemStack itemStack = item.getDefaultInstance();
        itemStack.set(DataComponents.CUSTOM_NAME, RailQuarryMod.literalText(string));
        return itemStack;
    }

    public boolean toggleFluidCollection() {
        boolean bl;
        this.collectFluids = bl = this.collectFluids ^ true;
        this.setChanged();
        this.syncSettingsButtons();
        return bl;
    }

    private void syncSettingsButtons() {
        if (!QuarryBlockEntity.isShulker((ItemStack)this.items.get(52))) {
            this.items.set(52, ItemStack.EMPTY);
        }
        if (!QuarryBlockEntity.isShulker((ItemStack)this.items.get(53))) {
            this.items.set(53, ItemStack.EMPTY);
        }
    }

    public ItemStack removeItem(int n, int n2) {
        ItemStack itemStack;
        if (n == 52) {
            if (!QuarryBlockEntity.isShulker((ItemStack)this.items.get(52))) {
                this.toggleSilkTouch();
                return ItemStack.EMPTY;
            }
        } else if (n == 53 && !QuarryBlockEntity.isShulker((ItemStack)this.items.get(53))) {
            this.toggleFluidCollection();
            return ItemStack.EMPTY;
        }
        if ((itemStack = (ItemStack)this.items.get(n)).isEmpty()) {
            return ItemStack.EMPTY;
        }
        int n3 = Math.min(n2, itemStack.getCount());
        ItemStack itemStack2 = itemStack.copy();
        itemStack2.setCount(n3);
        itemStack.shrink(n3);
        if (itemStack.isEmpty()) {
            this.items.set(n, ItemStack.EMPTY);
        }
        this.setChanged();
        return itemStack2;
    }

    public ItemStack getMenuItem(int n) {
        if (n == 52) {
            ItemStack itemStack = (ItemStack)this.items.get(52);
            if (QuarryBlockEntity.isShulker(itemStack)) {
                return itemStack;
            }
            return this.makeSettingsButton(this.silkTouch ? Items.GLASS : Items.EMERALD, this.silkTouch ? "Mining: SILK TOUCH [click]" : "Mining: FORTUNE III [click]");
        }
        if (n == 53) {
            ItemStack itemStack = (ItemStack)this.items.get(53);
            if (QuarryBlockEntity.isShulker(itemStack)) {
                return itemStack;
            }
            return this.makeSettingsButton(this.collectFluids ? Items.WATER_BUCKET : Items.SPONGE, this.collectFluids ? "Fluids: FILL BUCKETS [click]" : "Fluids: DESTROY [click]");
        }
        return this.getItem(n);
    }

    public void setChanged() {
        super.setChanged();
        PersistenceCompat.markBlockEntityDirty((Object)this);
    }

    private record TrackLayout(Direction facing, int railOffset) {
    }

    private static enum MiningResult {
        SCANNING,
        MINED,
        STORAGE_FULL,
        NO_SHULKER,
        NO_BUCKET,
        SLICE_CLEAR;

    }

    private static enum SpaceResult {
        READY,
        NO_SHULKER,
        NO_OUTPUT_SPACE;

    }
}
