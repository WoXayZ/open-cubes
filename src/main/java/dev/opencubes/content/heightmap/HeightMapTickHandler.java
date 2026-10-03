package dev.opencubes.content.heightmap;

import dev.opencubes.OCConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class HeightMapTickHandler {

    private HeightMapTickHandler() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        HeightMapManager.sendPendingUpdates(event.getServer());
    }
}
