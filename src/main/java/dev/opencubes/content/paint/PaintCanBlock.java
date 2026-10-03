package dev.opencubes.content.paint;

import com.mojang.serialization.MapCodec;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.registry.OCBlockEntities;
import dev.opencubes.registry.OCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PaintCanBlock extends BaseEntityBlock {

    public static final MapCodec<PaintCanBlock> CODEC = simpleCodec(PaintCanBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 11.0D, 12.0D);
    public static final int FULL_AMOUNT = 30;

    public PaintCanBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends PaintCanBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(HorizontalDirectionalBlock.FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof PaintCanBlockEntity can) {
            Integer color = stack.get(OCDataComponents.PAINT_COLOR.get());
            Integer amount = stack.get(OCDataComponents.PAINT_AMOUNT.get());
            if (color != null) {
                can.setColor(color);
            }
            can.setAmount(amount == null ? FULL_AMOUNT : amount);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PaintCanBlockEntity can)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof PaintBrushItem) {
            if (can.getAmount() <= 0) {
                return ItemInteractionResult.FAIL;
            }
            stack.set(OCDataComponents.PAINT_COLOR.get(), can.getColor());
            stack.setDamageValue(0);
            usePaint(level, pos, player, can);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (GliderPaint.isPaintable(stack)) {
            if (can.getAmount() <= 0 || GliderPaint.colour(stack) == can.getColor()) {
                return ItemInteractionResult.FAIL;
            }
            stack.set(OCDataComponents.PAINT_COLOR.get(), can.getColor());
            usePaint(level, pos, player, can);
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 0.4F, 1.8F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static void usePaint(Level level, BlockPos pos, Player player, PaintCanBlockEntity can) {
        if (level.isClientSide) {
            return;
        }
        can.setAmount(can.getAmount() - 1);
        if (can.getAmount() <= 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            if (!player.getAbilities().instabuild) {
                player.getInventory().add(new ItemStack(Items.BUCKET));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PaintCanBlockEntity(pos, state);
    }
}
