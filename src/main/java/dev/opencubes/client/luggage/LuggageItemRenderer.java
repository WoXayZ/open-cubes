package dev.opencubes.client.luggage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import dev.opencubes.OCConstants;
import dev.opencubes.registry.OCDataComponents;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Inventory and hand model: the luggage entity mesh, not the flat icon.
 *
 * <p>Replaces the removed item renderer hook. Register {@link Unbaked} on
 * {@code RegisterSpecialModelRendererEvent} and point the item model at that special model.
 * The feet flip is the only pose applied here. Scale, the inventory yaw and the per-context
 * placement live on the item model, the same split as a trident.
 */
public final class LuggageItemRenderer implements SpecialModelRenderer<Boolean> {

    private static final Identifier NORMAL = OCConstants.id("textures/entity/luggage_normal_body.png");
    private static final Identifier SPECIAL = OCConstants.id("textures/entity/luggage_special.png");

    private ModelPart normalRoot;
    private ModelPart specialRoot;

    public LuggageItemRenderer() {}

    @Override
    public @Nullable Boolean extractArgument(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(OCDataComponents.LUGGAGE_SPECIAL.get()));
    }

    @Override
    public void submit(
            @Nullable Boolean special,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor) {
        if (this.normalRoot == null) {
            var models = Minecraft.getInstance().getEntityModels();
            this.normalRoot = models.bakeLayer(LuggageModel.NORMAL_LAYER);
            this.specialRoot = models.bakeLayer(LuggageModel.SPECIAL_LAYER);
        }
        boolean upgraded = Boolean.TRUE.equals(special);
        ModelPart root = upgraded ? this.specialRoot : this.normalRoot;
        Identifier texture = upgraded ? SPECIAL : NORMAL;

        poseStack.pushPose();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        submitNodeCollector.submitModelPart(
                root, poseStack, RenderTypes.entityCutout(texture), lightCoords, overlayCoords, null, false, hasFoil);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        if (this.normalRoot == null) {
            output.accept(new Vector3f(0.0F, 0.0F, 0.0F));
            output.accept(new Vector3f(1.0F, 1.0F, 1.0F));
            return;
        }
        PoseStack poseStack = new PoseStack();
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        this.normalRoot.getExtentsForGui(poseStack, output);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Boolean> {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public SpecialModelRenderer<Boolean> bake(SpecialModelRenderer.BakingContext context) {
            LuggageItemRenderer renderer = new LuggageItemRenderer();
            renderer.normalRoot = context.entityModelSet().bakeLayer(LuggageModel.NORMAL_LAYER);
            renderer.specialRoot = context.entityModelSet().bakeLayer(LuggageModel.SPECIAL_LAYER);
            return renderer;
        }
    }
}
