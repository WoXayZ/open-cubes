package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import dev.opencubes.content.trophy.TrophyDefinition;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCRegistries;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Renders the trophy pedestal plus the typed mob when the item is held or in a GUI slot.
 *
 * <p>Replaces the removed item renderer hook. Register {@link Unbaked} on
 * {@code RegisterSpecialModelRendererEvent} and point the item model at that special model.
 * The mob is submitted with the current level camera because this hook has no camera of its own.
 */
public final class TrophyItemRenderer implements SpecialModelRenderer<TrophyItemRenderer.TrophyItemState> {

    private final Map<Identifier, Entity> entityCache = new HashMap<>();
    private final BlockModelResolver blockModelResolver;
    private final EntityRenderDispatcher entityRenderer;

    public TrophyItemRenderer() {
        Minecraft minecraft = Minecraft.getInstance();
        this.blockModelResolver = new BlockModelResolver(minecraft.getModelManager());
        this.entityRenderer = minecraft.getEntityRenderDispatcher();
    }

    @Override
    public TrophyItemState extractArgument(ItemStack stack) {
        TrophyItemState state = new TrophyItemState();
        this.blockModelResolver.update(
                state.pedestal, OCBlocks.TROPHY.get().defaultBlockState(), BlockDisplayContext.create());

        Minecraft minecraft = Minecraft.getInstance();
        Identifier trophyId = stack.get(OCDataComponents.TROPHY_ID.get());
        Level level = minecraft.level;
        if (trophyId == null || level == null) {
            return state;
        }
        Optional<TrophyDefinition> definition = level.registryAccess().lookup(OCRegistries.TROPHY)
                .flatMap(reg -> reg.getOptional(trophyId));
        if (definition.isEmpty()) {
            return state;
        }
        TrophyDefinition def = definition.get();
        Entity entity = getOrCreateEntity(level, trophyId, def);
        if (entity == null) {
            return state;
        }
        freezeEntity(entity);
        state.entity = capture(this.entityRenderer, entity);
        state.scale = def.scale();
        state.verticalOffset = def.verticalOffset();
        return state;
    }

    @Override
    public void submit(
            @Nullable TrophyItemState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor) {
        if (state == null) {
            return;
        }
        if (state.entity != null) {
            state.entity.lightCoords = lightCoords;
        }
        state.pedestal.submitMultiLayer(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor);
        if (state.entity == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.2D + state.verticalOffset, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(state.scale, state.scale, state.scale);
        CameraRenderState camera = Minecraft.getInstance().gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
        this.entityRenderer.submit(state.entity, camera, 0.0D, 0.0D, 0.0D, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0.0F, 0.0F, 0.0F));
        output.accept(new Vector3f(1.0F, 1.0F, 1.0F));
    }

    private Entity getOrCreateEntity(Level level, Identifier trophyId, TrophyDefinition def) {
        Entity cached = this.entityCache.get(trophyId);
        if (cached != null && !cached.isRemoved() && cached.level() == level) {
            return cached;
        }
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(def.entity());
        if (type.isEmpty()) {
            return null;
        }
        Entity entity = type.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        if (entity instanceof Bat bat) {
            bat.setResting(true);
        }
        this.entityCache.put(trophyId, entity);
        return entity;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static EntityRenderState capture(EntityRenderDispatcher dispatcher, Entity entity) {
        EntityRenderer renderer = dispatcher.getRenderer(entity);
        EntityRenderState captured = renderer.createRenderState(entity, 0.0F);
        captured.shadowPieces.clear();
        return captured;
    }

    private static void freezeEntity(Entity entity) {
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

    public static final class TrophyItemState {
        public final BlockModelRenderState pedestal = new BlockModelRenderState();
        public @Nullable EntityRenderState entity;
        public float scale = 1.0F;
        public float verticalOffset;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<TrophyItemState> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<TrophyItemState> bake(SpecialModelRenderer.BakingContext context) {
            return new TrophyItemRenderer();
        }
    }
}
