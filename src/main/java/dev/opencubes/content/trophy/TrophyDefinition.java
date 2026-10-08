package dev.opencubes.content.trophy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.OCConstants;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * One trophy type, loaded from {@code data/<ns>/opencubes/trophy/<id>.json}.
 */
public record TrophyDefinition(
        Identifier entity,
        float scale,
        float verticalOffset,
        Identifier behavior,
        Optional<DropSpec> drop
) {

    public static final Codec<TrophyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("entity").forGetter(TrophyDefinition::entity),
            Codec.FLOAT.optionalFieldOf("scale", 0.4F).forGetter(TrophyDefinition::scale),
            Codec.FLOAT.optionalFieldOf("vertical_offset", 0.0F).forGetter(TrophyDefinition::verticalOffset),
            Identifier.CODEC.optionalFieldOf("behavior", OCConstants.id("none"))
                    .forGetter(TrophyDefinition::behavior),
            DropSpec.CODEC.optionalFieldOf("drop").forGetter(TrophyDefinition::drop)
    ).apply(instance, TrophyDefinition::new));

    public record DropSpec(
            Identifier item,
            int count,
            int cooldown,
            Optional<Identifier> sound
    ) {
        public static final Codec<DropSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("item").forGetter(DropSpec::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(DropSpec::count),
                Codec.INT.optionalFieldOf("cooldown", 20000).forGetter(DropSpec::cooldown),
                Identifier.CODEC.optionalFieldOf("sound").forGetter(DropSpec::sound)
        ).apply(instance, DropSpec::new));
    }
}
