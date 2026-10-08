package dev.opencubes.content.paint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/** Per-face canvas paint state: solid background, stencil layers, optional unpainted cover. */
public final class CanvasFaceData {

    public static final Codec<CanvasFaceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("background", 0).forGetter(CanvasFaceData::background),
            Layer.CODEC.listOf().optionalFieldOf("layers", List.of()).forGetter(CanvasFaceData::layers),
            Cover.CODEC.optionalFieldOf("cover").forGetter(f -> Optional.ofNullable(f.cover))
    ).apply(instance, CanvasFaceData::fromCodec));

    private int background;
    private final List<Layer> layers = new ArrayList<>();
    private Cover cover;

    public record Layer(StencilPattern pattern, int rotation, int color) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(StencilPattern::byId, StencilPattern::id).fieldOf("pattern").forGetter(Layer::pattern),
                Codec.INT.optionalFieldOf("rotation", 0).forGetter(Layer::rotation),
                Codec.INT.fieldOf("color").forGetter(Layer::color)
        ).apply(instance, Layer::new));
    }

    public record Cover(StencilPattern pattern, int rotation) {
        public static final Codec<Cover> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.xmap(StencilPattern::byId, StencilPattern::id).fieldOf("pattern").forGetter(Cover::pattern),
                Codec.INT.optionalFieldOf("rotation", 0).forGetter(Cover::rotation)
        ).apply(instance, Cover::new));
    }

    private static CanvasFaceData fromCodec(int background, List<Layer> layers, Optional<Cover> cover) {
        CanvasFaceData data = new CanvasFaceData();
        data.background = background;
        data.layers.addAll(layers);
        data.cover = cover.orElse(null);
        return data;
    }

    public int background() {
        return background;
    }

    public List<Layer> layers() {
        return List.copyOf(layers);
    }

    public Cover cover() {
        return cover;
    }

    public boolean isEmpty() {
        return background == 0 && layers.isEmpty() && cover == null;
    }

    public void applyPaint(int argbColor) {
        if (cover != null) {
            if (!layers.isEmpty()) {
                Layer top = layers.get(layers.size() - 1);
                if (top.pattern() == cover.pattern() && top.rotation() == cover.rotation()) {
                    layers.set(layers.size() - 1, new Layer(top.pattern(), top.rotation(), argbColor));
                    return;
                }
            }
            layers.add(new Layer(cover.pattern(), cover.rotation(), argbColor));
        } else {
            layers.clear();
            background = argbColor;
        }
    }

    public boolean putCover(StencilPattern pattern, int rotation) {
        if (cover != null) {
            return false;
        }
        cover = new Cover(pattern, rotation & 3);
        return true;
    }

    public Optional<StencilPattern> popCover() {
        if (cover == null) {
            return Optional.empty();
        }
        StencilPattern pattern = cover.pattern();
        cover = null;
        return Optional.of(pattern);
    }

    public void rotateCover() {
        if (cover != null) {
            cover = new Cover(cover.pattern(), (cover.rotation() + 1) & 3);
        }
    }

    public Optional<StencilPattern> clearAll() {
        Optional<StencilPattern> dropped = popCover();
        layers.clear();
        background = 0;
        return dropped;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Background", background);
        ListTag list = new ListTag();
        for (Layer layer : layers) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Pattern", layer.pattern().id());
            entry.putInt("Rotation", layer.rotation());
            entry.putInt("Color", layer.color());
            list.add(entry);
        }
        tag.put("Layers", list);
        if (cover != null) {
            CompoundTag coverTag = new CompoundTag();
            coverTag.putString("Pattern", cover.pattern().id());
            coverTag.putInt("Rotation", cover.rotation());
            tag.put("Cover", coverTag);
        }
        return tag;
    }

    public static CanvasFaceData load(CompoundTag tag) {
        CanvasFaceData data = new CanvasFaceData();
        data.background = tag.getIntOr("Background", 0);
        ListTag list = tag.getListOrEmpty("Layers");
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            data.layers.add(new Layer(
                    StencilPattern.byId(entry.getStringOr("Pattern", "")),
                    entry.getIntOr("Rotation", 0),
                    entry.getIntOr("Color", 0)));
        }
        if (tag.contains("Cover")) {
            CompoundTag coverTag = tag.getCompoundOrEmpty("Cover");
            data.cover = new Cover(
                    StencilPattern.byId(coverTag.getStringOr("Pattern", "")),
                    coverTag.getIntOr("Rotation", 0));
        }
        return data;
    }

    public CanvasFaceData copy() {
        return load(save());
    }
}
