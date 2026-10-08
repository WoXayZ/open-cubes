package dev.opencubes.content.paint;

import dev.opencubes.registry.OCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * An 8×8 pixel letter stuck on a wall, like the OpenBlocks glyph. The pixel offsets locate its
 * centre on the face of the block behind it, measured from the top left corner.
 */
public class GlyphEntity extends HangingEntity {

    public static final int SIZE = 8;
    public static final int HALF = SIZE / 2;
    public static final double DEPTH = 0.5D / 16.0D;

    private static final EntityDataAccessor<String> DATA_CHAR =
            SynchedEntityData.defineId(GlyphEntity.class, EntityDataSerializers.STRING);

    private int offsetX;
    private int offsetY;

    public GlyphEntity(EntityType<? extends GlyphEntity> type, Level level) {
        super(type, level);
    }

    public GlyphEntity(Level level, BlockPos pos, Direction facing, int offsetX, int offsetY, char character) {
        super(OCEntities.GLYPH.get(), level, pos);
        this.offsetX = clampOffset(offsetX);
        this.offsetY = clampOffset(offsetY);
        setCharacter(character);
        setDirection(facing);
    }

    private static int clampOffset(int value) {
        return Mth.clamp(value, HALF, 16 - HALF);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHAR, String.valueOf(GlyphItem.CHARACTERS.charAt(0)));
    }

    public void setCharacter(char character) {
        entityData.set(DATA_CHAR, String.valueOf(GlyphItem.sanitize(character)));
    }

    public char getCharacter() {
        String value = entityData.get(DATA_CHAR);
        return value.isEmpty() ? GlyphItem.CHARACTERS.charAt(0) : GlyphItem.sanitize(value.charAt(0));
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        Direction right = direction.getCounterClockWise();
        double along = (offsetX - 8) / 16.0D;
        double up = (8 - offsetY) / 16.0D;
        Vec3 centre = Vec3.atCenterOf(pos)
                .relative(direction, DEPTH / 2.0D - 0.5D)
                .relative(right, along)
                .add(0.0D, up, 0.0D);
        double half = SIZE / 32.0D;
        double width = right.getAxis() == Direction.Axis.X ? half * 2 : DEPTH;
        double depth = right.getAxis() == Direction.Axis.Z ? half * 2 : DEPTH;
        return AABB.ofSize(centre, width, half * 2, depth);
    }

    @Override
    public void playPlacementSound() {
        playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.4F);
    }

    @Override
    public void dropItem(ServerLevel level, @Nullable Entity breaker) {
        playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.4F);
        if (!level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            return;
        }
        if (breaker instanceof Player player && player.hasInfiniteMaterials()) {
            return;
        }
        spawnAtLocation(level, GlyphItem.create(getCharacter()));
    }

    @Override
    public ItemStack getPickResult() {
        return GlyphItem.create(getCharacter());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        int data = getDirection().get3DDataValue() | offsetX << 4 | offsetY << 9;
        return new ClientboundAddEntityPacket(this, data, getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        int data = packet.getData();
        offsetX = clampOffset(data >> 4 & 31);
        offsetY = clampOffset(data >> 9 & 31);
        setDirection(Direction.from3DDataValue(data & 15));
    }

    @Override
    public void addAdditionalSaveData(ValueOutput tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Facing", (byte) getDirection().get2DDataValue());
        tag.putByte("OffsetX", (byte) offsetX);
        tag.putByte("OffsetY", (byte) offsetY);
        tag.putString("Char", String.valueOf(getCharacter()));
    }

    @Override
    public void readAdditionalSaveData(ValueInput tag) {
        super.readAdditionalSaveData(tag);
        offsetX = clampOffset(tag.getByteOr("OffsetX", (byte) 0));
        offsetY = clampOffset(tag.getByteOr("OffsetY", (byte) 0));
        String value = tag.getStringOr("Char", "");
        if (!value.isEmpty()) {
            setCharacter(value.charAt(0));
        }
        setDirection(Direction.from2DDataValue(tag.getByteOr("Facing", (byte) 0)));
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }
}
