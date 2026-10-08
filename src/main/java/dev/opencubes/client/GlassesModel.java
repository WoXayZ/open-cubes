package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.opencubes.OCConstants;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Unit;

/** Sonic glasses: bar, ear cups, eye pieces and the three-stage cones of the 1.12 model. */
public class GlassesModel extends Model<Unit> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(OCConstants.id("glasses"), "main");

    private final ModelPart head;

    public GlassesModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.head = root.getChild("head");
    }

    @Override
    public void setupAnim(Unit state) {
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-5, -4, -5, 10, 1, 1)
                        .texOffs(0, 22).addBox(-6, -5, -1, 2, 3, 3)
                        .texOffs(0, 22).addBox(4, -5, -1, 2, 3, 3)
                        .texOffs(0, 28).addBox(-3, -5, -5, 2, 3, 1)
                        .texOffs(0, 28).addBox(1, -5, -5, 2, 3, 1)
                        .texOffs(0, 18).addBox(-5, -4, -4, 1, 1, 3)
                        .texOffs(0, 18).addBox(4, -4, -4, 1, 1, 3)
                        .texOffs(10, 18).addBox(-5, -10, 0, 1, 5, 1)
                        .texOffs(10, 18).addBox(4, -10, 0, 1, 5, 1)
                        .texOffs(32, 0).addBox(-6, -11, -3, 3, 3, 3)
                        .texOffs(32, 0).addBox(3, -11, -3, 3, 3, 3)
                        .texOffs(32, 6).addBox(-7, -12, -7, 5, 5, 4)
                        .texOffs(32, 6).addBox(2, -12, -7, 5, 5, 4)
                        .texOffs(32, 15).addBox(-8, -13, -9, 7, 7, 2)
                        .texOffs(32, 15).addBox(1, -13, -9, 7, 7, 2),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void draw(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        head.render(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
