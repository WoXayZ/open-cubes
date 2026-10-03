package dev.opencubes.content.automation;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.util.OCFakePlayers;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Breaks the faced block on a redstone pulse and tries to push drops into the inventory on the
 * back face. Anything that does not fit is left as item entities at the broken block.
 */
public class BlockBreakerBlockEntity extends BlockManipulatorBlockEntity {

    public BlockBreakerBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.BLOCK_BREAKER.get(), pos, state);
    }

    @Override
    protected int getActionLimit() {
        return OCCommonConfig.BLOCK_BREAKER_ACTION_LIMIT.get();
    }

    @Override
    protected boolean canWork(BlockState targetState, BlockPos target, Direction facing) {
        return !targetState.isAir()
                && !targetState.is(Blocks.BEDROCK)
                && targetState.getDestroySpeed(level, target) >= 0.0F;
    }

    @Override
    protected void doWork(BlockState targetState, BlockPos target, Direction facing) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        List<ItemStack> drops = OCFakePlayers.breakBlock(serverLevel, target);
        if (drops.isEmpty()) {
            return;
        }

        Direction back = facing.getOpposite();
        IItemHandler inventory = level.getCapability(
                Capabilities.ItemHandler.BLOCK, worldPosition.relative(back), facing);

        for (ItemStack drop : drops) {
            ItemStack leftover = inventory == null ? drop
                    : ItemHandlerHelper.insertItem(inventory, drop, false);
            if (!leftover.isEmpty()) {
                ItemEntity entity = new ItemEntity(level,
                        target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, leftover);
                level.addFreshEntity(entity);
            }
        }
    }
}
