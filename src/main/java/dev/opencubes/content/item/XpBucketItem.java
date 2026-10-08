package dev.opencubes.content.item;

import dev.opencubes.registry.OCFluids;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * A bucket of XP juice. Behaves like a vanilla bucket - it empties into tanks and any other
 * fluid handler, and places a liquid experience source block when used on the world.
 *
 * <p>The fluid transfer itself goes through the item fluid handler capability, which
 * {@code OCCapabilities} registers for this item; NeoForge only does that automatically for
 * plain {@link BucketItem} instances.
 */
public class XpBucketItem extends BucketItem {

    public XpBucketItem(Properties properties) {
        super(OCFluids.XP_JUICE.get(), properties.stacksTo(1).craftRemainder(Items.BUCKET));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        // Prefer the clicked face; fall back to a sideless lookup for handlers that ignore sides.
        if (pour(level, player, context, pos, context.getClickedFace()) || pour(level, player, context, pos, null)) {
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
        }
        return super.useOn(context);
    }

    private static boolean pour(Level level, Player player, UseOnContext context, BlockPos pos, @Nullable Direction face) {
        if (level.getCapability(Capabilities.Fluid.BLOCK, pos, face) == null) {
            return false;
        }
        return FluidUtil.interactWithFluidHandler(player, context.getHand(), level, pos, face);
    }
}
