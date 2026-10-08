package dev.opencubes.content.book;

import dev.opencubes.util.PlayerFeedback;

import dev.opencubes.compat.PatchouliCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class InfoBookItem extends Item {

    public InfoBookItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (ModList.get().isLoaded("patchouli")) {
                PatchouliCompat.openBook(serverPlayer);
            } else {
                PlayerFeedback.tell(serverPlayer, 
                        Component.translatable("opencubes.misc.info_book_missing_patchouli"), true);
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
