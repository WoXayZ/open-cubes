package dev.opencubes.network;

import dev.opencubes.OCConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class OCNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private OCNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(
                ElevatorMovePayload.TYPE,
                ElevatorMovePayload.STREAM_CODEC,
                ElevatorMovePayload::handle);

        registrar.playToServer(
                HeightMapRequestPayload.TYPE,
                HeightMapRequestPayload.STREAM_CODEC,
                HeightMapRequestPayload::handle);

        registrar.playToClient(
                HeightMapResponsePayload.TYPE,
                HeightMapResponsePayload.STREAM_CODEC,
                HeightMapResponsePayload::handle);

        registrar.playToClient(
                HeightMapDirtyPayload.TYPE,
                HeightMapDirtyPayload.STREAM_CODEC,
                HeightMapDirtyPayload::handle);

        registrar.playToServer(
                CraneTogglePayload.TYPE,
                CraneTogglePayload.STREAM_CODEC,
                CraneTogglePayload::handle);

        registrar.playToServer(
                BrickDropPayload.TYPE,
                BrickDropPayload.STREAM_CODEC,
                BrickDropPayload::handle);

        registrar.playToServer(
                SetPaintColorPayload.TYPE,
                SetPaintColorPayload.STREAM_CODEC,
                SetPaintColorPayload::handle);
    }
}
