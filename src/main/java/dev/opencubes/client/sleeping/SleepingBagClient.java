package dev.opencubes.client.sleeping;

import dev.opencubes.OCConstants;
import dev.opencubes.content.sleeping.SleepingBagItem;
import java.lang.reflect.Method;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Raises the first-person camera while sleeping in a sleeping bag so the view is not inside the torso.
 */
@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class SleepingBagClient {

    @Nullable
    private static final Method CAMERA_MOVE = findCameraMove();

    private SleepingBagClient() {}

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (CAMERA_MOVE == null) {
            return;
        }
        Camera camera = event.getCamera();
        if (!(camera.entity() instanceof LocalPlayer player)) {
            return;
        }
        if (!player.isSleeping()) {
            return;
        }
        if (!(player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof SleepingBagItem)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        try {
            CAMERA_MOVE.invoke(camera, 0.0F, 0.4F, 0.0F);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Nullable
    private static Method findCameraMove() {
        try {
            Method move = Camera.class.getDeclaredMethod("move", float.class, float.class, float.class);
            move.setAccessible(true);
            return move;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
