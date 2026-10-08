package dev.opencubes.client.crane;

import dev.opencubes.OCConstants;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The OpenBlocks crane backpack (a box on the back, a mast and a boom that follows the head)
 * and its stepped magnet. Model units are pixels, as in every vanilla entity model.
 */
public final class CraneModels {

    public static final Identifier BACKPACK_TEXTURE = OCConstants.id("textures/entity/crane_backpack.png");
    public static final Identifier MAGNET_TEXTURE = OCConstants.id("textures/entity/crane_magnet.png");

    /** Boom pivot, relative to the top of the body: a block above the neck, 7 px behind. */
    public static final float PIVOT_UP = 16.0F;
    public static final float PIVOT_BACK = 7.0F;
    /**
     * The boom starts 2 px behind its pivot as a counterweight. Its 42 px length is the one the
     * texture is laid out for; the cable hangs 1 px before the end, 2 blocks ahead of the player.
     */
    public static final int BOOM_START = -2;
    public static final int BOOM_LENGTH = 42;
    public static final int BOOM_REACH = BOOM_START + BOOM_LENGTH - 1;
    /** Top of the magnet model above the entity position, in blocks. */
    public static final double MAGNET_TOP = 6.0D / 16.0D;

    private CraneModels() {}

    /** Root holding {@code body} (box and mast) with the {@code arm} boom as its child. */
    public static ModelPart createBackpack() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 8, new CubeDeformation(0.3F))
                        .texOffs(32, 0).addBox(-1.0F, -PIVOT_UP, 6.0F, 2, 24, 2),
                PartPose.ZERO);
        body.addOrReplaceChild("arm", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0F, 0.0F, BOOM_START, 2, 2, BOOM_LENGTH),
                PartPose.offset(0.0F, -PIVOT_UP, PIVOT_BACK));
        return LayerDefinition.create(mesh, 128, 64).bakeRoot();
    }

    /**
     * A 6×6 plate under two smaller steps, drawn at twice the pixel size like the original. The
     * texture expects Y to point up (the opposite of other entity models): the low face of each
     * box is the visible underside, and the high faces only show the ring left uncovered above.
     */
    public static ModelPart createMagnet() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("magnet", CubeListBuilder.create().mirror()
                        .texOffs(0, 0).addBox(-3.0F, 0.0F, -3.0F, 6, 1, 6)
                        .texOffs(0, 7).addBox(-2.0F, 1.0F, -2.0F, 4, 1, 4)
                        .texOffs(0, 12).addBox(-1.0F, 2.0F, -1.0F, 2, 1, 2),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32).bakeRoot();
    }

    private static Vec3 horizontal(float yawDegrees, double distance) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw) * distance, 0.0D, Mth.cos(yaw) * distance);
    }

    /** World position of the boom pivot, matching where the worn model puts it. */
    public static Vec3 boomPivot(Player player, float partialTick) {
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        double neck = player.getBbHeight() - 0.3D;
        return player.getPosition(partialTick)
                .add(0.0D, neck + PIVOT_UP / 16.0D, 0.0D)
                .add(horizontal(bodyYaw, -PIVOT_BACK / 16.0D));
    }

    /** Where the cable leaves the boom: under its far end. */
    public static Vec3 boomTip(Player player, float partialTick) {
        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        return boomPivot(player, partialTick)
                .add(0.0D, -2.0D / 16.0D, 0.0D)
                .add(horizontal(headYaw, BOOM_REACH / 16.0D));
    }
}
