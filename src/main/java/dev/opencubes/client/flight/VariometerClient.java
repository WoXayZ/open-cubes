package dev.opencubes.client.flight;

import com.mojang.blaze3d.platform.InputConstants;
import dev.opencubes.OCConstants;
import dev.opencubes.content.flight.GliderState;
import dev.opencubes.content.flight.HangGliderItem;
import dev.opencubes.content.flight.HangGliderPhysics;
import dev.opencubes.content.flight.ThermalElytraItem;
import dev.opencubes.registry.OCAttachments;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = OCConstants.MOD_ID, value = Dist.CLIENT)
public final class VariometerClient {

    public static final KeyMapping TOGGLE = new KeyMapping(
            "key.opencubes.variometer",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.opencubes");

    public static final KeyMapping VOLUME_UP = new KeyMapping(
            "key.opencubes.variometer_volume_up",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.opencubes");

    public static final KeyMapping VOLUME_DOWN = new KeyMapping(
            "key.opencubes.variometer_volume_down",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.opencubes");

    private static boolean enabled;
    private static int beepCooldown;
    /** 0.0–1.0, OpenBlocks-style volume steps via unbound keys. */
    private static float volume = 0.6F;

    private VariometerClient() {}

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
        event.register(VOLUME_UP);
        event.register(VOLUME_DOWN);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        while (TOGGLE.consumeClick()) {
            enabled = !enabled;
            player.displayClientMessage(Component.translatable(
                    enabled ? "opencubes.misc.variometer_on" : "opencubes.misc.variometer_off"), true);
        }
        while (VOLUME_UP.consumeClick()) {
            volume = Mth.clamp(volume + 0.1F, 0.0F, 1.0F);
            player.displayClientMessage(Component.translatable("opencubes.misc.variometer_volume",
                    Math.round(volume * 100.0F)), true);
        }
        while (VOLUME_DOWN.consumeClick()) {
            volume = Mth.clamp(volume - 0.1F, 0.0F, 1.0F);
            player.displayClientMessage(Component.translatable("opencubes.misc.variometer_volume",
                    Math.round(volume * 100.0F)), true);
        }

        if (!enabled || mc.isPaused() || volume <= 0.001F) {
            return;
        }

        boolean flying = HangGliderItem.isHoldingEngaged(player)
                || player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ThermalElytraItem
                && player.isFallFlying();
        if (!flying) {
            return;
        }

        GliderState state = player.getData(OCAttachments.GLIDER.get());
        double vspeed = state.lastVerticalSpeed();
        if (beepCooldown > 0) {
            beepCooldown--;
            return;
        }

        float rate = HangGliderPhysics.varioBeepRate(vspeed);
        if (rate <= 0) {
            return;
        }
        float freq = HangGliderPhysics.varioFrequency(vspeed);
        float pitch = Mth.clamp(freq / 1000.0F, 0.5F, 2.0F);
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), pitch, volume));
        beepCooldown = Math.max(2, (int) (20.0F / rate));
    }
}
