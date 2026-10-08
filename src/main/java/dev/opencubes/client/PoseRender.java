package dev.opencubes.client;

import com.mojang.blaze3d.vertex.PoseStack;

/** The custom-geometry callback hands back a pose, while model parts still draw with a stack. */
public final class PoseRender {

    private PoseRender() {}

    public static PoseStack stack(PoseStack.Pose pose) {
        PoseStack stack = new PoseStack();
        stack.last().set(pose);
        return stack;
    }
}
