package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.opencubes.OCConstants;
import dev.opencubes.client.luggage.LuggageModel;
import dev.opencubes.content.luggage.LuggageEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Ported {@code ModelLuggage} render: normal skin, or the atlas-style special skin after a lightning strike. */
public class LuggageRenderer extends MobRenderer<LuggageEntity, LuggageModel> {

    private static final ResourceLocation NORMAL_TEXTURE = OCConstants.id("textures/entity/luggage_normal_body.png");
    private static final ResourceLocation SPECIAL_TEXTURE = OCConstants.id("textures/entity/luggage_special.png");

    private final LuggageModel normalModel;
    private final LuggageModel specialModel;

    public LuggageRenderer(EntityRendererProvider.Context context) {
        super(context, new LuggageModel(context.bakeLayer(LuggageModel.NORMAL_LAYER)), 0.5F);
        this.normalModel = this.getModel();
        this.specialModel = new LuggageModel(context.bakeLayer(LuggageModel.SPECIAL_LAYER));
    }

    @Override
    public void render(LuggageEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight) {
        this.model = entity.isSpecial() ? specialModel : normalModel;
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(LuggageEntity entity) {
        return entity.isSpecial() ? SPECIAL_TEXTURE : NORMAL_TEXTURE;
    }
}
