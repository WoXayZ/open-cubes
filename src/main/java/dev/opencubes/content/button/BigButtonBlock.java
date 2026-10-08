package dev.opencubes.content.button;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A button that fills the whole face it is attached to, and whose press duration is set by
 * the items stored inside it.
 *
 * <p>Almost everything else is vanilla's. {@link ButtonBlock} already carries the attachment
 * states, the placement rules, the redstone output and - through {@link BlockSetType} - both
 * the click sounds and the rule that wooden buttons answer to arrows while stone ones do not.
 * That rule was the only behavioural difference between upstream's two big buttons, so
 * inheriting it is what lets one class cover all thirteen materials.
 */
public class BigButtonBlock extends ButtonBlock implements EntityBlock {

    // Typed as the supertype because ButtonBlock#codec is invariant, not because the codec
    // produces one: decoding always yields a BigButtonBlock.
    public static final MapCodec<ButtonBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockSetType.CODEC.fieldOf("block_set_type").forGetter(block -> ((BigButtonBlock) block).setType),
            propertiesCodec()
    ).apply(instance, (setType, properties) -> new BigButtonBlock(setType, properties)));

    /** Only used until the block entity is consulted; the real duration comes from the items. */
    private static final int FALLBACK_TICKS = 20;

    private static final double THICKNESS = 2.0D;
    private static final double PRESSED_THICKNESS = 1.0D;
    private static final double MARGIN = 1.0D;

    private final BlockSetType setType;

    public BigButtonBlock(BlockSetType setType, Properties properties) {
        super(setType, FALLBACK_TICKS, properties);
        this.setType = setType;
    }

    @Override
    public MapCodec<ButtonBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigButtonBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        double depth = state.getValue(POWERED) ? PRESSED_THICKNESS : THICKNESS;
        double near = MARGIN;
        double far = 16.0D - MARGIN;

        return switch (state.getValue(FACE)) {
            case FLOOR -> Block.box(near, 0.0D, near, far, depth, far);
            case CEILING -> Block.box(near, 16.0D - depth, near, far, 16.0D, far);
            case WALL -> switch (state.getValue(FACING)) {
                case NORTH -> Block.box(near, near, 16.0D - depth, far, far, 16.0D);
                case SOUTH -> Block.box(near, near, 0.0D, far, far, depth);
                case WEST -> Block.box(16.0D - depth, near, near, 16.0D, far, far);
                default -> Block.box(0.0D, near, near, depth, far, far);
            };
        };
    }

    /** Sneaking with an empty hand opens the timer inventory instead of pressing. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isSecondaryUseActive()) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BigButtonBlockEntity button) {
                player.openMenu(button);
            }
            return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public void press(BlockState state, Level level, BlockPos pos, @Nullable Player player) {
        level.setBlock(pos, state.setValue(POWERED, true), Block.UPDATE_ALL);
        updateNeighbours(state, level, pos);
        level.scheduleTick(pos, this, pressDuration(level, pos));
        playSound(player, level, pos, true);
        level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos,
                        RandomSource random) {
        if (state.getValue(POWERED)) {
            checkPressed(state, level, pos);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!level.isClientSide() && setType.canButtonBeActivatedByArrows() && !state.getValue(POWERED)) {
            checkPressed(state, level, pos);
        }
    }

    /**
     * Overridden only to feed it the configurable press duration; vanilla's version reads a
     * private field. The behaviour is unchanged: a button that answers to arrows stays down
     * for as long as one is stuck in it.
     */
    @Override
    protected void checkPressed(BlockState state, Level level, BlockPos pos) {
        boolean arrowPresent = setType.canButtonBeActivatedByArrows()
                && !level.getEntitiesOfClass(AbstractArrow.class,
                        getShape(state, level, pos, CollisionContext.empty()).bounds().move(pos)).isEmpty();

        if (arrowPresent != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, arrowPresent), Block.UPDATE_ALL);
            updateNeighbours(state, level, pos);
            playSound(null, level, pos, arrowPresent);
            level.gameEvent(null, arrowPresent ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos);
        }

        if (arrowPresent) {
            level.scheduleTick(pos, this, pressDuration(level, pos));
        }
    }

    private void updateNeighbours(BlockState state, Level level, BlockPos pos) {
        Direction support = getConnectedDirection(state).getOpposite();
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.relative(support), this);
    }

    private int pressDuration(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BigButtonBlockEntity button
                ? button.pressDuration()
                : dev.opencubes.config.OCCommonConfig.BIG_BUTTON_EMPTY_DURATION.get();
    }

    /** Exposed so data generation can pick the right rotation per attachment face. */
    public static AttachFace faceOf(BlockState state) {
        return state.getValue(FACE);
    }
}
