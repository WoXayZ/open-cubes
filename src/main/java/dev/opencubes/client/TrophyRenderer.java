package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.content.trophy.TrophyBlockEntity;
import dev.opencubes.content.trophy.TrophyDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class TrophyRenderer implements BlockEntityRenderer<TrophyBlockEntity, TrophyRenderState> {

    private final EntityRenderDispatcher entityRenderer;

    public TrophyRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public TrophyRenderState createRenderState() {
        return new TrophyRenderState();
    }

    @Override
    public void extractRenderState(
            TrophyBlockEntity trophy,
            TrophyRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(trophy, state, partialTicks, cameraPosition, breakProgress);
        TrophyDefinition definition = trophy.definition().orElse(null);
        Entity entity = trophy.getOrCreateRenderEntity();
        state.draw = definition != null && entity != null;
        if (!state.draw) {
            state.entity = null;
            return;
        }
        float facing = trophy.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot();
        freezeFacing(entity);
        state.yaw = entity instanceof EnderDragon ? -facing + 180.0F : -facing;
        state.scale = definition.scale();
        state.verticalOffset = definition.verticalOffset();
        state.entity = capture(this.entityRenderer, entity, state.lightCoords);
    }

    @Override
    public void submit(TrophyRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (!state.draw || state.entity == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.2D + state.verticalOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poseStack.scale(state.scale, state.scale, state.scale);
        this.entityRenderer.submit(state.entity, camera, 0.0D, 0.0D, 0.0D, poseStack, submitNodeCollector);
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static EntityRenderState capture(EntityRenderDispatcher dispatcher, Entity entity, int light) {
        EntityRenderer renderer = dispatcher.getRenderer(entity);
        EntityRenderState captured = renderer.createRenderState(entity, 0.0F);
        captured.lightCoords = light;
        // Shadow pieces are stored in world space. The trophy draws the mob at the block, so those
        // pieces would land far from the pedestal.
        captured.shadowPieces.clear();
        return captured;
    }

    private static void freezeFacing(Entity entity) {
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
