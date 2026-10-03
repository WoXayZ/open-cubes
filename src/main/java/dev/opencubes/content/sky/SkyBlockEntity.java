package dev.opencubes.content.sky;

import dev.opencubes.registry.OCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marker block entity purely so an active Sky Block (rendered {@link net.minecraft.world.level.block.RenderShape#INVISIBLE})
 * still gets a {@link net.minecraft.client.renderer.blockentity.BlockEntityRenderer} pass to draw the
 * sky-tinted faces. Holds no data.
 */
public class SkyBlockEntity extends BlockEntity {

    public SkyBlockEntity(BlockPos pos, BlockState state) {
        super(OCBlockEntities.SKY_BLOCK.get(), pos, state);
    }
}
