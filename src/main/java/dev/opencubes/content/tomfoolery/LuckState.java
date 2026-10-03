package dev.opencubes.content.tomfoolery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class LuckState {

    public static final Codec<LuckState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("luck").forGetter(s -> s.luck),
            Codec.INT.fieldOf("cooldown").forGetter(s -> s.cooldown),
            Codec.BOOL.fieldOf("forceNext").forGetter(s -> s.forceNext)
    ).apply(instance, LuckState::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LuckState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, s -> s.luck,
            ByteBufCodecs.VAR_INT, s -> s.cooldown,
            ByteBufCodecs.BOOL, s -> s.forceNext,
            LuckState::new);

    public int luck;
    public int cooldown;
    public boolean forceNext;

    public LuckState() {
        this(0, 0, false);
    }

    public LuckState(int luck, int cooldown, boolean forceNext) {
        this.luck = luck;
        this.cooldown = cooldown;
        this.forceNext = forceNext;
    }
}
