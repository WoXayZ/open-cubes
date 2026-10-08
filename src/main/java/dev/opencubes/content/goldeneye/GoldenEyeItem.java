package dev.opencubes.content.goldeneye;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.function.Consumer;

public class GoldenEyeItem extends Item {

    private static final List<net.minecraft.tags.TagKey<Structure>> SEARCH_TAGS = List.of(
            StructureTags.VILLAGE,
            StructureTags.MINESHAFT,
            StructureTags.RUINED_PORTAL,
            StructureTags.SHIPWRECK,
            StructureTags.OCEAN_RUIN,
            StructureTags.ON_TREASURE_MAPS,
            StructureTags.ON_WOODLAND_EXPLORER_MAPS,
            StructureTags.ON_OCEAN_EXPLORER_MAPS
    );

    public GoldenEyeItem(Properties properties) {
        // Default matches config; runtime max damage comes from getMaxDamage.
        super(properties.stacksTo(1).durability(100));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return OCCommonConfig.SPEC.isLoaded() ? OCCommonConfig.GOLDEN_EYE_MAX_DAMAGE.get() : 100;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            tryLearn(stack, serverLevel, serverPlayer);
        } else if (tryThrow(stack, serverLevel, serverPlayer)) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static void tryLearn(ItemStack stack, ServerLevel level, ServerPlayer player) {
        GoldenEyeTarget best = null;
        double bestDist = Double.MAX_VALUE;
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (var tag : SEARCH_TAGS) {
            HolderSet.Named<Structure> set = structures.get(tag).orElse(null);
            if (set == null) {
                continue;
            }
            var found = level.getChunkSource().getGenerator()
                    .findNearestMapStructure(level, set, player.blockPosition(),
                            OCCommonConfig.GOLDEN_EYE_SEARCH_RADIUS.get(), false);
            if (found == null) {
                continue;
            }
            BlockPos pos = found.getFirst();
            Holder<Structure> holder = found.getSecond();
            double dist = player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
            if (dist < bestDist) {
                bestDist = dist;
                best = new GoldenEyeTarget(holder.unwrapKey().orElseThrow().identifier(), pos);
            }
        }

        if (best == null) {
            player.sendSystemMessage(Component.translatable("opencubes.misc.no_nearby_structures"));
            return;
        }
        stack.set(OCDataComponents.GOLDEN_EYE_TARGET.get(), best);
        player.sendSystemMessage(Component.translatable("opencubes.misc.locked_on_nearest_structure",
                    Component.translatable(net.minecraft.util.Util.makeDescriptionId("structure", best.structure()))));
    }

    private static boolean tryThrow(ItemStack stack, ServerLevel level, ServerPlayer player) {
        GoldenEyeTarget target = stack.get(OCDataComponents.GOLDEN_EYE_TARGET.get());
        if (target == null) {
            player.sendSystemMessage(Component.translatable("opencubes.misc.no_nearby_structures"));
            return false;
        }
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            return false;
        }

        ItemStack thrown = stack.copy();
        thrown.setDamageValue(thrown.getDamageValue() + 1);
        GoldenEyeEntity eye = OCEntities.GOLDEN_EYE.get().create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
        if (eye == null) {
            return false;
        }
        eye.setPos(player.getX(), player.getY(0.5D), player.getZ());
        eye.setOwner(player);
        eye.setStack(thrown);
        eye.setTarget(target.pos());
        level.addFreshEntity(eye);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        GoldenEyeTarget target = stack.get(OCDataComponents.GOLDEN_EYE_TARGET.get());
        if (target != null) {
            tooltip.accept(Component.translatable("opencubes.misc.locked_on_nearest_structure",
                    Component.translatable(net.minecraft.util.Util.makeDescriptionId("structure", target.structure()))));
        }
    }

}
