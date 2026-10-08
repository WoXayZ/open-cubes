package dev.opencubes.client.heightmap;

import dev.opencubes.content.heightmap.HeightMapData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

public class HeightMapProjectorRenderState extends BlockEntityRenderState {
    public @Nullable HeightMapData map;
    public float rotation;
}
