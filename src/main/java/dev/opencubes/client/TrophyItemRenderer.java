package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.content.trophy.TrophyDefinition;
import dev.opencubes.registry.OCBlocks;
import dev.opencubes.registry.OCDataComponents;
import dev.opencubes.registry.OCRegistries;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Renders the trophy pedestal plus the typed mob when the item is held or in a GUI slot. */
public final class TrophyItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final Map<ResourceLocation, Entity> entityCache = new HashMap<>();

    public TrophyItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffers, int light, int overlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockState state = OCBlocks.TROPHY.get().defaultBlockState();
        minecraft.getBlockRenderer().renderSingleBlock(state, poseStack, buffers, light, overlay);

        ResourceLocation trophyId = stack.get(OCDataComponents.TROPHY_ID.get());
        Level level = minecraft.level;
        if (trophyId == null || level == null) {
            return;
        }

        Optional<TrophyDefinition> definition = level.registryAccess().registry(OCRegistries.TROPHY)
                .flatMap(reg -> reg.getOptional(trophyId));
        if (definition.isEmpty()) {
            return;
        }
        TrophyDefinition def = definition.get();
        Entity entity = getOrCreateEntity(level, trophyId, def);
        if (entity == null) {
            return;
        }

        freezeEntity(entity);
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.2D + def.verticalOffset(), 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float scale = def.scale();
        poseStack.scale(scale, scale, scale);
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, poseStack, buffers, light);
        poseStack.popPose();
    }

    private Entity getOrCreateEntity(Level level, ResourceLocation trophyId, TrophyDefinition def) {
        Entity cached = entityCache.get(trophyId);
        if (cached != null && !cached.isRemoved() && cached.level() == level) {
            return cached;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(def.entity());
        if (type == null) {
            return null;
        }
        Entity entity = type.create(level);
        if (entity == null) {
            return null;
        }
        if (entity instanceof Bat bat) {
            bat.setResting(true);
        }
        entityCache.put(trophyId, entity);
        return entity;
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
}
