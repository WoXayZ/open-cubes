package dev.opencubes.registry;

import com.mojang.serialization.Codec;
import dev.opencubes.OCConstants;
import dev.opencubes.content.goldeneye.GoldenEyeTarget;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class OCDataComponents {

    private static final DeferredRegister.DataComponents REGISTRY =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, OCConstants.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> TANK_FLUID =
            register("tank_fluid", builder -> builder
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> TROPHY_ID =
            register("trophy_id", builder -> builder
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SLIMALYZER_ACTIVE =
            register("slimalyzer_active", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Immutable single-slot bag; raw {@code ItemStack} is not a valid data-component type. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> DEV_NULL_CONTENTS =
            register("dev_null_contents", builder -> builder
                    .persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SLEEPING_BAG_SLOT =
            register("sleeping_bag_slot", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SLEEPING_BAG_ASLEEP =
            register("sleeping_bag_asleep", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Hang glider deployed; lives on the item so clients (and other players) see the wing render. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> GLIDER_ENGAGED =
            register("glider_engaged", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GoldenEyeTarget>> GOLDEN_EYE_TARGET =
            register("golden_eye_target", builder -> builder
                    .persistent(GoldenEyeTarget.CODEC)
                    .networkSynchronized(GoldenEyeTarget.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LUGGAGE_SPECIAL =
            register("luggage_special", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PAINT_COLOR =
            register("paint_color", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PAINT_AMOUNT =
            register("paint_amount", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> STENCIL_PATTERN =
            register("stencil_pattern", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> GLYPH_CHAR =
            register("glyph_char", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MAP_SCALE =
            register("map_scale", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> HEIGHT_MAP_ID =
            register("height_map_id", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> IMAGINARY_USES =
            register("imaginary_uses", builder -> builder
                    .persistent(Codec.FLOAT)
                    .networkSynchronized(ByteBufCodecs.FLOAT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> IMAGINARY_MODE =
            register("imaginary_mode", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> POINTER_POS =
            register("pointer_pos", builder -> builder
                    .persistent(Codec.LONG)
                    .networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> POINTER_DIM =
            register("pointer_dim", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> EPIC_LORE =
            register("epic_lore", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<dev.opencubes.content.cursor.CursorTarget>> CURSOR_TARGET =
            register("cursor_target", builder -> builder
                    .persistent(dev.opencubes.content.cursor.CursorTarget.CODEC)
                    .networkSynchronized(dev.opencubes.content.cursor.CursorTarget.STREAM_CODEC));

    private OCDataComponents() {}

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> operator) {
        return REGISTRY.registerComponentType(name, operator);
    }

    public static void register(IEventBus modBus) {
        REGISTRY.register(modBus);
    }
}
