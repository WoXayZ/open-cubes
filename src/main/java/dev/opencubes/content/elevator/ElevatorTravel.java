package dev.opencubes.content.elevator;

import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCSounds;
import dev.opencubes.registry.OCTags;
import dev.opencubes.util.ExperienceUtil;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The whole of elevator travel. Runs on the server: the client only reports that the player
 * jumped or crouched, and every rule is re-checked here.
 */
public final class ElevatorTravel {

    private ElevatorTravel() {}

    /** Where the destination elevator was found, and how the player should land on it. */
    private record Destination(BlockPos pos, @Nullable Direction facing) {}

    public static void travel(ServerPlayer player, Direction direction) {
        if (player.isPassenger() || player.isSpectator()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos origin = standingOn(player);

        BlockState originState = level.getBlockState(origin);
        if (!(originState.getBlock() instanceof Elevator elevator)) {
            return;
        }

        Destination destination = search(level, player, elevator.elevatorColour(originState), origin, direction);
        if (destination == null) {
            return;
        }

        int cost = experienceCost(player, destination.pos());
        if (ExperienceUtil.totalExperience(player) < cost) {
            return;
        }
        ExperienceUtil.consume(player, cost);

        double x = OCCommonConfig.ELEVATOR_CENTRE_ON_BLOCK.get() ? destination.pos().getX() + 0.5D : player.getX();
        double z = OCCommonConfig.ELEVATOR_CENTRE_ON_BLOCK.get() ? destination.pos().getZ() + 0.5D : player.getZ();
        double y = destination.pos().getY() + 1.0D;
        float yaw = destination.facing() != null ? destination.facing().toYRot() : player.getYRot();

        player.teleportTo(level, x, y, z, Set.of(), yaw, player.getXRot());
        player.resetFallDistance();
        level.playSound(null, destination.pos(), OCSounds.ELEVATOR_ACTIVATE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * The block the player is standing on. Their bounding box sits just above it, so the
     * floor of the box minus one is the block bearing their weight.
     */
    public static BlockPos standingOn(ServerPlayer player) {
        return new BlockPos(
                Mth.floor(player.getX()),
                Mth.floor(player.getBoundingBox().minY) - 1,
                Mth.floor(player.getZ()));
    }

    @Nullable
    private static Destination search(ServerLevel level, ServerPlayer player, DyeColor colour,
                                      BlockPos origin, Direction direction) {
        boolean matchColour = OCCommonConfig.ELEVATOR_MATCH_COLOUR.get();
        boolean ignoreBlocks = OCCommonConfig.ELEVATOR_IGNORE_BLOCKS.get();
        int maxBlocked = OCCommonConfig.ELEVATOR_MAX_BLOCKS_PASSED.get();
        int range = OCCommonConfig.ELEVATOR_TRAVEL_DISTANCE.get();

        int blocked = 0;
        BlockPos pos = origin;

        for (int step = 0; step < range; step++) {
            pos = pos.relative(direction);

            if (!level.isLoaded(pos) || level.isOutsideBuildHeight(pos)) {
                return null;
            }

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }

            if (state.getBlock() instanceof Elevator other
                    && (!matchColour || other.elevatorColour(state) == colour)
                    && hasHeadroom(level, player, pos.above())) {
                return new Destination(pos, other.arrivalFacing(state));
            }

            if (ignoreBlocks) {
                continue;
            }
            if (state.is(OCTags.Blocks.ELEVATOR_BLOCKING)) {
                return null;
            }
            if (state.is(OCTags.Blocks.ELEVATOR_TRANSPARENT)
                    && !state.is(OCTags.Blocks.ELEVATOR_INCREMENT)) {
                continue;
            }
            if (++blocked > maxBlocked) {
                return null;
            }
        }

        return null;
    }

    /** Whether the player fits standing on {@code floor}. */
    private static boolean hasHeadroom(Level level, ServerPlayer player, BlockPos floor) {
        int height = Math.max(1, Mth.ceil(player.getBbHeight()));
        for (int dy = 0; dy < height; dy++) {
            if (!isPassable(level, floor.above(dy))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPassable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        // Torches, carpets and snow layers should not stop a trip; a slab or a full block should.
        return shape.isEmpty() || shape.bounds().getYsize() < 0.7D;
    }

    private static int experienceCost(ServerPlayer player, BlockPos destination) {
        double perBlock = OCCommonConfig.ELEVATOR_XP_PER_BLOCK.get();
        if (perBlock <= 0.0D || player.getAbilities().instabuild) {
            return 0;
        }
        int distance = Mth.abs((int) (player.getY() - destination.getY()));
        return Mth.ceil(perBlock * distance);
    }
}
