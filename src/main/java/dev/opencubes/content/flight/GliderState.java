package dev.opencubes.content.flight;

import net.minecraft.world.InteractionHand;

/** Single source of truth for hang-glider deployment (§10). */
public class GliderState {

    private boolean engaged;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private double lastMotionY;
    private double lastVerticalSpeed;

    public boolean isEngaged() {
        return engaged;
    }

    public void setEngaged(boolean engaged) {
        this.engaged = engaged;
    }

    public void toggle() {
        engaged = !engaged;
    }

    public InteractionHand hand() {
        return hand;
    }

    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }

    public double lastMotionY() {
        return lastMotionY;
    }

    public void setLastMotionY(double lastMotionY) {
        this.lastMotionY = lastMotionY;
    }

    public double lastVerticalSpeed() {
        return lastVerticalSpeed;
    }

    public void setLastVerticalSpeed(double lastVerticalSpeed) {
        this.lastVerticalSpeed = lastVerticalSpeed;
    }
}
