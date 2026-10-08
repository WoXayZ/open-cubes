package dev.opencubes.content.automation;

import dev.opencubes.util.PlayerFeedback;

import com.mojang.serialization.MapCodec;
import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class VacuumHopperBlock extends BaseEntityBlock {

    public static final MapCodec<VacuumHopperBlock> CODEC = simpleCodec(VacuumHopperBlock::new);

    /** One face-mode property per side, driven by {@link VacuumHopperBlockEntity} output masks. */
    public static final EnumProperty<FaceMode> NORTH = EnumProperty.create("north", FaceMode.class);
    public static final EnumProperty<FaceMode> SOUTH = EnumProperty.create("south", FaceMode.class);
    public static final EnumProperty<FaceMode> EAST = EnumProperty.create("east", FaceMode.class);
    public static final EnumProperty<FaceMode> WEST = EnumProperty.create("west", FaceMode.class);
    public static final EnumProperty<FaceMode> UP = EnumProperty.create("up", FaceMode.class);
    public static final EnumProperty<FaceMode> DOWN = EnumProperty.create("down", FaceMode.class);

    private static final VoxelShape BODY = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
    private static final VoxelShape PORT_NORTH = Block.box(5.5, 5.5, 0.0, 10.5, 10.5, 4.0);
    private static final VoxelShape PORT_SOUTH = Block.box(5.5, 5.5, 12.0, 10.5, 10.5, 16.0);
    private static final VoxelShape PORT_WEST = Block.box(0.0, 5.5, 5.5, 4.0, 10.5, 10.5);
    private static final VoxelShape PORT_EAST = Block.box(12.0, 5.5, 5.5, 16.0, 10.5, 10.5);
    private static final VoxelShape PORT_DOWN = Block.box(5.5, 0.0, 5.5, 10.5, 4.0, 10.5);
    private static final VoxelShape PORT_UP = Block.box(5.5, 12.0, 5.5, 10.5, 16.0, 10.5);

    public VacuumHopperBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(NORTH, FaceMode.NONE)
                .setValue(SOUTH, FaceMode.NONE)
                .setValue(EAST, FaceMode.NONE)
                .setValue(WEST, FaceMode.NONE)
                .setValue(UP, FaceMode.NONE)
                .setValue(DOWN, FaceMode.NONE));
    }

    @Override
    protected MapCodec<? extends VacuumHopperBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }

    /** Maps a world direction to the blockstate property that renders that face's overlay. */
    public static EnumProperty<FaceMode> propertyForSide(Direction side) {
        return switch (side) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = BODY;
        if (state.getValue(NORTH) != FaceMode.NONE) {
            shape = Shapes.or(shape, PORT_NORTH);
        }
        if (state.getValue(SOUTH) != FaceMode.NONE) {
            shape = Shapes.or(shape, PORT_SOUTH);
        }
        if (state.getValue(EAST) != FaceMode.NONE) {
            shape = Shapes.or(shape, PORT_EAST);
        }
        if (state.getValue(WEST) != FaceMode.NONE) {
            shape = Shapes.or(shape, PORT_WEST);
        }
        if (state.getValue(UP) != FaceMode.NONE) {
            shape = Shapes.or(shape, PORT_UP);
        }
        if (state.getValue(DOWN) != FaceMode.NONE) {
            shape = Shapes.or(shape, PORT_DOWN);
        }
        return shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof VacuumHopperBlockEntity vacuum) {
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                vacuum.toggleVacuum();
                boolean on = !vacuum.isVacuumDisabled();
                PlayerFeedback.tell(player, net.minecraft.network.chat.Component.translatable(
                        on ? "opencubes.misc.vacuum_on" : "opencubes.misc.vacuum_off"), true);
                level.playSound(null, pos,
                        on ? net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value()
                                : net.minecraft.sounds.SoundEvents.NOTE_BLOCK_BASS.value(),
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.6F, on ? 1.4F : 0.8F);
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(vacuum, buf -> buf.writeBlockPos(pos));
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (level.getBlockEntity(pos) instanceof VacuumHopperBlockEntity vacuum) {
            vacuum.onEntityCollided(entity);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (level.getBlockEntity(pos) instanceof VacuumHopperBlockEntity vacuum) {
            if (level instanceof ServerLevel) {
                for (int i = 0; i < vacuum.getItems().getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                            vacuum.getItems().getStackInSlot(i));
                }
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VacuumHopperBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, OCBlockEntities.VACUUM_HOPPER.get(), VacuumHopperBlockEntity::tick);
    }
}
