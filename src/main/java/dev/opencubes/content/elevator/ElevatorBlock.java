package dev.opencubes.content.elevator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.registry.OCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ElevatorBlock extends Block implements Elevator {

    public static final MapCodec<ElevatorBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DyeColor.CODEC.fieldOf("colour").forGetter(block -> block.colour),
            propertiesCodec()
    ).apply(instance, ElevatorBlock::new));

    private final DyeColor colour;

    public ElevatorBlock(DyeColor colour, Properties properties) {
        super(properties);
        this.colour = colour;
    }

    @Override
    protected MapCodec<? extends ElevatorBlock> codec() {
        return CODEC;
    }

    @Override
    public DyeColor elevatorColour(BlockState state) {
        return colour;
    }

    /**
     * Recolouring in place, as the original allowed. Upstream stored the colour in a block
     * entity to do this; here each colour is its own block, so dyeing is a state swap that
     * carries the existing properties across.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hit) {
        if (!(stack.getItem() instanceof DyeItem dye)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        DyeColor target = dye.getDyeColor();
        if (target == colour) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Block recoloured = OCBlocks.recolour(this, target);
        if (recoloured == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            BlockState newState = copyProperties(state, recoloured.defaultBlockState());
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static BlockState copyProperties(BlockState from, BlockState to) {
        BlockState result = to;
        for (var property : from.getProperties()) {
            if (result.hasProperty(property)) {
                result = copyProperty(from, result, property);
            }
        }
        return result;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(
            BlockState from, BlockState to, net.minecraft.world.level.block.state.properties.Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
