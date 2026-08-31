package dev.lumungus.integration.gametest;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.entity.InventoryCableConnectorBlockEntity;
import com.tom.storagemod.inventory.RemoteConnections;
import dev.lumungus.integration.toms.MinecraftTomsInventoryWorldView;
import dev.lumungus.integration.toms.TomsDryRunReport;
import dev.lumungus.integration.toms.TomsReadOnlyNetworkScanner;
import dev.lumungus.integration.toms.TomsRemoteConnectorInspector;
import dev.lumungus.integration.toms.TomsRemoteConnectorStatus;
import dev.lumungus.integration.toms.TomsRemoteScanResult;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public final class TomsRemoteRuntimeIntegrationGameTest implements CustomTestMethodInvoker {
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
