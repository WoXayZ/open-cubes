package dev.opencubes.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.opencubes.content.flight.GliderPaint;
import dev.opencubes.registry.OCDataComponents;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Item tint sources referenced from {@code assets/opencubes/items}. Dye colours that never
 * change use {@code minecraft:constant} instead of a source.
 */
public final class OCItemTints {

    public static final MapCodec<Paint> PAINT = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("fallback", 0xFFFFFF).forGetter(Paint::fallback)
    ).apply(instance, Paint::new));
    public static final MapCodec<Glider> GLIDER = MapCodec.unit(Glider::new);

    private OCItemTints() {}

    public record Paint(int fallback) implements ItemTintSource {
        @Override
        public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
            Integer color = stack.get(OCDataComponents.PAINT_COLOR.get());
            return opaque(color == null ? fallback : color);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return PAINT;
        }
    }

    public record Glider() implements ItemTintSource {
        @Override
        public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity) {
            return opaque(GliderPaint.colour(stack));
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return GLIDER;
        }
    }

    private static int opaque(int rgb) {
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }
}
