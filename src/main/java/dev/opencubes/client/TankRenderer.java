package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.tank.TankBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/**
 * Draws the fluid inside a tank.
 *
 * <p>The block model is a cutout glass cube ({@code tank.png} + CTM). Fluid is inset just inside
 * the frame border on exposed sides. Faces shared with a neighbouring tank holding the same fluid
 * are omitted and the body extends flush to that edge, so adjacent tanks read as one volume.
 */
public class TankRenderer implements BlockEntityRenderer<TankBlockEntity, TankRenderState> {

    // tank.png is 32px with a 1px opaque frame - keep the fluid behind that rim when exposed.
    private static final float INSET = 1.0F / 32.0F + 0.001F;
    private static final float MIN_HEIGHT = INSET * 2.0F;

    public TankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public TankRenderState createRenderState() {
        return new TankRenderState();
    }

    @Override
    public void extractRenderState(
            TankBlockEntity tank,
            TankRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tank, state, partialTicks, cameraPosition, breakProgress);
        FluidStack fluid = tank.getTank().getFluid();
        state.empty = fluid.isEmpty();
        if (state.empty) {
            return;
        }

        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(fluid.getFluid().defaultFluidState());
        TextureAtlasSprite still = model.stillMaterial().sprite();
        FluidTintSource tint = model.fluidTintSource();
        int colour = tint == null ? 0xFFFFFFFF : tint.colorAsStack(fluid);
        state.alpha = ((colour >> 24) & 0xFF) / 255.0F;
        if (state.alpha == 0.0F) {
            state.alpha = 1.0F;
        }
        state.red = ((colour >> 16) & 0xFF) / 255.0F;
        state.green = ((colour >> 8) & 0xFF) / 255.0F;
        state.blue = (colour & 0xFF) / 255.0F;
        state.u0 = still.getU0();
        state.v0 = still.getV0();
        state.u1 = still.getU1();
        state.v1 = still.getV1();

        boolean shareWest = shares(tank, Direction.WEST) != null;
        boolean shareEast = shares(tank, Direction.EAST) != null;
        boolean shareNorth = shares(tank, Direction.NORTH) != null;
        boolean shareSouth = shares(tank, Direction.SOUTH) != null;
        boolean shareDown = shares(tank, Direction.DOWN) != null;
        boolean shareUp = shares(tank, Direction.UP) != null;

        state.x0 = shareWest ? 0.0F : INSET;
        state.x1 = shareEast ? 1.0F : 1.0F - INSET;
        state.z0 = shareNorth ? 0.0F : INSET;
        state.z1 = shareSouth ? 1.0F : 1.0F - INSET;
        state.y0 = shareDown ? 0.0F : INSET;
        state.y1 = fluidTop(tank, state.y0, shareUp);
        state.surfaceHidden = tank.fillRatio() >= 0.999F && shareUp;
        state.drawBottom = !shareDown;

        int index = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            TankRenderState.Side face = state.sides[index++];
            face.direction = side;
            face.draw = false;
            TankBlockEntity neighbour = shares(tank, side);
            float base = state.y0;
            if (neighbour != null) {
                float theirs = fluidTop(neighbour);
                if (theirs >= state.y1 - 1.0E-4F) {
                    continue;
                }
                base = Math.min(state.y1, Math.max(state.y0, theirs));
            }
            if (state.y1 - base <= 1.0E-4F) {
                continue;
            }
            face.draw = true;
            face.base = base;
            face.plane = switch (side) {
                case NORTH -> shareNorth ? 0.0F : state.z0;
                case SOUTH -> shareSouth ? 1.0F : state.z1;
                case WEST -> shareWest ? 0.0F : state.x0;
                default -> shareEast ? 1.0F : state.x1;
            };
        }
    }

    @Override
    public void submit(TankRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (state.empty) {
            return;
        }
        int light = state.lightCoords;
        submitNodeCollector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(), (pose, buffer) -> {
            var matrix = pose.pose();
            if (!state.surfaceHidden) {
                quad(buffer, matrix, 0.0F, 1.0F, 0.0F,
                        state.x0, state.y1, state.z1, state.x1, state.y1, state.z1,
                        state.x1, state.y1, state.z0, state.x0, state.y1, state.z0,
                        state.u0, state.v1, state.u1, state.v0, state, light);
            }
            if (state.drawBottom) {
                quad(buffer, matrix, 0.0F, -1.0F, 0.0F,
                        state.x0, state.y0, state.z0, state.x1, state.y0, state.z0,
                        state.x1, state.y0, state.z1, state.x0, state.y0, state.z1,
                        state.u0, state.v0, state.u1, state.v1, state, light);
            }
            for (TankRenderState.Side side : state.sides) {
                if (!side.draw) {
                    continue;
                }
                float vBottom = Mth.lerp(side.base, state.v1, state.v0);
                float vTop = Mth.lerp(state.y1, state.v1, state.v0);
                float plane = side.plane;
                switch (side.direction) {
                    case NORTH -> quad(buffer, matrix, 0.0F, 0.0F, -1.0F,
                            state.x1, side.base, plane, state.x0, side.base, plane,
                            state.x0, state.y1, plane, state.x1, state.y1, plane,
                            state.u0, vBottom, state.u1, vTop, state, light);
                    case SOUTH -> quad(buffer, matrix, 0.0F, 0.0F, 1.0F,
                            state.x0, side.base, plane, state.x1, side.base, plane,
                            state.x1, state.y1, plane, state.x0, state.y1, plane,
                            state.u0, vBottom, state.u1, vTop, state, light);
                    case WEST -> quad(buffer, matrix, -1.0F, 0.0F, 0.0F,
                            plane, side.base, state.z0, plane, side.base, state.z1,
                            plane, state.y1, state.z1, plane, state.y1, state.z0,
                            state.u0, vBottom, state.u1, vTop, state, light);
                    default -> quad(buffer, matrix, 1.0F, 0.0F, 0.0F,
                            plane, side.base, state.z1, plane, side.base, state.z0,
                            plane, state.y1, state.z0, plane, state.y1, state.z1,
                            state.u0, vBottom, state.u1, vTop, state, light);
                }
            }
        });
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

    private static void quad(VertexConsumer consumer, org.joml.Matrix4f matrix,
                             float nx, float ny, float nz,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float u0, float v0, float u1, float v1,
                             TankRenderState state, int light) {
        vertex(consumer, matrix, nx, ny, nz, x1, y1, z1, u0, v0, state, light);
        vertex(consumer, matrix, nx, ny, nz, x2, y2, z2, u1, v0, state, light);
        vertex(consumer, matrix, nx, ny, nz, x3, y3, z3, u1, v1, state, light);
        vertex(consumer, matrix, nx, ny, nz, x4, y4, z4, u0, v1, state, light);
    }

    private static void vertex(VertexConsumer consumer, org.joml.Matrix4f matrix,
                               float nx, float ny, float nz,
                               float x, float y, float z, float u, float v,
                               TankRenderState state, int light) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(state.red, state.green, state.blue, state.alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }
}
