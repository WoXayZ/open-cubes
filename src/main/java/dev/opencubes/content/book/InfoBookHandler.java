package dev.opencubes.content.book;

import dev.opencubes.OCConstants;
import dev.opencubes.config.OCCommonConfig;
import dev.opencubes.registry.OCItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = OCConstants.MOD_ID)
public final class InfoBookHandler {

    private InfoBookHandler() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!OCCommonConfig.SPAM_INFO_BOOK.get()) {
            return;
        }
        if (player.getPersistentData().getBooleanOr("opencubes:got_info_book", false)) {
            return;
        }
        player.getPersistentData().putBoolean("opencubes:got_info_book", true);
        if (!player.getInventory().add(new ItemStack(OCItems.INFO_BOOK.get()))) {
            player.drop(new ItemStack(OCItems.INFO_BOOK.get()), false);
        }
    }
}
