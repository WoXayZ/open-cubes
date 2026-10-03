package dev.opencubes.content.automation;

import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockPlacerMenu extends SimpleMachineMenu {

    public BlockPlacerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, getPlacer(playerInventory, buffer.readBlockPos()));
    }

    public BlockPlacerMenu(int containerId, Inventory playerInventory, BlockPlacerBlockEntity placer) {
        super(OCMenus.BLOCK_PLACER.get(), containerId, playerInventory, placer, placer.getItems(),
                () -> OCBlocks.BLOCK_PLACER.get());
    }

    private static BlockPlacerBlockEntity getPlacer(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof BlockPlacerBlockEntity placer) {
            return placer;
        }
        throw new IllegalStateException("Block placer missing at " + pos);
    }
}
