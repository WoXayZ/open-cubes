package dev.opencubes.content.heightmap;

import dev.opencubes.registry.OCEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CartographerItem extends Item {

    public CartographerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && level instanceof ServerLevel) {
            Vec3 look = player.getLookAngle();
            Vec3 spawn = player.position().add(look.x * 1.5D, player.getEyeHeight(), look.z * 1.5D);
            CartographerEntity entity = OCEntities.CARTOGRAPHER.get().create(level);
            if (entity != null) {
                entity.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot(), 0.0F);
                entity.setOwner(player);
                entity.loadFromItem(stack);
                level.addFreshEntity(entity);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
