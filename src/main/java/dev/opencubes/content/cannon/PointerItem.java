package dev.opencubes.content.cannon;

import dev.opencubes.util.PlayerFeedback;

import dev.opencubes.registry.OCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class PointerItem extends Item {

    public PointerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        BlockPos hitPos = hit.getBlockPos();
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                BlockEntity be = level.getBlockEntity(hitPos);
                if (be instanceof Pointable) {
                    stack.set(OCDataComponents.POINTER_POS.get(), hitPos.asLong());
                    stack.set(OCDataComponents.POINTER_DIM.get(), level.dimension().identifier().toString());
                    PlayerFeedback.tell(player, Component.translatable("opencubes.misc.pointer_selected",
                            hitPos.getX(), hitPos.getY(), hitPos.getZ()), true);
                }
            } else {
                Long linked = stack.get(OCDataComponents.POINTER_POS.get());
                String dim = stack.get(OCDataComponents.POINTER_DIM.get());
                if (linked != null && dim != null) {
                    ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dim));
                    if (level.dimension().equals(key)) {
                        BlockPos cannonPos = BlockPos.of(linked);
                        BlockEntity be = level.getBlockEntity(cannonPos);
                        if (be instanceof Pointable pointable) {
                            pointable.setTarget(level, hitPos, player);
                            PlayerFeedback.tell(player, Component.translatable("opencubes.misc.pointer_aimed",
                                    hitPos.getX(), hitPos.getY(), hitPos.getZ()), true);
                        }
                    }
                }
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }
}
