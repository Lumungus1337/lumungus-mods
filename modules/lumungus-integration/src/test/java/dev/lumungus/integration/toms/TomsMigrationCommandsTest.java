package dev.lumungus.integration.toms;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

final class TomsMigrationCommandsTest {
    @Test
    void registersTheMigrationScanCommandPath() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();

        TomsMigrationCommands.register(dispatcher);

        var lumungus = dispatcher.getRoot().getChild("lumungus");
        assertNotNull(lumungus);
        var migration = lumungus.getChild("migration");
        assertNotNull(migration);
        var scan = migration.getChild("scan");
        assertNotNull(scan);
        assertNotNull(scan.getChild("position"));
    }
}
