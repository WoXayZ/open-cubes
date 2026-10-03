package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.tank.TankBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Draws the fluid inside a tank.
 *
 * <p>The block model is a cutout glass cube ({@code tank.png} + CTM). Fluid is inset just inside
 * the frame border on exposed sides. Faces shared with a neighbouring tank holding the same fluid
 * are omitted and the body extends flush to that edge, so adjacent tanks read as one volume.
 */
public class TankRenderer implements BlockEntityRenderer<TankBlockEntity> {

    // tank.png is 32px with a 1px opaque frame - keep the fluid behind that rim when exposed.
    private static final float INSET = 1.0F / 32.0F + 0.001F;
    private static final float MIN_HEIGHT = INSET * 2.0F;

    public TankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TankBlockEntity tank, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        FluidStack fluid = tank.getTank().getFluid();
        if (fluid.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluidType());
        ResourceLocation texture = extensions.getStillTexture(fluid);
        if (texture == null) {
            return;
        }
        TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);

        int colour = extensions.getTintColor(fluid);
        float a = ((colour >> 24) & 0xFF) / 255.0F;
        if (a == 0.0F) {
            a = 1.0F;
        }
        float r = ((colour >> 16) & 0xFF) / 255.0F;
        float g = ((colour >> 8) & 0xFF) / 255.0F;
        float b = (colour & 0xFF) / 255.0F;

        boolean shareWest = shares(tank, Direction.WEST) != null;
        boolean shareEast = shares(tank, Direction.EAST) != null;
        boolean shareNorth = shares(tank, Direction.NORTH) != null;
        boolean shareSouth = shares(tank, Direction.SOUTH) != null;
        boolean shareDown = shares(tank, Direction.DOWN) != null;
        boolean shareUp = shares(tank, Direction.UP) != null;

        // Flush with same-fluid neighbours; keep the glass-rim inset only on exposed sides.
        float x0 = shareWest ? 0.0F : INSET;
        float x1 = shareEast ? 1.0F : 1.0F - INSET;
        float z0 = shareNorth ? 0.0F : INSET;
        float z1 = shareSouth ? 1.0F : 1.0F - INSET;
        float y0 = shareDown ? 0.0F : INSET;
        float y1 = fluidTop(tank, y0, shareUp);

        VertexConsumer consumer = buffers.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        // A full tank under a tank holding the same fluid has no visible surface.
        boolean surfaceHidden = tank.fillRatio() >= 0.999F && shareUp;
        if (!surfaceHidden) {
            quad(consumer, matrix, 0.0F, 1.0F, 0.0F,
                    x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0,
                    still.getU0(), still.getV1(), still.getU1(), still.getV0(), r, g, b, a, light, overlay);
        }
        if (!shareDown) {
            quad(consumer, matrix, 0.0F, -1.0F, 0.0F,
                    x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1,
                    still.getU0(), still.getV0(), still.getU1(), still.getV1(), r, g, b, a, light, overlay);
        }

        for (Direction side : Direction.Plane.HORIZONTAL) {
            TankBlockEntity neighbour = shares(tank, side);
            float base = y0;
            if (neighbour != null) {
                float theirs = fluidTop(neighbour);
                // Shared face: never draw the overlapping band (that reads as a vertical divider).
                if (theirs >= y1 - 1.0E-4F) {
                    continue;
                }
                base = Math.min(y1, Math.max(y0, theirs));
            }
            if (y1 - base <= 1.0E-4F) {
                continue;
            }

            // Side quads use the merged bounds (0/1 on shared axes, inset on exposed ones).
            float vBottom = Mth.lerp(base, still.getV1(), still.getV0());
            float vTop = Mth.lerp(y1, still.getV1(), still.getV0());
            switch (side) {
                case NORTH -> {
                    float z = shareNorth ? 0.0F : z0;
                    quad(consumer, matrix, 0.0F, 0.0F, -1.0F,
                            x1, base, z, x0, base, z, x0, y1, z, x1, y1, z,
                            still.getU0(), vBottom, still.getU1(), vTop, r, g, b, a, light, overlay);
                }
                case SOUTH -> {
                    float z = shareSouth ? 1.0F : z1;
                    quad(consumer, matrix, 0.0F, 0.0F, 1.0F,
                            x0, base, z, x1, base, z, x1, y1, z, x0, y1, z,
                            still.getU0(), vBottom, still.getU1(), vTop, r, g, b, a, light, overlay);
                }
                case WEST -> {
                    float x = shareWest ? 0.0F : x0;
                    quad(consumer, matrix, -1.0F, 0.0F, 0.0F,
                            x, base, z0, x, base, z1, x, y1, z1, x, y1, z0,
                            still.getU0(), vBottom, still.getU1(), vTop, r, g, b, a, light, overlay);
                }
                default -> {
                    float x = shareEast ? 1.0F : x1;
                    quad(consumer, matrix, 1.0F, 0.0F, 0.0F,
                            x, base, z1, x, base, z0, x, y1, z0, x, y1, z1,
                            still.getU0(), vBottom, still.getU1(), vTop, r, g, b, a, light, overlay);
                }
            }
        }
    }

    private static float fluidTop(TankBlockEntity tank) {
        boolean shareDown = shares(tank, Direction.DOWN) != null;
        boolean shareUp = shares(tank, Direction.UP) != null;
        float y0 = shareDown ? 0.0F : INSET;
        return fluidTop(tank, y0, shareUp);
    }

    private static float fluidTop(TankBlockEntity tank, float y0, boolean shareUp) {
        float fill = Mth.clamp(tank.fillRatio(), 0.0F, 1.0F);
        if (shareUp && fill >= 0.999F) {
            return 1.0F;
        }
        float ceiling = shareUp ? 1.0F : 1.0F - INSET;
        return y0 + Math.max(MIN_HEIGHT, fill * (ceiling - y0));
    }

    /** The neighbouring tank on that side if it holds the same fluid, otherwise null. */
    @Nullable
    private static TankBlockEntity shares(TankBlockEntity tank, Direction side) {
        Level level = tank.getLevel();
        if (level == null) {
            return null;
        }
        if (!(level.getBlockEntity(tank.getBlockPos().relative(side)) instanceof TankBlockEntity neighbour)) {
            return null;
        }
        FluidStack ours = tank.getTank().getFluid();
        FluidStack theirs = neighbour.getTank().getFluid();
        if (ours.isEmpty() || theirs.isEmpty()) {
            return null;
        }
        return FluidStack.isSameFluidSameComponents(ours, theirs) ? neighbour : null;
    }

    private static void quad(VertexConsumer consumer, Matrix4f matrix,
                             float nx, float ny, float nz,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float u0, float v0, float u1, float v1,
                             float r, float g, float b, float a, int light, int overlay) {
        vertex(consumer, matrix, nx, ny, nz, x1, y1, z1, u0, v0, r, g, b, a, light, overlay);
        vertex(consumer, matrix, nx, ny, nz, x2, y2, z2, u1, v0, r, g, b, a, light, overlay);
        vertex(consumer, matrix, nx, ny, nz, x3, y3, z3, u1, v1, r, g, b, a, light, overlay);
        vertex(consumer, matrix, nx, ny, nz, x4, y4, z4, u0, v1, r, g, b, a, light, overlay);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix,
                               float nx, float ny, float nz,
                               float x, float y, float z, float u, float v,
                               float r, float g, float b, float a, int light, int overlay) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }
}
