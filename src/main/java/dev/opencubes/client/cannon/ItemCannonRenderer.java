package dev.opencubes.client.cannon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.content.cannon.CannonTrajectory;
import dev.opencubes.content.cannon.ItemCannonBlockEntity;
import dev.opencubes.registry.OCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ItemCannonRenderer implements BlockEntityRenderer<ItemCannonBlockEntity> {

    private static final int TRAJECTORY_STEPS = 150;
    private static final float[] XS = new float[TRAJECTORY_STEPS];
    private static final float[] YS = new float[TRAJECTORY_STEPS];
    private static final float[] ZS = new float[TRAJECTORY_STEPS];

    public ItemCannonRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(ItemCannonBlockEntity cannon, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (!playerHasPointer()) {
            return;
        }

        Vec3 muzzle = CannonTrajectory.muzzleOffset(cannon.getYaw(), cannon.getPitch());
        Vec3 motion = CannonTrajectory.motionFromAngles(cannon.getYaw(), cannon.getPitch(), cannon.getSpeed());
        CannonTrajectory.simulateRelative(muzzle, motion, TRAJECTORY_STEPS, XS, YS, ZS);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffers.getBuffer(RenderType.lines());
        for (int i = 1; i < TRAJECTORY_STEPS; i++) {
            line(consumer, matrix, XS[i - 1], YS[i - 1], ZS[i - 1], XS[i], YS[i], ZS[i]);
        }
    }

    private static void line(VertexConsumer consumer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2) {
        consumer.addVertex(matrix, x1, y1, z1).setColor(0, 0, 0, 255).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex(matrix, x2, y2, z2).setColor(0, 0, 0, 255).setNormal(0.0F, 1.0F, 0.0F);
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
    public boolean shouldRenderOffScreen(ItemCannonBlockEntity cannon) {
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
