package dev.lumungus.integration.gametest;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.entity.InventoryCableConnectorBlockEntity;
import com.tom.storagemod.inventory.RemoteConnections;
import dev.lumungus.integration.migration.MigrationInventorySnapshot;
import dev.lumungus.integration.toms.MinecraftTomsInventoryWorldView;
import dev.lumungus.integration.toms.TomsDryRunReport;
import dev.lumungus.integration.toms.TomsInventorySnapshotCollector;
import dev.lumungus.integration.toms.TomsNetworkSegment;
import dev.lumungus.integration.toms.TomsReadOnlyNetworkScanner;
import dev.lumungus.integration.toms.TomsRemoteConnectorInspector;
import dev.lumungus.integration.toms.TomsRemoteConnectorStatus;
import dev.lumungus.integration.toms.TomsRemoteScanResult;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;

public final class TomsRemoteRuntimeIntegrationGameTest implements CustomTestMethodInvoker {
    @GameTest(maxTicks = 100)
    public void snapshotsInventoriesDiscoveredByRealTomsConnector(GameTestHelper context) {
        BlockPos connectorPos = new BlockPos(1, 2, 1);
        BlockPos firstBarrelPos = connectorPos.east();
        BlockPos secondBarrelPos = firstBarrelPos.east();
        context.setBlock(connectorPos, Content.connector.get());
        context.setBlock(firstBarrelPos, Blocks.BARREL);
        context.setBlock(secondBarrelPos, Blocks.BARREL);
        context.getBlockEntity(firstBarrelPos, BarrelBlockEntity.class).setItem(0, new ItemStack(Items.DIAMOND, 31));
        context.getBlockEntity(secondBarrelPos, BarrelBlockEntity.class).setItem(0, new ItemStack(Items.IRON_INGOT, 47));

        context.succeedWhen(() -> {
            TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(
                    new MinecraftTomsInventoryWorldView(context.getLevel()),
                    context.absolutePos(connectorPos),
                    100
            );
            MigrationInventorySnapshot snapshot = TomsInventorySnapshotCollector.capture(
                    java.util.List.of(new TomsNetworkSegment(context.getLevel(), report)),
                    "Real Tom's connector dry run"
            );
            context.assertValueEqual(snapshot.endpointCount(), 2, "Tom's discovered inventory count");
            context.assertValueEqual(snapshot.slotCount(), 54L, "Tom's discovered slot count");
            context.assertValueEqual(snapshot.totalAmount(), 78L, "Tom's discovered item count");
        });
    }

    @GameTest(maxTicks = 100)
    public void resolvesTwoRealToms211RemoteSegments(GameTestHelper context) {
        BlockPos firstPos = new BlockPos(1, 2, 1);
        BlockPos secondPos = new BlockPos(5, 2, 1);
        context.setBlock(firstPos.below(), Blocks.BEACON);
        context.setBlock(secondPos.below(), Blocks.BEACON);
        context.setBlock(firstPos, Content.invCableConnector.get());
        context.setBlock(secondPos, Content.invCableConnector.get());

        var channel = RemoteConnections.get(context.getLevel()).makeChannel(
                "Lumungus migration test",
                true,
                context.makeMockPlayer(GameType.CREATIVE)
        );
        context.getBlockEntity(firstPos, InventoryCableConnectorBlockEntity.class).setChannel(channel);
        context.getBlockEntity(secondPos, InventoryCableConnectorBlockEntity.class).setChannel(channel);

        context.succeedWhen(() -> {
            TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(
                    new MinecraftTomsInventoryWorldView(context.getLevel()),
                    context.absolutePos(firstPos),
                    100
            );
            TomsRemoteScanResult result = TomsRemoteConnectorInspector.resolve(
                    context.getLevel(), report, 100
            );
            context.assertTrue(
                    result.status() == TomsRemoteConnectorStatus.CONFIGURED_RESOLVED,
                    "Tom's remote channel is not ready yet: " + result.status()
            );
            context.assertValueEqual(result.segments().size(), 2, "Tom's remote network segments");
            context.assertValueEqual(result.blockCount(), 2, "Tom's remote network blocks");
            context.assertTrue(result.safeForInventorySnapshot(), "Resolved Tom's network remained blocked");
        });
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }
}
