package dev.opencubes.client.luggage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import dev.opencubes.client.LuggageRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * 1.21-style port of OpenBlocks 1.12 {@code ModelLuggage}: a lid, a body, and a ring of 21 tiny
 * "walking" legs underneath that swing like a millipede.
 *
 * <p>Two bakes share the same geometry (the box layout leaves plenty of blank canvas either way):
 * {@link #NORMAL_LAYER} matches the compact {@code luggage_normal_body.png} atlas, and
 * {@link #SPECIAL_LAYER} matches the original 128x64 {@code luggage_special.png} atlas.
 */
public class LuggageModel extends EntityModel<LuggageRenderState> {

    private static final int LEGS_X = 7;
    private static final int LEGS_Z = 3;
    private static final int LEG_COUNT = LEGS_X * LEGS_Z;

    public static final ModelLayerLocation NORMAL_LAYER =
            new ModelLayerLocation(OCConstants.id("luggage"), "normal");
    public static final ModelLayerLocation SPECIAL_LAYER =
            new ModelLayerLocation(OCConstants.id("luggage"), "special");

    private final ModelPart body;
    private final ModelPart lid;
    private final ModelPart[] legs = new ModelPart[LEG_COUNT];

    public LuggageModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.lid = root.getChild("lid");
        int i = 0;
        for (int x = -3; x <= 3; x++) {
            for (int z = -1; z <= 1; z++) {
                legs[i++] = root.getChild(legName(x, z));
            }
        }
    }

    public static LayerDefinition createBodyLayer(int textureWidth, int textureHeight, int lidV) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-8F, 0F, -4F, 16F, 7F, 8F),
                PartPose.offset(0F, 13F, 0F));

        root.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, lidV).mirror().addBox(-8F, -2F, -8F, 16F, 2F, 8F),
                PartPose.offset(0F, 13F, 4F));

        for (int x = -3; x <= 3; x++) {
            for (int z = -1; z <= 1; z++) {
                root.addOrReplaceChild(legName(x, z),
                        CubeListBuilder.create().texOffs(0, 41).mirror().addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
                        PartPose.offset(x * 2F, 20F, z * 2F));
            }
        }

        return LayerDefinition.create(mesh, textureWidth, textureHeight);
    }

    private static String legName(int x, int z) {
        return "leg_" + (x < 0 ? "n" + (-x) : String.valueOf(x)) + "_" + (z < 0 ? "n" + (-z) : String.valueOf(z));
    }

    @Override
    public void setupAnim(LuggageRenderState state) {
        if (state.entity == null) {
            return;
        }
        float limbSwing = state.walkPosition;
        float limbSwingAmount = state.walkSpeed;
        float open = state.entity.lidOpenness(state.ageInTicks - state.entity.tickCount);
        open = 1.0F - open;
        open = 1.0F - open * open * open;
        float walk = Math.min(0F, Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount);
        lid.xRot = Math.min(walk, -open * 1.2F);

        int i = 0;
        for (int x = -3; x <= 3; x++) {
            for (int z = -1; z <= 1; z++) {
                legs[i++].xRot = Mth.cos(limbSwing + (x * z) * 0.6662F) * 1.4F * limbSwingAmount;
            }
        }
    }

}
