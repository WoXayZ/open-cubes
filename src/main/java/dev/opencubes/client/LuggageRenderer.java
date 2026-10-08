package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.opencubes.OCConstants;
import dev.opencubes.client.luggage.LuggageModel;
import dev.opencubes.content.luggage.LuggageEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Ported ModelLuggage render: normal skin, or the atlas-style special skin after a lightning strike.
 *
 * <p>Leg and lid motion still goes through {@link LuggageModel#setupAnim}, which takes the entity.
 * The pose is applied when the geometry is drawn so two luggage in one frame do not share a pose.
 */
public class LuggageRenderer extends EntityRenderer<LuggageEntity, LuggageRenderState> {

    private static final Identifier NORMAL_TEXTURE = OCConstants.id("textures/entity/luggage_normal_body.png");
    private static final Identifier SPECIAL_TEXTURE = OCConstants.id("textures/entity/luggage_special.png");

    private final LuggageModel normalModel;
    private final LuggageModel specialModel;
    private final ModelPart normalRoot;
    private final ModelPart specialRoot;

    public LuggageRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.normalRoot = context.bakeLayer(LuggageModel.NORMAL_LAYER);
        this.specialRoot = context.bakeLayer(LuggageModel.SPECIAL_LAYER);
        this.normalModel = new LuggageModel(this.normalRoot);
        this.specialModel = new LuggageModel(this.specialRoot);
    }

    @Override
    public LuggageRenderState createRenderState() {
        return new LuggageRenderState();
    }

    @Override
    public void extractRenderState(LuggageEntity entity, LuggageRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.special = entity.isSpecial();
        state.entity = entity;
        state.bodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        state.scale = entity.getScale();
        state.walkPosition = entity.walkAnimation.position(partialTicks);
        state.walkSpeed = entity.walkAnimation.speed(partialTicks);
        state.deathTime = entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F;
        state.hasRedOverlay = entity.hurtTime > 0 || entity.deathTime > 0;
    }

    @Override
    public void submit(LuggageRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        LuggageModel model = state.special ? this.specialModel : this.normalModel;
        ModelPart root = state.special ? this.specialRoot : this.normalRoot;
        Identifier texture = state.special ? SPECIAL_TEXTURE : NORMAL_TEXTURE;
        LuggageEntity entity = state.entity;
        int light = state.lightCoords;
        int overlay = OverlayTexture.pack(0.0F, state.hasRedOverlay);
        poseStack.pushPose();
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.bodyRot));
        if (state.deathTime > 0.0F) {
            float fall = (state.deathTime - 1.0F) / 20.0F * 1.6F;
            fall = Mth.sqrt(fall);
            if (fall > 1.0F) {
                fall = 1.0F;
            }
            poseStack.mulPose(Axis.ZP.rotationDegrees(fall * 90.0F));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.501F, 0.0F);
        float walkPosition = state.walkPosition;
        float walkSpeed = state.walkSpeed;
        float age = state.ageInTicks;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(texture), (pose, buffer) -> {
            state.ageInTicks = age;
            model.setupAnim(state);
            PoseStack modelPose = new PoseStack();
            modelPose.last().set(pose);
            root.render(modelPose, buffer, light, overlay);
        });
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
