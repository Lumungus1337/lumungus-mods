package dev.lumungus.storage.item;

import dev.lumungus.storage.block.WirelessStorageControllerBlock;
import dev.lumungus.storage.block.entity.StorageControllerBlockEntity;
import dev.lumungus.storage.data.BoundStorageController;
import dev.lumungus.storage.menu.LumungusCraftingMenu;
import dev.lumungus.storage.registry.LumungusStorageDataComponents;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class PortableStorageInterfaceItem extends Item {
    private final WirelessStorageControllerBlock.WirelessTier tier;

    public PortableStorageInterfaceItem(Properties properties, WirelessStorageControllerBlock.WirelessTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> textConsumer,
            TooltipFlag flag
    ) {
        textConsumer.accept(Component.translatable("tooltip.lumungus_storage.portable_interface"));
        textConsumer.accept(Component.translatable("tooltip.lumungus_storage.portable_interface." + tier.label()));
        BoundStorageController bound = stack.get(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER);
        textConsumer.accept(bound == null
                ? Component.translatable("tooltip.lumungus_storage.portable_interface.unbound")
                : Component.translatable(
                        "tooltip.lumungus_storage.portable_interface.bound",
                        bound.dimension().toString(),
                        describePosition(bound.pos())));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!(player instanceof ServerPlayer serverPlayer) || !context.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof StorageControllerBlockEntity controller)) {
            return InteractionResult.PASS;
        }
        if (!tier.canReach(true, player.blockPosition(), controller.getBlockPos())) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.portable_interface.out_of_range",
                    tier.label()
            ));
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        stack.set(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER, new BoundStorageController(
                level.dimension().identifier(),
                controller.getBlockPos().immutable(),
                controller.getNetworkId()
        ));
        serverPlayer.sendSystemMessage(Component.translatable(
                "message.lumungus_storage.portable_interface.bound",
                tier.label(),
                controller.getNetworkLabel()
        ));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = player.getItemInHand(hand);
        BoundStorageController bound = stack.get(LumungusStorageDataComponents.BOUND_STORAGE_CONTROLLER);
        if (bound == null) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.portable_interface.unlinked",
                    tier.label()
            ));
            return InteractionResult.SUCCESS;
        }

        StorageControllerBlockEntity controller = boundController(bound, serverPlayer);
        if (controller == null) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.lumungus_storage.portable_interface.unlinked",
                    tier.label()
            ));
            return InteractionResult.SUCCESS;
        }

        StorageControllerBlockEntity linkedController = controller;
        serverPlayer.openMenu(new PortableMenuProvider(linkedController));
        serverPlayer.sendSystemMessage(Component.translatable(
                "message.lumungus_storage.portable_interface.connected",
                tier.label(),
                linkedController.getNetworkLabel()
        ));
        return InteractionResult.SUCCESS;
    }

    private StorageControllerBlockEntity boundController(BoundStorageController bound, ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return null;
        }

        for (ResourceKey<Level> levelKey : server.levelKeys()) {
            if (!levelKey.identifier().equals(bound.dimension())) {
                continue;
            }
            ServerLevel targetLevel = server.getLevel(levelKey);
            if (targetLevel == null) {
                return null;
            }
            boolean sameDimension = player.level().dimension().identifier().equals(bound.dimension());
            if (!tier.canReach(sameDimension, player.blockPosition(), bound.pos())) {
                return null;
            }
            if (!targetLevel.isLoaded(bound.pos())) {
                targetLevel.getChunkAt(bound.pos());
            }
            if (!(targetLevel.getBlockEntity(bound.pos()) instanceof StorageControllerBlockEntity controller)
                    || !controller.getNetworkId().equals(bound.networkId())) {
                return null;
            }
            return controller;
        }
        return null;
    }

    private static Component describePosition(BlockPos pos) {
        return Component.literal(pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
    }

    private static final class PortableMenuProvider implements ExtendedMenuProvider<BlockPos> {
        private final StorageControllerBlockEntity controller;

        private PortableMenuProvider(StorageControllerBlockEntity controller) {
            this.controller = controller;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("container.lumungus_storage.crafting_terminal");
        }

        @Override
        public BlockPos getScreenOpeningData(ServerPlayer player) {
            return controller.getBlockPos();
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return new LumungusCraftingMenu(
                    containerId,
                    inventory,
                    ContainerLevelAccess.create(controller.getLevel(), controller.getBlockPos()),
                    controller.getBlockPos()
            );
        }
    }
}
