package dev.lumungus.machines.gametest;

import dev.lumungus.core.api.inventory.TransferMode;
import dev.lumungus.machines.block.entity.AutocrafterBlockEntity;
import dev.lumungus.machines.production.AutocrafterState;
import dev.lumungus.machines.registry.LumungusMachinesBlocks;
import dev.lumungus.storage.block.entity.DriveBayBlockEntity;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.registry.LumungusStorageBlocks;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import dev.lumungus.storage.registry.LumungusStorageItems;
import java.lang.reflect.Method;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AutocrafterGameTest implements CustomTestMethodInvoker {
    @GameTest(padding = 16)
    public void autocrafterConsumesAndStoresThroughPrimedWirelessModule(GameTestHelper context) {
        BlockPos controllerPos = new BlockPos(1, 1, 1);
        BlockPos driveBayPos = new BlockPos(2, 1, 1);
        BlockPos autocrafterPos = new BlockPos(10, 1, 1);
        context.setBlock(controllerPos, LumungusStorageBlocks.STORAGE_CONTROLLER);
        context.setBlock(driveBayPos, LumungusStorageBlocks.DRIVE_BAY);
        context.setBlock(autocrafterPos, LumungusMachinesBlocks.AUTOCRAFTER);

        StorageControllerBlockEntity controller = context.getBlockEntity(
                controllerPos,
                StorageControllerBlockEntity.class
        );
        DriveBayBlockEntity driveBay = context.getBlockEntity(driveBayPos, DriveBayBlockEntity.class);
        AutocrafterBlockEntity autocrafter = context.getBlockEntity(
                autocrafterPos,
                AutocrafterBlockEntity.class
        );
        driveBay.insertCell(new ItemStack(LumungusStorageItems.STORAGE_CELL_16K), TransferMode.EXECUTE);
        context.assertTrue(
                controller.insert(new ItemStack(Items.OAK_LOG), TransferMode.EXECUTE).isEmpty(),
                "Test log should fit into the storage network"
        );

        ItemStack module = new ItemStack(LumungusStorageItems.WIRELESS_NETWORK_MODULE);
        module.set(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER, new BoundStorageController(
                context.getLevel().dimension().identifier(),
                context.absolutePos(controllerPos),
                controller.getNetworkId()
        ));
        context.assertTrue(autocrafter.installWirelessModule(module), "Primed module should install");
        autocrafter.setTarget(new ItemStack(Items.OAK_PLANKS), 4);

        autocrafter.runCraftCycle();

        context.assertTrue(controller.count(new ItemStack(Items.OAK_LOG)) == 0, "Recipe should consume one log");
        context.assertTrue(
                controller.count(new ItemStack(Items.OAK_PLANKS)) == 4,
                "Recipe should return four planks to storage"
        );
        context.assertTrue(autocrafter.completedAmount() == 4, "Autocrafter should track produced items");
        context.assertTrue(autocrafter.state() == AutocrafterState.COMPLETE, "Autocrafter should complete its job");
        context.succeed();
    }

    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        method.invoke(this, context);
    }
}
