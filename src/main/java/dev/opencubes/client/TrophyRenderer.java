package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;

public class TrophyRenderer implements BlockEntityRenderer<TrophyBlockEntity> {

    private final EntityRenderDispatcher entityRenderer;

    public TrophyRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }

    @Override
    public void render(TrophyBlockEntity trophy, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light, int overlay) {
        TrophyDefinition definition = trophy.definition().orElse(null);
        Entity entity = trophy.getOrCreateRenderEntity();
        if (definition == null || entity == null) {
            return;
        }

        float facing = trophy.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot();
        freezeFacing(entity, facing);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.2D + definition.verticalOffset(), 0.5D);
        // Placement facing only - entity yaw is frozen, no idle spin.
        // Ender dragon model faces the opposite way from living mobs.
        float yaw = entity instanceof EnderDragon ? -facing + 180.0F : -facing;
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        float scale = definition.scale();
        poseStack.scale(scale, scale, scale);

        entityRenderer.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, poseStack, buffers, light);
        poseStack.popPose();
    }

    /**
     * The mob is drawn past the block cube. A tight box culls it while it is still on screen.
     */
    @Override
    public AABB getRenderBoundingBox(TrophyBlockEntity trophy) {
        double extra = trophy.definition()
                .map(def -> Math.max(0.5D, def.scale() * 2.0D))
                .orElse(0.5D);
        return BlockEntityRenderer.super.getRenderBoundingBox(trophy).inflate(extra);
    }

    private static void freezeFacing(Entity entity, float facing) {
        entity.setYRot(0.0F);
        entity.yRotO = 0.0F;
        entity.setXRot(0.0F);
        entity.xRotO = 0.0F;
        entity.tickCount = 0;
        if (entity instanceof LivingEntity living) {
            living.yBodyRot = 0.0F;
            living.yBodyRotO = 0.0F;
            living.yHeadRot = 0.0F;
            living.yHeadRotO = 0.0F;
        }
        if (entity instanceof Bat bat) {
            bat.setResting(true);
        }
    }
}
