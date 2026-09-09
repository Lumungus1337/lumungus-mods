package de.djdaddy.railquarry;

import de.djdaddy.railquarry.block.entity.QuarryBlockEntity;
import de.djdaddy.railquarry.menu.QuarryMenu;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;

public final class QuarryHeightGameTest {
    private static final BlockPos POS = new BlockPos(1, 3, 1);

    private QuarryBlockEntity quarry(GameTestHelper context) {
        context.setBlock(POS, RailQuarryMod.QUARRY);
        return (QuarryBlockEntity) context.getLevel().getBlockEntity(context.absolutePos(POS));
    }

    @GameTest
    public void heightDefaultsClampsAndSurvivesReload(GameTestHelper context) {
        var quarry = quarry(context);
        context.assertTrue(quarry.getMiningHeight() == 8, "Legacy default must remain eight");
        quarry.setMiningHeight(0);
        context.assertTrue(quarry.getMiningHeight() == 1, "Lower bound");
        quarry.setMiningHeight(100);
        context.assertTrue(quarry.getMiningHeight() == 64, "Upper bound");
        quarry.setMiningHeight(37);
        var tag = quarry.saveWithoutMetadata(context.getLevel().registryAccess());
        quarry.setMiningHeight(8);
        quarry.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, context.getLevel().registryAccess(), tag));
        context.assertTrue(quarry.getMiningHeight() == 37, "Height lost on reload");
        quarry.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, context.getLevel().registryAccess(), new CompoundTag()));
        context.assertTrue(quarry.getMiningHeight() == 8, "Old save without height must use eight");
        context.succeed();
    }

    @GameTest
    public void miningRespectsSelectedHeight(GameTestHelper context) {
        var quarry = quarry(context);
        quarry.setItem(9, new ItemStack(Items.SHULKER_BOX));
        var base = context.absolutePos(POS);
        var low = base.north();
        var high = low.above(15);
        var outside = low.above(16);
        context.getLevel().setBlockAndUpdate(low, Blocks.STONE.defaultBlockState());
        context.getLevel().setBlockAndUpdate(high, Blocks.STONE.defaultBlockState());
        context.getLevel().setBlockAndUpdate(outside, Blocks.STONE.defaultBlockState());
        quarry.setMiningHeight(1);
        String result = invoke(quarry, "mineOneBlock", context.getLevel(), base, Direction.NORTH, 1).toString();
        context.assertTrue(result.equals("MINED"), "Height one should mine base, got " + result);
        context.assertTrue(context.getLevel().getBlockState(low).isAir(), "Base not mined");
        context.assertTrue(context.getLevel().getBlockState(high).is(Blocks.STONE), "Mined above chosen height");
        quarry.setMiningHeight(16);
        invoke(quarry, "mineOneBlock", context.getLevel(), base, Direction.NORTH, 1);
        context.assertTrue(context.getLevel().getBlockState(high).isAir(), "Height sixteen did not mine top row");
        context.assertTrue(context.getLevel().getBlockState(outside).is(Blocks.STONE), "Mined above top row");
        context.succeed();
    }

    @GameTest
    public void largeScansYieldAndRespectWorldTop(GameTestHelper context) {
        var quarry = quarry(context);
        quarry.setMiningHeight(64);
        var base = context.absolutePos(POS).above(100);
        String result = invoke(quarry, "mineOneBlock", context.getLevel(), base, Direction.NORTH, 64).toString();
        context.assertTrue(result.equals("SCANNING"), "Large scan must yield, not declare slice clear");
        int calls = 1;
        while (result.equals("SCANNING") && calls < 20) {
            result = invoke(quarry, "mineOneBlock", context.getLevel(), base, Direction.NORTH, 64).toString();
            calls++;
        }
        context.assertTrue(result.equals("SLICE_CLEAR") && calls == 17, "4160 positions must take 17 bounded scans");
        int top = context.getLevel().getMaxY();
        context.assertTrue((int) invoke(quarry, "effectiveHeight", context.getLevel(), new BlockPos(base.getX(), top, base.getZ())) == 1, "World ceiling must clamp to one");
        context.succeed();
    }

    @GameTest
    public void fluidShieldUsesSelectedHeight(GameTestHelper context) {
        var quarry = quarry(context);
        quarry.toggleFluidCollection();
        var base = context.absolutePos(POS);
        var water = base.north().above(16);
        context.getLevel().setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        quarry.setMiningHeight(1);
        invoke(quarry, "drainFluidShield", context.getLevel(), base, Direction.NORTH, 1);
        context.assertTrue(context.getLevel().getBlockState(water).is(Blocks.WATER), "Short shield reached too high");
        quarry.setMiningHeight(16);
        invoke(quarry, "drainFluidShield", context.getLevel(), base, Direction.NORTH, 1);
        context.assertTrue(context.getLevel().getBlockState(water).isAir(), "Tall shield missed upper boundary");
        context.succeed();
    }

    @GameTest
    public void movingCopiesHeightAndInventory(GameTestHelper context) {
        var quarry = quarry(context);
        quarry.setMiningHeight(29);
        quarry.setItem(0, new ItemStack(Items.COAL, 12));
        var base = context.absolutePos(POS);
        for (var pos : new BlockPos[]{base.below(), base.north().below(), base.east(2).below(), base.north().east(2).below()}) {
            context.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            context.getLevel().setBlockAndUpdate(pos, Blocks.RAIL.defaultBlockState());
        }
        try {
            Class<?> layoutType = Class.forName(QuarryBlockEntity.class.getName() + "$TrackLayout");
            var constructor = layoutType.getDeclaredConstructor(Direction.class, int.class);
            constructor.setAccessible(true);
            Object layout = constructor.newInstance(Direction.NORTH, 2);
            context.assertTrue((boolean) invoke(quarry, "tryAdvance", context.getLevel(), base, quarry.getBlockState(), layout), "Quarry did not advance");
            var moved = (QuarryBlockEntity) context.getLevel().getBlockEntity(base.north());
            context.assertTrue(moved.getMiningHeight() == 29 && moved.getItem(0).getCount() == 12, "Movement lost settings or inventory");
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
        context.succeed();
    }

    @GameTest
    public void menuRejectsInvalidHeights(GameTestHelper context) {
        var quarry = quarry(context);
        var player = context.makeMockServerPlayerInLevel();
        var pos = context.absolutePos(POS);
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        var menu = new QuarryMenu(1, player.getInventory(), quarry);
        context.assertTrue(!menu.clickMenuButton(player, 0) && !menu.clickMenuButton(player, 65), "Invalid network value accepted");
        context.assertTrue(menu.clickMenuButton(player, 64) && quarry.getMiningHeight() == 64, "Valid menu value rejected");
        player.setPos(pos.getX() + 100, pos.getY(), pos.getZ());
        context.assertTrue(!menu.clickMenuButton(player, 1), "Out-of-range player changed quarry");
        context.succeed();
    }

    private static Object invoke(Object target, String name, Object... args) {
        try {
            for (Method method : target.getClass().getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != args.length) continue;
                method.setAccessible(true);
                return method.invoke(target, args);
            }
            throw new NoSuchMethodException(name);
        } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
    }
}
