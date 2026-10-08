package dev.opencubes.client;

import dev.opencubes.content.paint.CanvasFaceData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class CanvasRenderState extends BlockEntityRenderState {
    public boolean glass;
    public final List<Face> faces = new ArrayList<>();

    public static final class Face {
        public Direction direction = Direction.UP;
        public int light;
        public int background;
        public List<CanvasFaceData.Layer> layers = List.of();
        public CanvasFaceData.Cover cover;
    }
}
