package dev.opencubes.client.tomfoolery;

import com.mojang.blaze3d.platform.InputConstants;
import dev.opencubes.OCConstants;
import dev.opencubes.network.BrickDropPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class BrickKeyHandler {

    public static final KeyMapping DROP_BRICK = new KeyMapping(
            "key.opencubes.drop_brick",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.opencubes");

    private BrickKeyHandler() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(DROP_BRICK);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        // Config is server-authoritative for behaviour; client only gates the key spam.
        while (DROP_BRICK.consumeClick()) {
            PacketDistributor.sendToServer(new BrickDropPayload());
        }
    }
}
