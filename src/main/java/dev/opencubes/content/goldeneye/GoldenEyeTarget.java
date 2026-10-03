package dev.opencubes.content.goldeneye;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record GoldenEyeTarget(ResourceLocation structure, BlockPos pos) {

    public static final Codec<GoldenEyeTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(GoldenEyeTarget::structure),
            BlockPos.CODEC.fieldOf("pos").forGetter(GoldenEyeTarget::pos)
    ).apply(instance, GoldenEyeTarget::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GoldenEyeTarget> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, GoldenEyeTarget::structure,
            BlockPos.STREAM_CODEC, GoldenEyeTarget::pos,
            GoldenEyeTarget::new);
}
