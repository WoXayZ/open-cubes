package dev.opencubes.content.cursor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record CursorTarget(Identifier dimension, BlockPos pos, Direction side) {

    public static final Codec<CursorTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("dimension").forGetter(CursorTarget::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(CursorTarget::pos),
            Direction.CODEC.fieldOf("side").forGetter(CursorTarget::side)
    ).apply(instance, CursorTarget::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, Direction> DIRECTION_STREAM =
            ByteBufCodecs.VAR_INT.<RegistryFriendlyByteBuf>cast()
                    .map(Direction::from3DDataValue, Direction::get3DDataValue);

    public static final StreamCodec<RegistryFriendlyByteBuf, CursorTarget> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, CursorTarget::dimension,
            BlockPos.STREAM_CODEC, CursorTarget::pos,
            DIRECTION_STREAM, CursorTarget::side,
            CursorTarget::new);
}
