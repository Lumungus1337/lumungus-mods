package dev.lumungus.integration.toms;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import dev.lumungus.integration.migration.MigrationInventorySnapshot;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class TomsMigrationCommands {
    private static final int MAX_SCAN_NODES = 100_000;

    private TomsMigrationCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> register(dispatcher));
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("lumungus")
                .then(literal("migration")
                        .then(literal("scan")
                                .then(argument("position", BlockPosArgument.blockPos())
                                        .executes(context -> scan(
                                                context.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(context, "position")
                                        ))))));
    }

    private static int scan(CommandSourceStack source, BlockPos start) {
        ServerLevel level = source.getLevel();
        MinecraftTomsInventoryWorldView world = new MinecraftTomsInventoryWorldView(level);
        TomsDryRunReport report = TomsReadOnlyNetworkScanner.scan(world, start, MAX_SCAN_NODES);
        if (report.blocks().isEmpty()) {
            source.sendFailure(Component.translatable("command.lumungus_integration.migration.not_toms"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "command.lumungus_integration.migration.blocks",
                report.blocks().size(),
                report.convertibleCount(),
                report.blockingCount()
        ), false);

        if (report.unloadedBoundary()) {
            source.sendFailure(Component.translatable("command.lumungus_integration.migration.unloaded"));
        }
        if (report.nodeLimitReached()) {
            source.sendFailure(Component.translatable(
                    "command.lumungus_integration.migration.limit",
                    MAX_SCAN_NODES
            ));
        }
        if (report.remoteConnectionsRequireScan()) {
            source.sendFailure(Component.translatable("command.lumungus_integration.migration.remote_pending"));
        }
        report.blocks().stream()
                .filter(block -> block.plan().disposition() != TomsMigrationDisposition.CONVERTIBLE)
                .limit(10)
                .forEach(block -> source.sendFailure(Component.translatable(
                        "command.lumungus_integration.migration.blocker",
                        block.position().toShortString(),
                        block.plan().sourceId().toString(),
                        block.plan().disposition().name()
                )));

        if (!report.safeForInventorySnapshot()) {
            source.sendFailure(Component.translatable("command.lumungus_integration.migration.blocked"));
            return 0;
        }

        MigrationInventorySnapshot snapshot = TomsInventorySnapshotCollector.capture(
                world,
                report,
                "Tom's Simple Storage dry run"
        );
        source.sendSuccess(() -> Component.translatable(
                "command.lumungus_integration.migration.inventory",
                snapshot.endpointCount(),
                snapshot.slotCount(),
                snapshot.totalAmount(),
                snapshot.distinctTypes()
        ), false);
        source.sendSuccess(
                () -> Component.translatable("command.lumungus_integration.migration.read_only_complete"),
                false
        );
        return report.blocks().size();
    }
}
