package dev.opencubes.client.sky;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.opencubes.OCConstants;
import java.io.IOException;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class SkyShaders {

    @Nullable
    private static ShaderInstance skyWindow;

    private SkyShaders() {}

    @Nullable
    public static ShaderInstance skyWindow() {
        return skyWindow;
    }

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), OCConstants.id("sky_window"),
                        DefaultVertexFormat.POSITION),
                shader -> skyWindow = shader);
    }
}
