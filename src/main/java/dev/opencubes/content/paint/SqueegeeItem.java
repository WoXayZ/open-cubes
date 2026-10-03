package dev.opencubes.content.paint;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SqueegeeItem extends Item {

    public SqueegeeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CanvasBlockEntity canvas)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        boolean all = player != null && player.isShiftKeyDown();
        if (!level.isClientSide) {
            Optional<ItemStack> dropped = canvas.squeegee(context.getClickedFace(), all);
            dropped.ifPresent(stack -> {
                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
                level.addFreshEntity(entity);
            });
            if (canvas.allFacesEmpty() && !canvas.getPaintedBlock().isAir()) {
                BlockState original = canvas.getPaintedBlock();
                level.setBlock(pos, original, 3);
            } else if (canvas.allFacesEmpty() && canvas.getPaintedBlock().isAir()) {
                // Crafted blank canvas stays; nothing to restore.
            }
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.5F, 1.2F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
