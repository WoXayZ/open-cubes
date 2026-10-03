package dev.opencubes.content.scaffolding;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Copy of {@link net.minecraft.world.item.ScaffoldingBlockItem} that walks the placement out along
 * an existing run of {@link TemporaryScaffoldingBlock} rather than vanilla scaffolding.
 */
public class TemporaryScaffoldingBlockItem extends BlockItem {

    public TemporaryScaffoldingBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        if (!(getBlock() instanceof TemporaryScaffoldingBlock scaffolding)) {
            return context;
        }

        if (!state.is(scaffolding)) {
            return scaffolding.distanceTo(level, pos) == ScaffoldingBlock.STABILITY_MAX_DISTANCE ? null : context;
        }

        Direction direction;
        if (context.isSecondaryUseActive()) {
            direction = context.isInside() ? context.getClickedFace().getOpposite() : context.getClickedFace();
        } else {
            direction = context.getClickedFace() == Direction.UP ? context.getHorizontalDirection() : Direction.UP;
        }

        int travelled = 0;
        BlockPos.MutableBlockPos cursor = pos.mutable().move(direction);
        while (travelled < ScaffoldingBlock.STABILITY_MAX_DISTANCE) {
            if (!level.isClientSide && !level.isInWorldBounds(cursor)) {
                int ceiling = level.getMaxBuildHeight();
                if (context.getPlayer() instanceof ServerPlayer serverPlayer && cursor.getY() >= ceiling) {
                    serverPlayer.sendSystemMessage(
                            Component.translatable("build.tooHigh", ceiling - 1).withStyle(ChatFormatting.RED), true);
                }
                break;
            }
            state = level.getBlockState(cursor);
            if (!state.is(scaffolding)) {
                if (state.canBeReplaced(context)) {
                    return BlockPlaceContext.at(context, cursor, direction);
                }
                break;
            }
            cursor.move(direction);
            if (direction.getAxis().isHorizontal()) {
                travelled++;
            }
        }
        return null;
    }

    @Override
    protected boolean mustSurvive() {
        return false;
    }
}
