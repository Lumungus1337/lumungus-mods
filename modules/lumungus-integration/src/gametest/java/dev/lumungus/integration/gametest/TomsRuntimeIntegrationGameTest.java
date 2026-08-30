package dev.lumungus.integration.gametest;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.entity.InventoryCableConnectorBlockEntity;
import dev.lumungus.integration.toms.MinecraftTomsInventoryWorldView;
import dev.lumungus.integration.toms.TomsDryRunReport;
import dev.lumungus.integration.toms.TomsReadOnlyNetworkScanner;
import dev.lumungus.integration.toms.TomsRemoteConnectorInspector;
import dev.lumungus.integration.toms.TomsRemoteConnectorStatus;
import java.lang.reflect.Method;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class TomsRuntimeIntegrationGameTest implements CustomTestMethodInvoker {
    @GameTest
    public void readsARealToms211ConnectorWithoutMutatingIt(GameTestHelper context) {
        BlockPos connectorPos = new BlockPos(1, 1, 1);
        context.setBlock(connectorPos, Content.invCableConnector.get());
        InventoryCableConnectorBlockEntity connector = context.getBlockEntity(
                connectorPos,
                InventoryCableConnectorBlockEntity.class
        );
        MinecraftTomsInventoryWorldView world = new MinecraftTomsInventoryWorldView(context.getLevel());
        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(
                world,
                context.absolutePos(connectorPos),
                100
        );

        context.assertTrue(report.remoteConnectionsRequireScan(), "Tom's connector was not detected");
        context.assertTrue(connector.getChannel() == null, "Fresh Tom's connector unexpectedly has a channel");
        context.assertTrue(
                TomsRemoteConnectorInspector.inspect(context.getLevel(), report)
                        == TomsRemoteConnectorStatus.NONE_CONFIGURED,
                "Ordinary Tom's connector was treated as a remote channel"
        );

        connector.setChannel(UUID.randomUUID());
        context.assertTrue(
                TomsRemoteConnectorInspector.inspect(context.getLevel(), report)
                        == TomsRemoteConnectorStatus.CONFIGURED_NOT_READY,
                "Uninitialized remote channel was incorrectly approved"
        );
        context.succeed();
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }
}
