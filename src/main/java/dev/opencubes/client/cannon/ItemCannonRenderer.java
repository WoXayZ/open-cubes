package dev.opencubes.client.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.client.village.VillageHighlighterRenderer;
import dev.opencubes.content.cannon.CannonTrajectory;
import dev.opencubes.content.cannon.ItemCannonBlockEntity;
import dev.opencubes.registry.OCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemCannonRenderer implements BlockEntityRenderer<ItemCannonBlockEntity, ItemCannonRenderState> {

    private static final int TRAJECTORY_STEPS = 150;
    private static final float[] XS = new float[TRAJECTORY_STEPS];
    private static final float[] YS = new float[TRAJECTORY_STEPS];
    private static final float[] ZS = new float[TRAJECTORY_STEPS];

    public ItemCannonRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public ItemCannonRenderState createRenderState() {
        return new ItemCannonRenderState();
    }

    @Override
    public void extractRenderState(
            ItemCannonBlockEntity blockEntity,
            ItemCannonRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.draw = playerHasPointer();
        if (!state.draw) {
            return;
        }
        Vec3 muzzle = CannonTrajectory.muzzleOffset(blockEntity.getYaw(), blockEntity.getPitch());
        Vec3 motion = CannonTrajectory.motionFromAngles(blockEntity.getYaw(), blockEntity.getPitch(), blockEntity.getSpeed());
        CannonTrajectory.simulateRelative(muzzle, motion, TRAJECTORY_STEPS, XS, YS, ZS);
        state.xs = XS.clone();
        state.ys = YS.clone();
        state.zs = ZS.clone();
    }

    @Override
    public void submit(ItemCannonRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (!state.draw || state.xs.length < 2) {
            return;
        }
        float[] xs = state.xs;
        float[] ys = state.ys;
        float[] zs = state.zs;
        float width = VillageHighlighterRenderer.lineWidth();
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) -> {
            for (int i = 1; i < xs.length; i++) {
                line(buffer, pose, xs[i - 1], ys[i - 1], zs[i - 1], xs[i], ys[i], zs[i], width);
            }
        });
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2, float width) {
        VillageHighlighterRenderer.line(consumer, pose, x1, y1, z1, x2, y2, z2, 0.0F, 0.0F, 0.0F, 1.0F, width);
    }

    private static boolean playerHasPointer() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return false;
        }
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.is(OCItems.POINTER.get()) || off.is(OCItems.POINTER.get());
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(ItemCannonBlockEntity cannon) {
        return BlockEntityRenderer.super.getRenderBoundingBox(cannon).inflate(32.0D);
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
